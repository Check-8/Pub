package ui.client;

public interface TabsInterface {
	public void openTab(int tableNumber, String waiter);

	public void closeTab(long id, double amountPaid);

	public void placeOrder(long id, int[] orderedItem);

	public void markDrinksServed(long id, int[] drinksServed);

	public void markFoodPrepared(long id, int[] foodPrepared);

	public void markFoodServed(long id, int[] foodServed);
}
