package ui.client.test;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ui.client.TabsInterface;

public class TabsStatic implements TabsInterface {
	private Logger logger = LoggerFactory.getLogger(TabsStatic.class);

	@Override
	public void openTab(int tableNumber, String waiter) {
		logger.info("OPEN TAB: " + tableNumber + " " + waiter);
	}

	@Override
	public void closeTab(long id, double amount_paid) {
		logger.info("CLOSE TAB: " + id + " " + amount_paid);
	}

	@Override
	public void placeOrder(long id, int[] orderedItem) {
		logger.info("PLACE ORDER: " + id + " " + Arrays.toString(orderedItem));
	}

	@Override
	public void markDrinksServed(long id, int[] drinksServed) {
		logger.info("DRINKS SERVED: " + id + " " + Arrays.toString(drinksServed));
	}

	@Override
	public void markFoodPrepared(long id, int[] foodPrepared) {
		logger.info("FOOD PREPARED: " + id + " " + Arrays.toString(foodPrepared));
	}

	@Override
	public void markFoodServed(long id, int[] foodServed) {
		logger.info("FOOD SERVED: " + id + " " + Arrays.toString(foodServed));
	}

}
