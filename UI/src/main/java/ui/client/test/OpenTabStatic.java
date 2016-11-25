package ui.client.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ui.ItemTodo;
import ui.TableTodo;
import ui.client.OpenTabInteface;

public class OpenTabStatic implements OpenTabInteface {
	private TableTodo tt;

	public OpenTabStatic() {
		ItemTodo item0, item1, item2, item3, drink1, drink2;
		tt = new TableTodo();
		tt.setAmountPaid(0);
		tt.setClosed(false);
		item0 = new ItemTodo();
		item0.setDescription("pasta");
		item0.setMenuNumber(2);
		item0.setPrice(25);
		item1 = new ItemTodo();
		item1.setDescription("meat");
		item1.setMenuNumber(3);
		item1.setPrice(35);
		item2 = new ItemTodo();
		item2.setDescription("meatball");
		item2.setMenuNumber(4);
		item2.setPrice(15);
		item3 = new ItemTodo();
		item3.setDescription("chips");
		item3.setMenuNumber(5);
		item3.setPrice(10);
		tt.setInPreparation(Arrays.asList(item0, item1, item2, item3));
		tt.setTableNumber(0);
		tt.setTip(0);
		tt.setToPay(0);
		drink1 = new ItemTodo();
		drink1.setDescription("coke");
		drink1.setMenuNumber(1);
		drink1.setPrice(5);
		drink2 = new ItemTodo();
		drink2.setDescription("wine");
		drink2.setMenuNumber(30);
		drink2.setPrice(6);
		tt.setDrinkToServe(Arrays.asList(drink1, drink2));
		tt.setWaiter("Jack");

	}

	@Override
	public TableTodo getByTab(long id) {
		if (id == 0)
			return tt;
		return null;
	}

	@Override
	public TableTodo getByTableNumber(int tableNumber) {
		if (tt.getTableNumber() == tableNumber)
			return tt;
		return null;
	}

	@Override
	public Map<Integer, List<ItemTodo>> getTodoListByWaiter(String waiter) {
		if (tt.getWaiter().equals(waiter)) {
			Map<Integer, List<ItemTodo>> map = new HashMap<>();
			map.put(tt.getTableNumber(), tt.getToServe());
			return map;
		}
		return null;
	}

	@Override
	public Double getToPayByTableNumber(int tableNumber) {
		return null;
	}

}
