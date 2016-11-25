package openTabs.readModel;

public class ItemTodo {
	private int menuNumber;
	private String description;
	private double price;

	public ItemTodo(int menuNumber, String description, double price) {
		super();
		this.menuNumber = menuNumber;
		this.description = description;
		this.price = price;
	}

	public int getMenuNumber() {
		return menuNumber;
	}

	public String getDescription() {
		return description;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "ItemTodo [menuNumber=" + menuNumber + ", description=" + description + ", price=" + price + "]";
	}

}
