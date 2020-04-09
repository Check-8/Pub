package openTabs.readModel;

import java.util.List;
import java.util.Map;

public interface OpenTabsQueries {
    TableTodo getByTab(long idTab);

    List<TableTodo> getByParams(QueryParams params);

    TableTodo getOpenByTableNumber(int tableNumber);

    Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter);

    Double getPriceToPayByTab(long idTab);

    Double getPriceToPayByTableNumber(int tableNumber);
}
