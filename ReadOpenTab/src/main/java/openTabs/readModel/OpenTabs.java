package openTabs.readModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import openTabs.ApplyEvent;
import openTabs.events.DrinksOrdered;
import openTabs.events.DrinksServed;
import openTabs.events.Event;
import openTabs.events.FoodOrdered;
import openTabs.events.FoodPrepared;
import openTabs.events.FoodServed;
import openTabs.events.OrderedItem;
import openTabs.events.TabClosed;
import openTabs.events.TabOpened;

@SuppressWarnings("rawtypes")
public class OpenTabs implements ApplyEvent, OpenTabsQueries, ReadModel {
	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private Map<Long, TableTodo> todoByTab;

	public OpenTabs() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(TabOpened.class, new TabOpenedApplier());
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(DrinksOrdered.class, new DrinksOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		aTemp.put(FoodServed.class, new FoodServedApplier());
		aTemp.put(DrinksServed.class, new DrinksServedApplier());
		aTemp.put(TabClosed.class, new TabClosedApplier());
		applier = Collections.unmodifiableMap(aTemp);

		todoByTab = new HashMap<>();
	}

	private class TabOpenedApplier implements ApplyEvent<TabOpened> {
		@Override
		public void apply(TabOpened event) {
			TableTodo tt = new TableTodo(event.getId(), event.getTableNumber(), event.getWaiter());
			synchronized (todoByTab) {
				todoByTab.put(event.getId(), tt);
			}
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		@Override
		public void apply(FoodOrdered event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				for (OrderedItem oi : event.getItems()) {
					ItemTodo item = new ItemTodo(oi.getMenuNumber(), oi.getDescription(), oi.getPrice());
					tt.addInPreparation(item);
				}
			}
		}
	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {
		@Override
		public void apply(DrinksOrdered event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				for (OrderedItem oi : event.getItems()) {
					ItemTodo item = new ItemTodo(oi.getMenuNumber(), oi.getDescription(), oi.getPrice());
					tt.addDrinkToServe(item);
				}
			}
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		@Override
		public void apply(FoodPrepared event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				for (Integer num : event.getMenuItems()) {
					ItemTodo item = tt.removeByMenuNumberInPreparation(num);
					tt.addFoodToServe(item);
				}
			}
		}
	}

	private class FoodServedApplier implements ApplyEvent<FoodServed> {
		@Override
		public void apply(FoodServed event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				for (Integer num : event.getMenuItems()) {
					tt.removeByMenuNumberFoodToServe(num);
				}
			}
		}
	}

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {
		@Override
		public void apply(DrinksServed event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				for (Integer num : event.getMenuItems()) {
					tt.removeByMenuNumberDrinkToServe(num);
				}
			}
		}
	}

	private class TabClosedApplier implements ApplyEvent<TabClosed> {
		@Override
		public void apply(TabClosed event) {
			synchronized (todoByTab) {
				TableTodo tt = todoByTab.get(event.getId());
				tt.setAmountPaid(event.getAumountPaid());
				tt.setToPay(event.getOrderValue());
				tt.setTip(event.getTipValue());
				tt.setClosed(true);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void apply(Event event) {
		if (applier.containsKey(event.getClass())) {
			applier.get(event.getClass()).apply(event);
		} else {
			throw new IllegalArgumentException("Event not supported: " + event.getClass());
		}
	}

	@Override
	public TableTodo getByTab(long idTab) {
		synchronized (todoByTab) {
			TableTodo tt = todoByTab.get(idTab);
			TableTodo copy = tt.clone();
			return copy;
		}
	}

	@Override
	public TableTodo getOpenByTableNumber(int tableNumber) {
		synchronized (todoByTab) {
			Optional<TableTodo> ott = todoByTab.values().stream()
					.filter(t -> t.getTableNumber() == tableNumber && !t.isClosed()).findFirst();
			if (ott.isPresent())
				return ott.get().clone();
			else
				return null;
		}
	}

	@Override
	public Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter) {
		synchronized (todoByTab) {
			Map<Integer, List<ItemTodo>> map = todoByTab.values().stream().filter(t -> t.getWaiter().equals(waiter))
					.collect(Collectors.toMap(t -> t.getTableNumber(), t -> t.getToServe()));
			return map;
		}
	}

	private Double getToPay(TableTodo tt) {
		if (tt == null)
			return null;
		double total = 0;
		Optional<Double> temp = tt.getDrinkToServe().stream().map(it -> it.getPrice()).reduce((u, v) -> u + v);
		if (temp.isPresent())
			total += temp.get();
		temp = tt.getFoodToServe().stream().map(it -> it.getPrice()).reduce((u, v) -> u + v);
		if (temp.isPresent())
			total += temp.get();
		temp = tt.getInPreparation().stream().map(it -> it.getPrice()).reduce((u, v) -> u + v);
		if (temp.isPresent())
			total += temp.get();
		return total;
	}

	public Double getPriceToPayByTab(long idTab) {
		TableTodo tt = getByTab(idTab);
		return getToPay(tt);
	}

	public Double getPriceToPayByTableNumber(int tableNumber) {
		TableTodo tt = getOpenByTableNumber(tableNumber);
		return getToPay(tt);
	}
}
