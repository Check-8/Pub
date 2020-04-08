package menu.controller;

import menu.OrderedItem;
import menu.service.MenuLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MenuController {
	private Logger logger = LoggerFactory.getLogger(MenuController.class);

	@Autowired
	@Qualifier("menuLoader")
	private MenuLoader menu;

	@GetMapping(value = "/{menu_number}")
	public @ResponseBody OrderedItem getMenuItem(@PathVariable(value = "menu_number") int menuNumber) {
		OrderedItem item = menu.getMenu().get(menuNumber);
		logger.info("Menu Item: {} {}", menuNumber, item);
		return item;
	}

	@GetMapping(value = "/menu")
	public @ResponseBody List<OrderedItem> getMenu() {
		List<OrderedItem> list = new ArrayList<>(menu.getMenu().values());
		logger.info("Menu: {}", list);
		return list;
	}

	@PostMapping(value = "/reload")
	public @ResponseBody void reloadMenu() {
		logger.info("Reload");
		menu.init();
	}
}
