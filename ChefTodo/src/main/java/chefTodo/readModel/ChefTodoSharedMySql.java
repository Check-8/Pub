package chefTodo.readModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import chefTodo.ApplyEvent;
import chefTodo.events.Event;

@SuppressWarnings("rawtypes")
public class ChefTodoSharedMySql implements ApplyEvent, ChefTodoListQueries, ReadModel {
	private static final String SELECT = "SELECT tab_id, menu_number, description FROM ordereditem WHERE list_type=\"TO_PREPARE\";";

	@Autowired
	private DataSource dataSource;

	@Override
	public void apply(Event event) {
		// DO NOTHING
	}

	@Override
	public List<TodoListGroup> getTodoList() {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT);) {
			Map<Long, TodoListGroup> id2group = new HashMap<>();
			try (ResultSet results = statement.executeQuery();) {
				while (results.next()) {
					long idTab = results.getLong("tab_id");
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
