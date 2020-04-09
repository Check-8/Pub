package openTabs.events;

public class TabClosed implements Event {
	private static final String EVENT_NAME = "TAB_CLOSED";
	
	private long id;
	private double amountPaid;
	private double orderValue;
	private double tipValue;
	
	private TabClosed() {
	}

	public TabClosed(long id, double amountPaid, double orderValue, double tipValue) {
		super();
		this.id = id;
		this.amountPaid = amountPaid;
		this.orderValue = orderValue;
		this.tipValue = tipValue;
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

	public double getOrderValue() {
		return orderValue;
	}

	public double getTipValue() {
		return tipValue;
	}

	@Override
	public String toString() {
		return "TabClosed [id=" + id + ", amountPaid=" + amountPaid + ", orderValue=" + orderValue + ", tipValue="
				+ tipValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amountPaid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (id ^ (id >>> 32));
		temp = Double.doubleToLongBits(orderValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(tipValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TabClosed))
			return false;
		TabClosed other = (TabClosed) obj;
		if (Double.doubleToLongBits(amountPaid) != Double.doubleToLongBits(other.amountPaid))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(orderValue) != Double.doubleToLongBits(other.orderValue))
			return false;
		return Double.doubleToLongBits(tipValue) == Double.doubleToLongBits(other.tipValue);
	}

}
