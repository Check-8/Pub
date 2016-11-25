package ui;

public class ItemTodo {
	private int menuNumber;
	private String description;
	private double price;

	public ItemTodo() {
	}

	public int getMenuNumber() {
		return menuNumber;
	}

	public void setMenuNumber(int menuNumber) {
		this.menuNumber = menuNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "ItemTodo [menuNumber=" + menuNumber + ", description=" + description + ", price=" + price + "]";
	}

}
