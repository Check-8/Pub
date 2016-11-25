package tab.events;

public class TabOpened implements Event {
	private static final String EVENT_NAME = "TAB_OPENED";

	private long id;
	private int tableNumber;
	private String waiter;

	@SuppressWarnings("unused")
	private TabOpened() {
	}

	public TabOpened(long id, int tableNumber, String waiter) {
		super();
		this.id = id;
		this.tableNumber = tableNumber;
		this.waiter = waiter;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public String getWaiter() {
		return waiter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + tableNumber;
		result = prime * result + ((waiter == null) ? 0 : waiter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TabOpened))
			return false;
		TabOpened other = (TabOpened) obj;
		if (id != other.id)
			return false;
		if (tableNumber != other.tableNumber)
			return false;
		if (waiter == null) {
			if (other.waiter != null)
				return false;
		} else if (!waiter.equals(other.waiter))
			return false;
		return true;
	}

}
