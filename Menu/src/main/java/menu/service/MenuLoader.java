package menu.service;

import java.util.Map;

import menu.OrderedItem;

public interface MenuLoader {

	void init();

	Map<Integer, OrderedItem> getMenu();
}
