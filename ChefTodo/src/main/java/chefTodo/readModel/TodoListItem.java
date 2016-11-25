package chefTodo.readModel;

public class TodoListItem {
	private int menuNumber;
	private String description;

	public TodoListItem(int menuNumber, String description) {
		super();
		this.menuNumber = menuNumber;
		this.description = description;
	}

	public int getMenuNumber() {
		return menuNumber;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "TodoListItem [menuNumber=" + menuNumber + ", description=" + description + "]";
	}

}
