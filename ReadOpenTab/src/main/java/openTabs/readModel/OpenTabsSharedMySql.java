package openTabs.readModel;

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

import openTabs.ApplyEvent;
import openTabs.events.Event;

@SuppressWarnings("rawtypes")
public class OpenTabsSharedMySql implements ApplyEvent, OpenTabsQueries, ReadModel {
	private static final String SELECT_ITEM_TODO_BY_TABID = "SELECT menu_number, description, price FROM ordereditem WHERE tab_id=? AND list_name=?;";

	private static final String SELECT_TABLETODO_BY_TABID = "SELECT table_number, waiter, open FROM tabs WHERE id=?;";

	private static final String SELECT_TABLETODO_BY_TABLENUMBER = "SELECT id, table_number, waiter, open FROM tabs WHERE table_number=? AND open=?;";

	private static final String SELECT_ITEM_TODO_BY_WAITER = "SELECT tt.table_number AS table_number, it.menu_number AS menu_number, it.description AS description, it.price AS price "
			+ "FROM ordereditem AS it, tabs AS tt "
			+ "WHERE it.tab_id=tt.id AND it.list_name LIKE \"%TO_SERVE\" AND tt.waiter=? AND tt.open=?;";

	private static final String SUM_PRICE_BY_TAB = "SELECT SUM(price) AS total FROM ordereditem WHERE tab_id=?;";

	private static final String SUM_PRICE_BY_TABLE_NUMBER = "SELECT SUM(price) AS total FROM ordereditem AS it, tabs AS tt WHERE tt.table_number=? AND tt.open=? AND it.tab_id=tt.id;";

	@Autowired
	private DataSource dataSource;

	@Override
	public void apply(Event event) {
		// DO NOTHING
	}

	private List<ItemTodo> getItems(PreparedStatement statement) throws SQLException {
		try (ResultSet results = statement.executeQuery();) {
			List<ItemTodo> items = new ArrayList<>();
			while (results.next()) {
				int menuNumber = results.getInt("menu_number");
				String description = results.getString("description");
				double price = results.getDouble("price");

				ItemTodo it = new ItemTodo(menuNumber, description, price);
				items.add(it);
			}
			return items;
		}
	}

	@Override
	public TableTodo getByTab(long idTab) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_TABLETODO_BY_TABID);
				PreparedStatement drinkToServeStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);
				PreparedStatement foodToServeStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);
				PreparedStatement inPrepStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);) {
			statement.setLong(1, idTab);

			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					int tableNumber = results.getInt("table_number");
					String waiter = results.getString("waiter");
					boolean open = results.getBoolean("open");

					drinkToServeStatement.setLong(1, idTab);
					drinkToServeStatement.setString(2, "DRINK_TO_SERVE");
					List<ItemTodo> drinkToServe = getItems(drinkToServeStatement);
					foodToServeStatement.setLong(1, idTab);
					foodToServeStatement.setString(2, "FOOD_TO_SERVE");
					List<ItemTodo> foodToServe = getItems(foodToServeStatement);
					inPrepStatement.setLong(1, idTab);
					inPrepStatement.setString(2, "IN_PREPARATION");
					List<ItemTodo> inPreparation = getItems(inPrepStatement);
					TableTodo tt = new TableTodo(idTab, tableNumber, waiter, drinkToServe, foodToServe, inPreparation);
					tt.setClosed(!open);
					return tt;
				} else {
					throw new SQLException("No match found.");
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TableTodo getOpenByTableNumber(int tableNumber) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_TABLETODO_BY_TABLENUMBER);
				PreparedStatement drinkToServeStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);
				PreparedStatement foodToServeStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);
				PreparedStatement inPrepStatement = connection.prepareStatement(SELECT_ITEM_TODO_BY_TABID);) {
			statement.setInt(1, tableNumber);
			statement.setBoolean(2, false);

			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					long idTab = results.getLong("id");
					String waiter = results.getString("waiter");
					boolean open = results.getBoolean("open");

					drinkToServeStatement.setLong(1, idTab);
					drinkToServeStatement.setString(2, "DRINK_TO_SERVE");
					List<ItemTodo> drinkToServe = getItems(drinkToServeStatement);
					foodToServeStatement.setLong(1, idTab);
					foodToServeStatement.setString(2, "FOOD_TO_SERVE");
					List<ItemTodo> foodToServe = getItems(foodToServeStatement);
					inPrepStatement.setLong(1, idTab);
					inPrepStatement.setString(2, "IN_PREPARATION");
					List<ItemTodo> inPreparation = getItems(inPrepStatement);
					TableTodo tt = new TableTodo(idTab, tableNumber, waiter, drinkToServe, foodToServe, inPreparation);
					tt.setClosed(!open);
					return tt;
				} else {
					throw new SQLException("No match found.");
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_ITEM_TODO_BY_WAITER);) {
			statement.setString(1, waiter);
			statement.setBoolean(2, true);
			Map<Integer, List<ItemTodo>> todo = new HashMap<>();
			try (ResultSet results = statement.executeQuery();) {
				List<ItemTodo> items = null;
				while (results.next()) {
					int tableNumber = results.getInt("table_number");
					items = todo.get(tableNumber);
					if (items == null) {
						items = new ArrayList<>();
						todo.put(tableNumber, items);
					}
					int menuNumber = results.getInt("menu_number");
					String description = results.getString("description");
					double price = results.getDouble("price");

					ItemTodo it = new ItemTodo(menuNumber, description, price);
					items.add(it);
				}
			}
			return todo;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Double getPriceToPayByTab(long idTab) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SUM_PRICE_BY_TAB);) {
			statement.setLong(1, idTab);
			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					Double total = results.getDouble("total");
					return total;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Double getPriceToPayByTableNumber(int tableNumber) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SUM_PRICE_BY_TABLE_NUMBER);) {
			statement.setInt(1, tableNumber);
			statement.setBoolean(2, false);
			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					Double total = results.getDouble("total");
					return total;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
