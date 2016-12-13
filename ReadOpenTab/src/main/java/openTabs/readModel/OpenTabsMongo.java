package openTabs.readModel;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

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
public class OpenTabsMongo implements ApplyEvent, OpenTabsQueries, ReadModel {

	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> tabs;
	private MongoCollection<Document> orderedItems;

	public OpenTabsMongo() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(TabOpened.class, new TabOpenedApplier());
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(DrinksOrdered.class, new DrinksOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		aTemp.put(FoodServed.class, new FoodServedApplier());
		aTemp.put(DrinksServed.class, new DrinksServedApplier());
		aTemp.put(TabClosed.class, new TabClosedApplier());
		applier = Collections.unmodifiableMap(aTemp);

		mongoClient = new MongoClient(Arrays.asList(new ServerAddress("mongo", 27017)));
		db = mongoClient.getDatabase("opentabs");
		tabs = db.getCollection("tabs");
		orderedItems = db.getCollection("orderedItems");
	}

	@PreDestroy
	public void destroy() {
		mongoClient.close();
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

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {

		private List<Document> eventToTab(String waiter, int tableNumber, FoodOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("waiter", waiter)
						.append("table_number", tableNumber).append("menu_number", item.getMenuNumber())
						.append("description", item.getDescription()).append("price", item.getPrice())
						.append("state", "TO_PREPARE");
				docs.add(doc);
			}
			return docs;
		}

		@Override
		public void apply(FoodOrdered event) {
			String waiter = getWaiterForTab(event.getId());
			int tableNumber = getTableNumberForTab(event.getId());
			List<Document> food = eventToTab(waiter, tableNumber, event);
			orderedItems.insertMany(food);
		}
	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {

		private List<Document> eventToTab(String waiter, int tableNumber, DrinksOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("waiter", waiter)
						.append("table_number", tableNumber).append("menu_number", item.getMenuNumber())
						.append("description", item.getDescription()).append("price", item.getPrice())
						.append("state", "TO_SERVE_DRINK");
				docs.add(doc);
			}

			return docs;

		}

		@Override
		public void apply(DrinksOrdered event) {
			String waiter = getWaiterForTab(event.getId());
			int tableNumber = getTableNumberForTab(event.getId());
			List<Document> drinks = eventToTab(waiter, tableNumber, event);
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
	public void apply(Event event) {
		if (applier.containsKey(event.getClass())) {
			applier.get(event.getClass()).apply(event);
		} else {
			throw new IllegalArgumentException("Event not supported: " + event.getClass());
		}
	}

	private List<ItemTodo> getItems(long idTab, String state) {
		MongoCursor<Document> cursor = null;
		cursor = orderedItems.find(and(eq("idTab", idTab), eq("state", state))).iterator();
		List<ItemTodo> lists = new ArrayList<>();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			int menuNumber = doc.getInteger("menu_number");
			String description = doc.getString("description");
			double price = doc.getDouble("price");
			ItemTodo it = new ItemTodo(menuNumber, description, price);
			lists.add(it);
		}
		return lists;
	}

	@Override
	public TableTodo getByTab(long idTab) {
		Document doc = tabs.find(eq("idTab", idTab)).first();
		int tableNumber = doc.getInteger("table_number");
		String waiter = doc.getString("waiter");
		double amountPaid = doc.getDouble("amount_paid");
		double tip = doc.getDouble("tip");
		double toPay = doc.getDouble("served_value");
		boolean open = doc.getBoolean("open");
		List<ItemTodo> drinksToServe = getItems(idTab, "TO_SERVE_DRINK");
		List<ItemTodo> foodToServe = getItems(idTab, "TO_SERVE_FOOD");
		List<ItemTodo> inPreparation = getItems(idTab, "TO_PREPARE");
		TableTodo tt = new TableTodo(idTab, tableNumber, waiter, drinksToServe, foodToServe, inPreparation);
		tt.setAmountPaid(amountPaid);
		tt.setTip(tip);
		tt.setToPay(toPay);
		tt.setClosed(open);
		return tt;
	}

	@Override
	public TableTodo getOpenByTableNumber(int tableNumber) {
		Document doc = tabs.find(and(eq("table_number", tableNumber), eq("open", true))).first();
		long idTab = doc.getLong("idTab");
		String waiter = doc.getString("waiter");
		double amountPaid = doc.getDouble("amount_paid");
		double tip = doc.getDouble("tip");
		double toPay = doc.getDouble("served_value");
		boolean open = doc.getBoolean("open");
		List<ItemTodo> drinksToServe = getItems(idTab, "TO_SERVE_DRINK");
		List<ItemTodo> foodToServe = getItems(idTab, "TO_SERVE_FOOD");
		List<ItemTodo> inPreparation = getItems(idTab, "TO_PREPARE");
		TableTodo tt = new TableTodo(idTab, tableNumber, waiter, drinksToServe, foodToServe, inPreparation);
		tt.setAmountPaid(amountPaid);
		tt.setTip(tip);
		tt.setToPay(toPay);
		tt.setClosed(open);
		return tt;
	}

	@Override
	public Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter) {
		MongoCursor<Document> cursor = null;
		cursor = orderedItems.find(and(eq("waiter", waiter), eq("state", "/^TO_SERVE/"))).iterator();
		Map<Integer, List<ItemTodo>> map = new HashMap<>();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			int tableNumber = doc.getInteger("table_number");
			int menuNumber = doc.getInteger("menu_number");
			String description = doc.getString("description");
			double price = doc.getDouble("price");
			List<ItemTodo> list = map.get(tableNumber);
			if (list == null) {
				list = new ArrayList<>();
				map.put(tableNumber, list);
			}
			list.add(new ItemTodo(menuNumber, description, price));
		}
		return map;
	}

	@Override
	public Double getPriceToPayByTab(long idTab) {
		MongoCursor<Document> cursor = null;
		cursor = orderedItems
				.aggregate(Arrays.asList(match(eq("idTab", idTab)), group("$idTab", sum("total", "$price"))))
				.iterator();
		double total = 0;
		if (cursor.hasNext())
			total = cursor.next().getDouble("total");
		return total;
	}

	public Double getPriceToPayByTableNumber(int tableNumber) {
		MongoCursor<Document> cursor = null;
		Bson filter = and(eq("table_number", tableNumber), eq("open", true));
		List<Bson> aggrFunction = Arrays.asList(match(filter), group("$idTab", sum("total", "$price")));
		cursor = orderedItems.aggregate(aggrFunction).iterator();
		double total = 0;
		if (cursor.hasNext())
			total = cursor.next().getDouble("total");
		return total;
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

	private int getTableNumberForTab(long idTab) {
		MongoCursor<Document> cursor = null;
		cursor = tabs.find(eq("idTab", idTab)).limit(1).iterator();
		if (!cursor.hasNext())
			return -1;
		Document doc = cursor.next();
		int tableNumber = doc.getInteger("table_number");
		return tableNumber;
	}
}
