package tab.aggregate;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

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
import tab.exception.TabAlreadyOpen;
import tab.exception.TabNotOpen;

public class TabAggregateMongo implements TabAggregate {

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Command>, CommandHandler> handlers;
	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> tabs;

	@SuppressWarnings("rawtypes")
	public TabAggregateMongo() {
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

		mongoClient = new MongoClient(Arrays.asList(new ServerAddress("mongo", 27017)));
		db = mongoClient.getDatabase("tabs");
		tabs = db.getCollection("tabs");
	}

	public void destroy() {
		mongoClient.close();
	}

	private class OpenTabHandler implements CommandHandler<OpenTab> {
		@Override
		public Collection<Event> handle(OpenTab c) {
			if (isTabOpenForTable(c.getTableNumber()))
				throw new TabAlreadyOpen("Tab for tableNumber: " + c.getTableNumber() + " already open");
			TabOpened tabOpened = new TabOpened(c.getId(), c.getTableNumber(), c.getWaiter());
			return Arrays.asList(tabOpened);
		}
	}

	private class PlaceOrderHandler implements CommandHandler<PlaceOrder> {
		@Override
		public Collection<Event> handle(PlaceOrder c) {
			if (!isOpen(c.getId()))
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
			if (!isOpen(c.getId()))
				throw new TabNotOpen();
			if (!areDrinksOutstanding(c.getId(), c.getMenuItems()))
				throw new DrinksNotOutstanding();
			Collection<Event> events = new ArrayList<>();
			events.add(new DrinksServed(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class MarkFoodPreparedHandler implements CommandHandler<MarkFoodPrepared> {
		@Override
		public Collection<Event> handle(MarkFoodPrepared c) {
			if (!isOpen(c.getId()))
				throw new TabNotOpen();
			if (!areFoodOutstanding(c.getId(), c.getMenuItems()))
				throw new FoodNotOutstanding();
			Collection<Event> events = new ArrayList<>();
			events.add(new FoodPrepared(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class MarkFoodServedHandler implements CommandHandler<MarkFoodServed> {
		@Override
		public Collection<Event> handle(MarkFoodServed c) {
			if (!isOpen(c.getId()))
				throw new TabNotOpen();
			if (!areFoodPrepared(c.getId(), c.getMenuItems()))
				throw new FoodNotPrepared();
			Collection<Event> events = new ArrayList<>();
			events.add(new FoodServed(c.getId(), c.getMenuItems()));
			return events;
		}
	}

	private class CloseTabHandler implements CommandHandler<CloseTab> {

		@SuppressWarnings("unchecked")
		private double getServedValue(long id) {
			Document doc = tabs.find(eq("idTab", id)).first();
			List<Document> items = (List<Document>) doc.get("ordered_items");
			double total = 0;
			for (Document item : items) {
				double price = item.getDouble("price");
				total += price;
			}
			return total;
		}

		@Override
		public Collection<Event> handle(CloseTab c) {
			long id = c.getId();
			if (!isOpen(id))
				throw new TabNotOpen();
			if (!isEverythingServed(id))
				throw new NotEverythingServed();
			double ap = c.getAmountPaid();
			double servedItemsValue = getServedValue(id);
			if (ap < servedItemsValue)
				throw new NotEnough();
			Collection<Event> events = new ArrayList<>();

			TabClosed tc = new TabClosed(id, ap, servedItemsValue, ap - servedItemsValue);
			events.add(tc);
			return events;
		}
	}

	private class TabOpenedApplier implements ApplyEvent<TabOpened> {

		private Document eventToTab(TabOpened event) {
			Document doc = new Document().append("idTab", event.getId()).append("open", true)
					.append("table_number", event.getTableNumber()).append("served_value", 0.0);
			return doc;
		}

		@Override
		public void apply(TabOpened event) {
			Document tab = eventToTab(event);
			tabs.insertOne(tab);
		}

	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {

		private List<Document> eventToTab(DrinksOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("menu_number", item.getMenuNumber())
						.append("description", item.getDescription()).append("price", item.getPrice())
						.append("state", "TO_SERVE_DRINK");
				docs.add(doc);
			}

			return docs;

		}

		@Override
		public void apply(DrinksOrdered event) {
			List<Document> drinks = eventToTab(event);
			tabs.updateOne(eq("idTab", event.getId()), pushEach("ordered_item", drinks));
		}
	}

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {

		private class FilterAndUpdate {
			public Bson filter;
			public Bson value;

			public FilterAndUpdate(Bson filter, Bson value) {
				super();
				this.filter = filter;
				this.value = value;
			}

		}

		private List<FilterAndUpdate> updateFilter(DrinksServed event) {
			List<FilterAndUpdate> faus = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("ordered_item.$.menu_number", number),
						eq("ordered_item.$.state", "TO_SERVE_DRINK"));
				Bson value = set("ordered_item.$.state", "SERVED");
				faus.add(new FilterAndUpdate(filter, value));
			}
			return faus;
		}

		@Override
		public void apply(DrinksServed event) {
			List<FilterAndUpdate> faus = updateFilter(event);
			for (FilterAndUpdate fau : faus)
				tabs.updateOne(fau.filter, fau.value);
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {

		private List<Document> eventToTab(FoodOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("menu_number", item.getMenuNumber())
						.append("description", item.getDescription()).append("price", item.getPrice())
						.append("state", "TO_PREPARE");
				docs.add(doc);
			}

			return docs;
		}

		@Override
		public void apply(FoodOrdered event) {
			List<Document> food = eventToTab(event);
			tabs.updateOne(eq("idTab", event.getId()), pushEach("ordered_item", food));
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {

		private class FilterAndUpdate {
			public Bson filter;
			public Bson value;

			public FilterAndUpdate(Bson filter, Bson value) {
				super();
				this.filter = filter;
				this.value = value;
			}

		}

		private List<FilterAndUpdate> updateFilter(FoodPrepared event) {
			List<FilterAndUpdate> faus = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("ordered_item.$.menu_number", number),
						eq("ordered_item.$.state", "TO_PREPARE"));
				Bson value = set("ordered_item.$.state", "TO_SERVE_FOOD");
				faus.add(new FilterAndUpdate(filter, value));
			}
			return faus;
		}

		@Override
		public void apply(FoodPrepared event) {
			List<FilterAndUpdate> faus = updateFilter(event);
			for (FilterAndUpdate fau : faus)
				tabs.updateOne(fau.filter, fau.value);
		}
	}

	private class FoodServedApplier implements ApplyEvent<FoodServed> {
		private class FilterAndUpdate {
			public Bson filter;
			public Bson value;

			public FilterAndUpdate(Bson filter, Bson value) {
				super();
				this.filter = filter;
				this.value = value;
			}

		}

		private List<FilterAndUpdate> updateFilter(FoodServed event) {
			List<FilterAndUpdate> faus = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("ordered_item.$.menu_number", number),
						eq("ordered_item.$.state", "TO_SERVE_FOOD"));
				Bson value = set("ordered_item.$.state", "SERVED");
				faus.add(new FilterAndUpdate(filter, value));
			}
			return faus;
		}

		@Override
		public void apply(FoodServed event) {
			List<FilterAndUpdate> faus = updateFilter(event);
			for (FilterAndUpdate fau : faus)
				tabs.updateOne(fau.filter, fau.value);
		}
	}

	private class TabClosedApplier implements ApplyEvent<TabClosed> {

		private Bson updateFilter(TabClosed event) {
			Bson filter = null;
			filter = eq("idTab", event.getId());
			return filter;
		}

		private Bson updateSet(TabClosed event) {
			Bson set = null;
			set = combine(set("open", false), set("amount_paid", event.getAumountPaid()),
					set("served_value", event.getOrderValue()), set("tip", event.getTipValue()));
			return set;
		}

		@Override
		public void apply(TabClosed event) {
			tabs.updateOne(updateFilter(event), updateSet(event));
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

	@SuppressWarnings("unchecked")
	private List<Integer> getItemFromList(long id, String listType) {
		List<Document> orderedItems = null;
		Document tab = null;
		tab = tabs.find(eq("idTab", id)).first();
		orderedItems = (List<Document>) tab.get("ordered_item");
		List<Integer> list = new ArrayList<>();
		for (Document item : orderedItems) {
			if (listType.equals(item.getString("state")))
				list.add(item.getInteger("menu_number"));
		}
		return list;
	}

	private boolean isTabOpenForTable(int tableNumber) {
		MongoCursor<Document> cursor = null;
		cursor = tabs.find(eq("table_number", tableNumber)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			boolean open = doc.getBoolean("open", false);
			if (open)
				return true;
		}
		return false;
	}

	private boolean areDrinksOutstanding(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, "TO_SERVE_DRINK");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodOutstanding(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, "TO_PREPARE");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodPrepared(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, "TO_SERVE_FOOD");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean isEverythingServed(long tabId) {
		FindIterable<Document> iterable = null;
		iterable = tabs.find(and(eq("idTab", tabId), ne("ordered_item.$.state", "SERVED"))).limit(1);
		if (iterable.iterator().hasNext())
			return false;
		return true;
	}

	private boolean isOpen(long id) {
		MongoCursor<Document> cursor = null;
		cursor = tabs.find(eq("idTab", id)).limit(1).iterator();
		if (!cursor.hasNext())
			return false;
		Document doc = cursor.next();
		boolean open = doc.getBoolean("open", false);
		return open;
	}

}
