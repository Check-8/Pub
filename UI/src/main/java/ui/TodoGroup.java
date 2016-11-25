package ui;

import java.util.List;

public class TodoGroup {
	private long tab;
	private List<ItemTodo> items;

	public TodoGroup() {
	}

	public Long getTab() {
		return tab;
	}

	public void setTab(long tab) {
		this.tab = tab;
	}

	public List<ItemTodo> getItems() {
		return items;
	}

	public void setItems(List<ItemTodo> items) {
		this.items = items;
	}

}
