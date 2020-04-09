package tab.commands;

import java.util.List;

import tab.events.OrderedItem;
import tab.model.Order;

public class PlaceOrder implements Command {
	private long id;
	private List<OrderedItem> items;

	public PlaceOrder(long id, List<OrderedItem> items) {
		super();
		this.id = id;
		this.items = items;
	}

	@Override
	public long getId() {
		return id;
	}

	public List<OrderedItem> getItems() {
		return items;
	}

}
