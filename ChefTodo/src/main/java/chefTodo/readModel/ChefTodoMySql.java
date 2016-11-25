package chefTodo.readModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import chefTodo.ApplyEvent;
import chefTodo.events.Event;
import chefTodo.events.FoodOrdered;
import chefTodo.events.FoodPrepared;
import chefTodo.events.OrderedItem;

@SuppressWarnings("rawtypes")
public class ChefTodoMySql implements ApplyEvent, ChefTodoListQueries, ReadModel {
	private static final String SELECT = "SELECT tab, menu_number, description FROM todoitem;";

	private final Map<Class<? extends Event>, ApplyEvent> applier;

	@Autowired
	private DataSource dataSource;

	public ChefTodoMySql() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		applier = Collections.unmodifiableMap(aTemp);
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		private static final String INSERT_TODOITEM = "INSERT INTO todoitem (tab, menu_number, description) VALUES (?,?,?);";

		@Override
		public void apply(FoodOrdered event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_TODOITEM);) {
				long id = event.getId();

				for (OrderedItem item : event.getItems()) {
					statement.setLong(1, id);
					statement.setInt(2, item.getMenuNumber());
					statement.setString(3, item.getDescription());

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		private static final String DELETE_TODOITEM = "DELETE FROM todoitem WHERE tab=? AND menu_number=? LIMIT 1;";

		@Override
		public void apply(FoodPrepared event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(DELETE_TODOITEM);) {
				long id = event.getId();

				for (Integer menuNumber : event.getMenuItems()) {
					statement.setLong(1, id);
					statement.setInt(2, menuNumber);

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
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
	public List<TodoListGroup> getTodoList() {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT);) {
			Map<Long, TodoListGroup> id2group = new HashMap<>();
			try (ResultSet results = statement.executeQuery();) {
				while (results.next()) {
					long idTab = results.getLong("tab");
					TodoListGroup tg = id2group.get(idTab);
					if (tg == null) {
						tg = new TodoListGroup(idTab, new ArrayList<>());
						id2group.put(idTab, tg);
					}
					int menuNumber = results.getInt("menu_number");
					String description = results.getString("description");
					TodoListItem ti = new TodoListItem(menuNumber, description);
					tg.getItems().add(ti);
				}
			}
			return new ArrayList<>(id2group.values());

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
