package openTabs.readModel;

import java.util.List;
import java.util.Map;

public interface OpenTabsQueries {
	public TableTodo getByTab(long idTab);

	public TableTodo getOpenByTableNumber(int tableNumber);

	public Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter);

	public Double getPriceToPayByTab(long idTab);
	
	public Double getPriceToPayByTableNumber(int tableNumber);
}
