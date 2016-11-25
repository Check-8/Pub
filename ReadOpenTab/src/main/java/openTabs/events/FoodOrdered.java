package openTabs.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class FoodOrdered implements Event {
	private static final String EVENT_NAME = "FOOD_ORDERED";
	
	private long id;
	private List<OrderedItem> items;

	@SuppressWarnings("unused")
	private FoodOrdered() {
	}
	
	public FoodOrdered(long id, List<OrderedItem> items) {
		super();
		this.id = id;
		this.items = new ArrayList<>(items);
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	public List<OrderedItem> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return "FoodOrdered [id=" + id + ", items=" + items + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FoodOrdered))
			return false;
		FoodOrdered other = (FoodOrdered) obj;
		if (id != other.id)
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!CollectionUtils.isEqualCollection(items, other.items))
			return false;
		return true;
	}

}
