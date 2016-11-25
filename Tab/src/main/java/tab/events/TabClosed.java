package tab.events;

public class TabClosed implements Event {
	private static final String EVENT_NAME = "TAB_CLOSED";
	
	private long id;
	private double aumountPaid;
	private double orderValue;
	private double tipValue;
	
	@SuppressWarnings("unused")
	private TabClosed() {
	}

	public TabClosed(long id, double aumountPaid, double orderValue, double tipValue) {
		super();
		this.id = id;
		this.aumountPaid = aumountPaid;
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

	public double getAumountPaid() {
		return aumountPaid;
	}

	public double getOrderValue() {
		return orderValue;
	}

	public double getTipValue() {
		return tipValue;
	}

	@Override
	public String toString() {
		return "TabClosed [id=" + id + ", aumountPaid=" + aumountPaid + ", orderValue=" + orderValue + ", tipValue="
				+ tipValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(aumountPaid);
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
		if (Double.doubleToLongBits(aumountPaid) != Double.doubleToLongBits(other.aumountPaid))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(orderValue) != Double.doubleToLongBits(other.orderValue))
			return false;
		if (Double.doubleToLongBits(tipValue) != Double.doubleToLongBits(other.tipValue))
			return false;
		return true;
	}

}
