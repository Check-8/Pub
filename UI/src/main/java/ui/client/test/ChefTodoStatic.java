package ui.client.test;

import java.util.ArrayList;
import java.util.List;

import ui.ItemTodo;
import ui.TodoGroup;
import ui.client.ChefTodoInterface;

public class ChefTodoStatic implements ChefTodoInterface {

	private List<TodoGroup> list;

	public ChefTodoStatic() {
		list = new ArrayList<>();
		ItemTodo item = null;
		TodoGroup tg = null;
		List<ItemTodo> items = null;

		tg = new TodoGroup();
		items = new ArrayList<>();
		item = new ItemTodo();
		tg.setTab(0);
		item.setDescription("pasta");
		item.setMenuNumber(2);
		item.setPrice(25);
		items.add(item);
		item = new ItemTodo();
		item.setDescription("meat");
		item.setMenuNumber(3);
		item.setPrice(35);
		items.add(item);
		tg.setItems(items);
		list.add(tg);

		tg = new TodoGroup();
		items = new ArrayList<>();
		item = new ItemTodo();
		tg.setTab(1);
		item.setDescription("meatball");
		item.setMenuNumber(4);
		item.setPrice(15);
		items.add(item);
		item = new ItemTodo();
		item.setDescription("chips");
		item.setMenuNumber(5);
		item.setPrice(10);
		items.add(item);
		tg.setItems(items);
		list.add(tg);

	}

	@Override
	public List<TodoGroup> getTodoList() {
		return list;
	}

}
