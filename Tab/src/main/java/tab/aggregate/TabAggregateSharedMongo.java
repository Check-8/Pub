package tab.aggregate;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
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

public class TabAggregateSharedMongo implements TabAggregate {

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Command>, CommandHandler> handlers;
	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> tabs;
	private MongoCollection<Document> orderedItems;

	@SuppressWarnings("rawtypes")
	public TabAggregateSharedMongo() {
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
		db = mongoClient.getDatabase("monoTabs");
		tabs = db.getCollection("tabs");
		orderedItems = db.getCollection("orderedItems");
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

		private double getServedValue(long id) {
			MongoCursor<Document> cursor = null;
			cursor = orderedItems
					.aggregate(Arrays.asList(match(eq("idTab", id)), group("$idTab", sum("total", "$price"))))
					.iterator();
			double total = 0;
			if (cursor.hasNext())
				total = cursor.next().getDouble("total");
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
			Document doc = new Document().append("idTab", event.getId()).append("table_number", event.getTableNumber())
					.append("waiter", event.getWaiter()).append("open", true).append("served_value", 0.0)
					.append("amount_paid", 0.0).append("tip", 0.0);
			return doc;
		}

		@Override
		public void apply(TabOpened event) {
			Document tab = eventToTab(event);
			tabs.insertOne(tab);
		}

	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {

		private List<Document> eventToTab(String waiter, DrinksOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("waiter", waiter)
						.append("menu_number", item.getMenuNumber()).append("description", item.getDescription())
						.append("price", item.getPrice()).append("state", "TO_SERVE_DRINK");
				docs.add(doc);
			}

			return docs;

		}

		@Override
		public void apply(DrinksOrdered event) {
			String waiter = getWaiterForTab(event.getId());
			List<Document> drinks = eventToTab(waiter, event);
			orderedItems.insertMany(drinks);
		}
	}

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {

		private List<Bson> updateFilter(DrinksServed event) {
			List<Bson> filters = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("menu_number", number), eq("state", "TO_SERVE_DRINK"));
				filters.add(filter);
			}
			return filters;
		}

		@Override
		public void apply(DrinksServed event) {
			List<Bson> filters = updateFilter(event);
			for (Bson filter : filters)
				orderedItems.updateOne(filter, set("state", "SERVED"));
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {

		private List<Document> eventToTab(String waiter, FoodOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("waiter", waiter)
						.append("menu_number", item.getMenuNumber()).append("description", item.getDescription())
						.append("price", item.getPrice()).append("state", "TO_PREPARE");
				docs.add(doc);
			}
			return docs;
		}

		@Override
		public void apply(FoodOrdered event) {
			String waiter = getWaiterForTab(event.getId());
			List<Document> drinks = eventToTab(waiter, event);
			orderedItems.insertMany(drinks);
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {

		private List<Bson> updateFilter(FoodPrepared event) {
			List<Bson> filters = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("menu_number", number), eq("state", "TO_PREPARE"));
				filters.add(filter);
			}
			return filters;
		}

		@Override
		public void apply(FoodPrepared event) {
			List<Bson> filters = updateFilter(event);
			for (Bson filter : filters)
				orderedItems.updateOne(filter, set("state", "TO_SERVE_FOOD"));
		}
	}

	private class FoodServedApplier implements ApplyEvent<FoodServed> {

		private List<Bson> updateFilter(FoodServed event) {
			List<Bson> filters = new ArrayList<>(event.getMenuItems().size());
			for (Integer number : event.getMenuItems()) {
				Bson filter = and(eq("idTab", event.getId()), eq("menu_number", number), eq("state", "TO_SERVE_FOOD"));
				filters.add(filter);
			}
			return filters;
		}

		@Override
		public void apply(FoodServed event) {
			List<Bson> filters = updateFilter(event);
			for (Bson filter : filters)
				orderedItems.updateOne(filter, set("state", "SERVED"));
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

	private List<Integer> getItemFromList(long id, String listType) {
		MongoCursor<Document> cursor = null;
		cursor = orderedItems.find(and(eq("idTab", id), eq("state", listType))).iterator();
		List<Integer> list = new ArrayList<>();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(doc.getInteger("menu_number"));
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
		iterable = orderedItems.find(and(eq("idTab", tabId), ne("state", "SERVED"))).limit(1);
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

	private String getWaiterForTab(long idTab) {
		MongoCursor<Document> cursor = null;
		cursor = tabs.find(eq("idTab", idTab)).limit(1).iterator();
		if (!cursor.hasNext())
			return null;
		Document doc = cursor.next();
		String waiter = doc.getString("waiter");
		return waiter;
	}

}
