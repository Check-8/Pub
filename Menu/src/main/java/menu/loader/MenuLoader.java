package menu.loader;

import java.util.Map;

import menu.OrderedItem;

public interface MenuLoader {

	public void init();

	public Map<Integer, OrderedItem> getMenu();
}
