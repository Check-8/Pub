package ui.client;

import java.util.List;
import java.util.Map;

import ui.ItemTodo;
import ui.TableTodo;

public interface OpenTabInteface {
	public TableTodo getByTab(long id);

	public TableTodo getByTableNumber(int tableNumber);

	public Map<Integer, List<ItemTodo>> getTodoListByWaiter(String waiter);

	public Double getToPayByTableNumber(int tableNumber);
}
