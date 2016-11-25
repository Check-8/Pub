package tab.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import tab.commands.CloseTab;
import tab.commands.Command;
import tab.commands.MarkDrinksServed;
import tab.commands.MarkFoodPrepared;
import tab.commands.MarkFoodServed;
import tab.commands.OpenTab;
import tab.commands.PlaceOrder;
import tab.events.DrinksOrdered;
import tab.events.DrinksServed;
import tab.events.Event;
import tab.events.FoodOrdered;
import tab.events.FoodPrepared;
import tab.events.FoodServed;
import tab.events.OrderedItem;
import tab.events.TabClosed;
import tab.events.TabOpened;
import tab.exception.DrinksNotOutstanding;
import tab.exception.FoodNotOutstanding;
import tab.exception.FoodNotPrepared;
import tab.exception.NotEnough;
import tab.exception.NotEverythingServed;
import tab.exception.TabNotOpen;

@SuppressWarnings("rawtypes")
public class TabAggregateMem implements TabAggregate {

	private final Map<Class<? extends Command>, CommandHandler> handlers;
	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private List<OrderedItem> outstandingDrinks;
	private List<OrderedItem> outstandingFood;
	private List<OrderedItem> preparedFood;
	private boolean open;
	private double servedItemsValue;

	public TabAggregateMem() {
		Map<Class<? extends Command>, CommandHandler> cTemp = new HashMap<>();
		cTemp.put(OpenTab.class, new OpenTabHandler());
		cTemp.put(PlaceOrder.class, new PlaceOrderHandler());
		cTemp.put(MarkDrinksServed.class, new MarkDrinksServedHandler());
		cTemp.put(MarkFoodPrepared.class, new MarkFoodPreparedHandler());
		cTemp.put(MarkFoodServed.class, new MarkFoodServedHandler());
		cTemp.put(CloseTab.class, new CloseTabHandler());
		handlers = Collections.unmodifiableMap(cTemp);

		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(TabOpened.class, new TabOpenedApplier());
		aTemp.put(DrinksOrdered.class, new DrinksOrderedApplier());
		aTemp.put(DrinksServed.class, new DrinksServedApplier());
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		aTemp.put(FoodServed.class, new FoodServedApplier());
		aTemp.put(TabClosed.class, new TabClosedApplier());
		applier = Collections.unmodifiableMap(aTemp);

		open = false;
		servedItemsValue = 0;
		outstandingDrinks = new ArrayList<>();
		outstandingFood = new ArrayList<>();
		preparedFood = new ArrayList<>();
	}

	private class OpenTabHandler implements CommandHandler<OpenTab> {
		@Override
		public Collection<Event> handle(OpenTab c) {
			TabOpened tabOpened = new TabOpened(c.getId(), c.getTableNumber(), c.getWaiter());
			return Arrays.asList(tabOpened);
		}
	}

	private class PlaceOrderHandler implements CommandHandler<PlaceOrder> {
		@Override
		public Collection<Event> handle(PlaceOrder c) {
			if (!open)
				throw new TabNotOpen();
			Collection<Event> events = new ArrayList<>();
			List<OrderedItem> drinks = null;
			drinks = c.getItems().stream().filter(i -> i.isDrink()).collect(Collectors.toList());
			if (!drinks.isEmpty()) {
				events.add(new DrinksOrdered(c.getId(), drinks));
			}
			List<OrderedItem> food = null;
			food = c.getItems().stream().filter(i -> !i.isDrink()).collect(Collectors.toList());
			if (!food.isEmpty()) {
				events.add(new FoodOrdered(c.getId(), food));
			}
			return events;
		}
	}

	private class MarkDrinksServedHandler implements CommandHandler<MarkDrinksServed> {
		@Override
		public Collection<Event> handle(MarkDrinksServed c) {
			if (!open)
				throw new TabNotOpen();
			if (!areDrinksOutstanding(c.getMenuItems()))
				throw new DrinksNotOutstanding();
			Collection<Event> events = new ArrayList<>();
			events.add(new DrinksServed(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class MarkFoodPreparedHandler implements CommandHandler<MarkFoodPrepared> {
		@Override
		public Collection<Event> handle(MarkFoodPrepared c) {
			if (!open)
				throw new TabNotOpen();
			if (!areFoodOutstanding(c.getMenuItems()))
				throw new FoodNotOutstanding();
			Collection<Event> events = new ArrayList<>();
			events.add(new FoodPrepared(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class MarkFoodServedHandler implements CommandHandler<MarkFoodServed> {
		@Override
		public Collection<Event> handle(MarkFoodServed c) {
			if (!open)
				throw new TabNotOpen();
			if (!areFoodPrepared(c.getMenuItems()))
				throw new FoodNotPrepared();
			Collection<Event> events = new ArrayList<>();
			events.add(new FoodServed(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class CloseTabHandler implements CommandHandler<CloseTab> {
		@Override
		public Collection<Event> handle(CloseTab c) {
			if (!open)
				throw new TabNotOpen();
			if(!outstandingDrinks.isEmpty()||!outstandingFood.isEmpty()||!preparedFood.isEmpty())
				throw new NotEverythingServed();
			double ap = c.getAmountPaid();
			if (ap < servedItemsValue)
				throw new NotEnough();
			Collection<Event> events = new ArrayList<>();

			TabClosed tc = new TabClosed(c.getId(), ap, servedItemsValue, ap - servedItemsValue);
			events.add(tc);
			return events;
		}
	}

	private class TabOpenedApplier implements ApplyEvent<TabOpened> {

		@Override
		public void apply(TabOpened event) {
			open = true;
		}

	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {
		@Override
		public void apply(DrinksOrdered event) {
			outstandingDrinks.addAll(event.getItems());
		}
	}

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {
		@Override
		public void apply(DrinksServed event) {
			for (Integer num : event.getMenuItems()) {
				Optional<OrderedItem> optional = null;
				optional = outstandingDrinks.stream().filter(i -> i.getMenuNumber() == num).findFirst();
				OrderedItem item = optional.get();
				outstandingDrinks.remove(item);
				servedItemsValue += item.getPrice();
			}
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		@Override
		public void apply(FoodOrdered event) {
			outstandingFood.addAll(event.getItems());
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		@Override
		public void apply(FoodPrepared event) {
			for (Integer num : event.getMenuItems()) {
				Optional<OrderedItem> optional = null;
				optional = outstandingFood.stream().filter(i -> i.getMenuNumber() == num).findFirst();
				OrderedItem item = optional.get();
				outstandingFood.remove(item);
				preparedFood.add(item);
			}
		}
	}

	private class FoodServedApplier implements ApplyEvent<FoodServed> {
		@Override
		public void apply(FoodServed event) {
			for (Integer num : event.getMenuItems()) {
				Optional<OrderedItem> optional = null;
				optional = preparedFood.stream().filter(i -> i.getMenuNumber() == num).findFirst();
				OrderedItem item = optional.get();
				preparedFood.remove(item);
				servedItemsValue += item.getPrice();
			}
		}
	}

	private class TabClosedApplier implements ApplyEvent<TabClosed> {

		@Override
		public void apply(TabClosed event) {
			open = false;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Event> handle(Command command) {
		if (handlers.containsKey(command.getClass())) {
			return handlers.get(command.getClass()).handle(command);
		} else {
			throw new IllegalArgumentException("Command not supported: " + command.getClass());
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

	private boolean areDrinksOutstanding(List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = new ArrayList<>(
				outstandingDrinks.stream().map(i -> i.getMenuNumber()).collect(Collectors.toList()));
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodOutstanding(List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = new ArrayList<>(
				outstandingFood.stream().map(i -> i.getMenuNumber()).collect(Collectors.toList()));
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodPrepared(List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = new ArrayList<>(
				preparedFood.stream().map(i -> i.getMenuNumber()).collect(Collectors.toList()));
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

}
