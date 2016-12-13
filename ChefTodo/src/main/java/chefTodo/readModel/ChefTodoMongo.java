package chefTodo.readModel;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import chefTodo.ApplyEvent;
import chefTodo.events.Event;
import chefTodo.events.FoodOrdered;
import chefTodo.events.FoodPrepared;
import chefTodo.events.OrderedItem;

@SuppressWarnings("rawtypes")
public class ChefTodoMongo implements ApplyEvent, ChefTodoListQueries, ReadModel {

	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> chef;

	public ChefTodoMongo() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		applier = Collections.unmodifiableMap(aTemp);

		mongoClient = new MongoClient(Arrays.asList(new ServerAddress("mongo", 27017)));
		db = mongoClient.getDatabase("chef");
		chef = db.getCollection("chef");
	}
	
	@PreDestroy
	public void destroy() {
		mongoClient.close();
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {

		private List<Document> eventToTab(FoodOrdered event) {
			List<Document> docs = new ArrayList<>(event.getItems().size());
			for (OrderedItem item : event.getItems()) {
				Document doc = new Document().append("idTab", event.getId()).append("menu_number", item.getMenuNumber())
						.append("description", item.getDescription());
				docs.add(doc);
			}
			return docs;
		}

		@Override
		public void apply(FoodOrdered event) {
			List<Document> drinks = eventToTab(event);
			chef.insertMany(drinks);
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {

		@Override
		public void apply(FoodPrepared event) {
			long idTab = event.getId();
			for (Integer mn : event.getMenuItems())
				chef.deleteOne(and(eq("idTab", idTab), eq("menu_number", mn)));
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
	public List<TodoListGroup> getTodoList() {
		Map<Long, TodoListGroup> tlgs = new HashMap<>();
		MongoCursor<Document> cursor = null;
		cursor = chef.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			long idTab = doc.getLong("idTab");
			int mn = doc.getInteger("menu_number");
			String desc = doc.getString("description");
			TodoListGroup tlg = tlgs.get(idTab);
			if (tlg == null) {
				tlg = new TodoListGroup(idTab, new ArrayList<>());
				tlgs.put(idTab, tlg);
			}
			tlg.getItems().add(new TodoListItem(mn, desc));
		}
		return new ArrayList<>(tlgs.values());
	}
}
