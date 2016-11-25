package openTabs.readModel;

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
public class OpenTabsMySql implements ApplyEvent, OpenTabsQueries, ReadModel {
	private static final String SELECT_ITEM_TODO_BY_TABID = "SELECT menu_number, description, price FROM itemtodo WHERE tab_id=? AND list_name=?;";

	private static final String SELECT_TABLETODO_BY_TABID = "SELECT table_number, waiter, amount_paid, to_pay, tip, closed FROM tabletodo WHERE tab_id=?;";

	private static final String SELECT_TABLETODO_BY_TABLENUMBER = "SELECT tab_id, table_number, waiter, amount_paid, to_pay, tip, closed FROM tabletodo WHERE table_number=? AND closed=?;";

	private static final String SELECT_ITEM_TODO_BY_WAITER = "SELECT tt.table_number AS table_number, it.menu_number AS menu_number, it.description AS description, it.price AS price "
			+ "FROM itemtodo AS it, tabletodo AS tt "
			+ "WHERE it.tab_id=tt.tab_id AND it.list_name LIKE \"%TO_SERVE\" AND tt.waiter=? AND tt.closed=?;";

	private static final String SUM_PRICE_BY_TAB = "SELECT SUM(price) AS total FROM itemtodo WHERE tab_id=?;";

	private static final String SUM_PRICE_BY_TABLE_NUMBER = "SELECT SUM(price) AS total FROM itemtodo AS it, tabletodo AS tt WHERE tt.table_number=? AND tt.closed=? AND it.tab_id=tt.tab_id;";

	@Autowired
	private DataSource dataSource;

	private final Map<Class<? extends Event>, ApplyEvent> applier;

	public OpenTabsMySql() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(TabOpened.class, new TabOpenedApplier());
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(DrinksOrdered.class, new DrinksOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		aTemp.put(FoodServed.class, new FoodServedApplier());
		aTemp.put(DrinksServed.class, new DrinksServedApplier());
		aTemp.put(TabClosed.class, new TabClosedApplier());
		applier = Collections.unmodifiableMap(aTemp);

	}

	private class TabOpenedApplier implements ApplyEvent<TabOpened> {
		private static final String INSERT_TABLETODO = "INSERT INTO tabletodo (tab_id, table_number, waiter, amount_paid, to_pay, tip, closed) "
				+ "VALUES (?,?,?,?,?,?,?);";

		@Override
		public void apply(TabOpened event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_TABLETODO);) {

				statement.setLong(1, event.getId());
				statement.setInt(2, event.getTableNumber());
				statement.setString(3, event.getWaiter());
				statement.setDouble(4, 0);
				statement.setDouble(5, 0);
				statement.setDouble(6, 0);
				statement.setBoolean(7, false);

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Insert failed");

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		private static final String INSERT_FOODORDERED = "INSERT INTO itemtodo (tab_id, list_name, menu_number, description, price) "
				+ "VALUES (?,?,?,?,?);";

		@Override
		public void apply(FoodOrdered event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_FOODORDERED);) {
				long id = event.getId();
				String listName = "TO_PREPARE";

				for (OrderedItem item : event.getItems()) {
					statement.setLong(1, id);
					statement.setString(2, listName);
					statement.setInt(3, item.getMenuNumber());
					statement.setString(4, item.getDescription());
					statement.setDouble(5, item.getPrice());

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {
		private static final String INSERT_DRINKSORDERED = "INSERT INTO itemtodo (tab_id, list_name, menu_number, description, price) "
				+ "VALUES (?,?,?,?,?);";

		@Override
		public void apply(DrinksOrdered event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_DRINKSORDERED);) {
				long id = event.getId();
				String listName = "DRINK_TO_SERVE";

				for (OrderedItem item : event.getItems()) {
					statement.setLong(1, id);
					statement.setString(2, listName);
					statement.setInt(3, item.getMenuNumber());
					statement.setString(4, item.getDescription());
					statement.setDouble(5, item.getPrice());

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		private static final String UPDATE_FOODPREPARED = "UPDATE itemtodo SET list_name=\"FOOD_TO_SERVE\""
				+ "WHERE tab_id=? AND menu_number=? AND list_name=\"TO_PREPARE\" LIMIT 1;";

		@Override
		public void apply(FoodPrepared event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_FOODPREPARED);) {
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

	private class FoodServedApplier implements ApplyEvent<FoodServed> {
		private static final String MARK_FOODSERVED = "UPDATE itemtodo SET list_name=\"SERVED\" "
				+ "WHERE tab_id=? AND menu_number=? AND list_name=\"FOOD_TO_SERVE\" LIMIT 1;";

		@Override
		public void apply(FoodServed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(MARK_FOODSERVED);) {
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

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {
		private static final String MARK_DRINKSSERVED = "UPDATE itemtodo SET list_name=\"SERVED\" "
				+ "WHERE tab_id=? AND menu_number=? AND list_name=\"DRINK_TO_SERVE\" LIMIT 1;";

		@Override
		public void apply(DrinksServed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(MARK_DRINKSSERVED);) {
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

	private class TabClosedApplier implements ApplyEvent<TabClosed> {
		private static final String UPDATE_TABCLOSED = "UPDATE tabletodo SET closed=?, amount_paid=?, to_pay=?, tip=? WHERE tab_id=? LIMIT 1;";

		@Override
		public void apply(TabClosed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_TABCLOSED);) {

				statement.setBoolean(1, true);
				statement.setDouble(2, event.getAumountPaid());
				statement.setDouble(3, event.getOrderValue());
				statement.setDouble(4, event.getTipValue());
				statement.setLong(5, event.getId());

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Insert failed");

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
					double amountPaid = results.getDouble("amount_paid");
					double toPay = results.getDouble("to_pay");
					double tip = results.getDouble("tip");
					boolean closed = results.getBoolean("closed");

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
					tt.setAmountPaid(amountPaid);
					tt.setToPay(toPay);
					tt.setTip(tip);
					tt.setClosed(closed);
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
					long idTab = results.getLong("tab_id");
					String waiter = results.getString("waiter");
					double amountPaid = results.getDouble("amount_paid");
					double toPay = results.getDouble("to_pay");
					double tip = results.getDouble("tip");
					boolean closed = results.getBoolean("closed");

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
					tt.setAmountPaid(amountPaid);
					tt.setToPay(toPay);
					tt.setTip(tip);
					tt.setClosed(closed);
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
			statement.setBoolean(2, false);
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
