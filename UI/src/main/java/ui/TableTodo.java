package ui;

import java.util.ArrayList;
import java.util.List;

public class TableTodo {
	private long tabId;
	private int tableNumber;
	private String waiter;
	private List<ItemTodo> drinkToServe;
	private List<ItemTodo> foodToServe;
	private List<ItemTodo> inPreparation;

	private double amountPaid;
	private double toPay;
	private double tip;
	private boolean closed;

	public TableTodo() {
	}

	public long getTabId() {
		return tabId;
	}

	public void setTabId(long tabId) {
		this.tabId = tabId;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public String getWaiter() {
		return waiter;
	}

	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}

	public List<ItemTodo> getDrinkToServe() {
		return drinkToServe;
	}

	public void setDrinkToServe(List<ItemTodo> toServe) {
		this.drinkToServe = toServe;
	}

	public List<ItemTodo> getFoodToServe() {
		return foodToServe;
	}

	public void setFoodToServe(List<ItemTodo> toServe) {
		this.foodToServe = toServe;
	}

	public List<ItemTodo> getToServe() {
		if (drinkToServe != null && foodToServe != null) {
			List<ItemTodo> temp = null;
			temp = new ArrayList<>(drinkToServe);
			temp.addAll(foodToServe);
			return temp;
		} else if (drinkToServe == null)
			return new ArrayList<>(foodToServe);
		else if (foodToServe == null)
			return new ArrayList<>(drinkToServe);
		else if (foodToServe == null && drinkToServe == null)
			return new ArrayList<>();
		return null;
	}

	public List<ItemTodo> getInPreparation() {
		return inPreparation;
	}

	public void setInPreparation(List<ItemTodo> inPreparation) {
		this.inPreparation = inPreparation;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(double amountPaid) {
		this.amountPaid = amountPaid;
	}

	public double getToPay() {
		return toPay;
	}

	public void setToPay(double toPay) {
		this.toPay = toPay;
	}

	public double getTip() {
		return tip;
	}

	public void setTip(double tip) {
		this.tip = tip;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	@Override
	public String toString() {
		return "TableTodo [tabId=" + tabId + ", tableNumber=" + tableNumber + ", waiter=" + waiter + ", drinkToServe="
				+ drinkToServe + ", foodToServe=" + foodToServe + ", inPreparation=" + inPreparation + ", amountPaid="
				+ amountPaid + ", toPay=" + toPay + ", tip=" + tip + ", closed=" + closed + "]";
	}

}
