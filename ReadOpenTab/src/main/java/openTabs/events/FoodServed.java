package openTabs.events;

import java.util.ArrayList;
import java.util.List;

public class FoodServed implements Event {
	private static final String EVENT_NAME = "FOOD_SERVED";
	
	private long id;
	private List<Integer> menuItems;
	
	@SuppressWarnings("unused")
	private FoodServed() {
	}

	public FoodServed(long id, List<Integer> menuItems) {
		super();
		this.id = id;
		this.menuItems = new ArrayList<>(menuItems);
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	public List<Integer> getMenuItems() {
		return menuItems;
	}

	@Override
	public String toString() {
		return "DrinksServed [id=" + id + ", menuItems=" + menuItems + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((menuItems == null) ? 0 : menuItems.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FoodServed))
			return false;
		FoodServed other = (FoodServed) obj;
		if (id != other.id)
			return false;
		if (menuItems == null) {
			if (other.menuItems != null)
				return false;
		} else if (!menuItems.equals(other.menuItems))
			return false;
		return true;
	}

}
