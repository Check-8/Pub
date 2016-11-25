package chefTodo.readModel;

import java.util.List;

public class TodoListGroup {
	private long tab;
	private List<TodoListItem> items;

	public TodoListGroup(long tab, List<TodoListItem> items) {
		super();
		this.tab = tab;
		this.items = items;
	}

	public long getTab() {
		return tab;
	}

	public List<TodoListItem> getItems() {
		return items;
	}

	public boolean removeByMenuNumber(int menuNumber) {
		return items.remove(items.stream().filter(i -> i.getMenuNumber() == menuNumber).findFirst().get());
	}

	@Override
	public String toString() {
		return "TodoListGroup [tab=" + tab + ", items=" + items + "]";
	}

}
