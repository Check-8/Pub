package chefTodo.events;

public class OrderedItem {
	private int menuNumber;
	private String description;
	private boolean isDrink;
	private double price;
	
	@SuppressWarnings("unused")
	private OrderedItem(){
	}

	public OrderedItem(int menuNumber, String description, boolean isDrink, double price) {
		super();
		this.menuNumber = menuNumber;
		this.description = description;
		this.isDrink = isDrink;
		this.price = price;
	}

	public int getMenuNumber() {
		return menuNumber;
	}

	public String getDescription() {
		return description;
	}

	public boolean isDrink() {
		return isDrink;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "OrderedItem [menuNumber=" + menuNumber + ", description=" + description + ", isDrink=" + isDrink
				+ ", price=" + price + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (isDrink ? 1231 : 1237);
		result = prime * result + menuNumber;
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OrderedItem))
			return false;
		OrderedItem other = (OrderedItem) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (isDrink != other.isDrink)
			return false;
		if (menuNumber != other.menuNumber)
			return false;
		if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
			return false;
		return true;
	}

}
