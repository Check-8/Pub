package tab.commands;

import java.util.ArrayList;
import java.util.List;

public class MarkFoodServed implements Command {
	private long id;
	private List<Integer> menuItems;

	public MarkFoodServed(long id, List<Integer> menuItems) {
		super();
		this.id = id;
		this.menuItems = new ArrayList<>(menuItems);
	}

	@Override
	public long getId() {
		return id;
	}

	public List<Integer> getMenuItems() {
		return menuItems;
	}

}
