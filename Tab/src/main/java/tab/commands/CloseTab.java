package tab.commands;

public class CloseTab implements Command {
	private long id;
	private double amountPaid;

	public CloseTab(long id, double amountPaid) {
		super();
		this.id = id;
		this.amountPaid = amountPaid;
	}

	@Override
	public long getId() {
		return id;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

}
