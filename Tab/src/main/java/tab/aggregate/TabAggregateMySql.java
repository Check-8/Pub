package tab.aggregate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tab.aggregate.ApplyEvent;
import tab.aggregate.CommandHandler;
import tab.aggregate.TabAggregate;
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

public class TabAggregateMySql implements TabAggregate {
	private static final String SELECT_OPEN = "SELECT open FROM tabs WHERE id=?;";

	private static final String SELECT_ITEM_FROM_LIST = "SELECT menu_number FROM ordereditem WHERE tab_id=? AND is_drink=? AND list_type=?;";

	private static final String COUNT_NOT_SERVED = "SELECT COUNT(*) AS not_served FROM ordereditem WHERE tab_id=? AND list_type<>\"SERVED\";";

	private Logger logger = LoggerFactory.getLogger(TabAggregate.class);

	private DataSource dataSource;

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Command>, CommandHandler> handlers;
	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Event>, ApplyEvent> applier;

	@SuppressWarnings("rawtypes")
	public TabAggregateMySql() {
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

	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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
		private static final String SELECT_TAB_VALUE = "SELECT price FROM ordereditem WHERE tab_id=?;";

		private double getServedValue(long id) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(SELECT_TAB_VALUE);) {
				statement.setLong(1, id);
				double value = 0;
				try (ResultSet results = statement.executeQuery();) {
					while (results.next()) {
						double temp = results.getDouble("price");
						value += temp;
					}
				}
				return value;

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
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
		private static final String INSERT_NEW_TAB = "INSERT INTO tabs (id, table_number, open, served_values) VALUES (?,?,?,?);";

		@Override
		public void apply(TabOpened event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_NEW_TAB);) {

				statement.setLong(1, event.getId());
				statement.setInt(2, event.getTableNumber());
				statement.setBoolean(3, true);
				statement.setDouble(4, 0);

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Insert failed");

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private class DrinksOrderedApplier implements ApplyEvent<DrinksOrdered> {
		private static final String INSERT_DRINKS = "INSERT INTO ordereditem (tab_id, menu_number, description, is_drink, price, list_type) VALUES (?,?,?,?,?,?);";

		@Override
		public void apply(DrinksOrdered event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_DRINKS);) {
				long id = event.getId();

				for (OrderedItem item : event.getItems()) {
					logger.info(item.toString());
					statement.setLong(1, id);
					statement.setInt(2, item.getMenuNumber());
					statement.setString(3, item.getDescription());
					statement.setBoolean(4, item.isDrink());
					statement.setDouble(5, item.getPrice());
					statement.setString(6, "TO_SERVE");

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class DrinksServedApplier implements ApplyEvent<DrinksServed> {
		private static final String UPDATE_DRINKS_SERVED = "UPDATE ordereditem SET list_type=\"SERVED\" WHERE tab_id=? AND menu_number=? AND list_type=\"TO_SERVE\" LIMIT 1;";

		@Override
		public void apply(DrinksServed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_DRINKS_SERVED);) {
				long id = event.getId();

				for (Integer item : event.getMenuItems()) {
					statement.setLong(1, id);
					statement.setInt(2, item);

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		private static final String INSERT_FOOD = "INSERT INTO ordereditem (tab_id, menu_number, description, is_drink, price, list_type) VALUES (?,?,?,?,?,?);";

		@Override
		public void apply(FoodOrdered event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(INSERT_FOOD);) {
				long id = event.getId();

				for (OrderedItem item : event.getItems()) {
					statement.setLong(1, id);
					statement.setInt(2, item.getMenuNumber());
					statement.setString(3, item.getDescription());
					statement.setBoolean(4, item.isDrink());
					statement.setDouble(5, item.getPrice());
					statement.setString(6, "TO_PREPARE");

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		private static final String UPDATE_FOOD_PREPARED = "UPDATE ordereditem SET list_type=\"TO_SERVE\" WHERE tab_id=? AND menu_number=? AND list_type=\"TO_PREPARE\" LIMIT 1;";

		@Override
		public void apply(FoodPrepared event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_FOOD_PREPARED);) {
				long id = event.getId();

				for (Integer item : event.getMenuItems()) {
					statement.setLong(1, id);
					statement.setInt(2, item);

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class FoodServedApplier implements ApplyEvent<FoodServed> {
		private static final String UPDATE_FOOD_SERVED = "UPDATE ordereditem SET list_type=\"SERVED\" WHERE tab_id=? AND menu_number=? AND list_type=\"TO_SERVE\" LIMIT 1;";

		@Override
		public void apply(FoodServed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_FOOD_SERVED);) {
				long id = event.getId();

				for (Integer item : event.getMenuItems()) {
					statement.setLong(1, id);
					statement.setInt(2, item);

					statement.addBatch();
				}
				statement.executeBatch();

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class TabClosedApplier implements ApplyEvent<TabClosed> {
		private static final String UPDATE_TAB_CLOSED = "UPDATE tabs SET open=?, served_values=? WHERE id=? LIMIT 1;";

		@Override
		public void apply(TabClosed event) {
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(UPDATE_TAB_CLOSED);) {

				statement.setBoolean(1, false);
				statement.setDouble(2, event.getOrderValue());
				statement.setLong(3, event.getId());

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Update failed");

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
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

	private List<Integer> getItemFromList(long id, boolean isDrink, String listType) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_ITEM_FROM_LIST);) {
			statement.setLong(1, id);
			statement.setBoolean(2, isDrink);
			statement.setString(3, listType);
			List<Integer> toServe = new ArrayList<>();
			try (ResultSet results = statement.executeQuery();) {
				while (results.next()) {
					int temp = results.getInt("menu_number");
					toServe.add(temp);
				}
			}
			return toServe;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static final String COUNT_OPEN_TAB_FOR_TABLE = "SELECT COUNT(*) AS n_tab FROM tabs WHERE table_number=? AND open=?;";

	private boolean isTabOpenForTable(int tableNumber) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(COUNT_OPEN_TAB_FOR_TABLE);) {
			statement.setInt(1, tableNumber);
			statement.setBoolean(2, true);
			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					int temp = results.getInt("n_tab");
					logger.info("FOUND OPEN FOR TABLE: " + tableNumber + " n°: " + temp);
					if (temp == 1)
						return true;
				}
				return false;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean areDrinksOutstanding(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, true, "TO_SERVE");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodOutstanding(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, false, "TO_PREPARE");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean areFoodPrepared(long tabId, List<Integer> menuNumbers) {
		List<Integer> curOutstanding = null;
		curOutstanding = getItemFromList(tabId, false, "TO_SERVE");
		for (Integer item : menuNumbers)
			if (!curOutstanding.remove(item))
				return false;
		return true;
	}

	private boolean isEverythingServed(long tabId) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(COUNT_NOT_SERVED);) {
			statement.setLong(1, tabId);
			try (ResultSet results = statement.executeQuery();) {
				if (results.next()) {
					int temp = results.getInt("not_served");
					if (temp != 0)
						return false;
				}
				return true;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isOpen(long id) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_OPEN);) {
			statement.setLong(1, id);

			try (ResultSet results = statement.executeQuery();) {
				while (results.next()) {
					if (results.getBoolean("open"))
						return true;
				}
				return false;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}