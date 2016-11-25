package tab.commands;

public class OpenTab implements Command {
	private long id;
	private int tableNumber;
	private String waiter;

	public OpenTab(long id, int tableNumber, String waiter) {
		super();
		this.id = id;
		this.tableNumber = tableNumber;
		this.waiter = waiter;
	}

	@Override
	public long getId() {
		return id;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public String getWaiter() {
		return waiter;
	}

}
