package menu.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import menu.OrderedItem;
import menu.loader.MenuLoader;

@Controller
public class MenuController {
	private Logger logger = LoggerFactory.getLogger(MenuController.class);

	@Autowired
	@Qualifier("menuLoader")
	private MenuLoader menu;

	@RequestMapping(value = "/{menu_number}", method = RequestMethod.GET)
	public @ResponseBody OrderedItem getMenuItem(@PathVariable(value = "menu_number") int menuNumber) {
		OrderedItem item = menu.getMenu().get(menuNumber);
		logger.info("Menu Item: " + menuNumber + " " + item);
		return item;
	}

	@RequestMapping(value = "/menu", method = RequestMethod.GET)
	public @ResponseBody List<OrderedItem> getMenu() {
		List<OrderedItem> list = new ArrayList<>(menu.getMenu().values());
		logger.info("Menu: " + list);
		return list;
	}

	@RequestMapping(value = "/reload", method = RequestMethod.POST)
	public @ResponseBody void reloadMenu() {
		logger.info("Reload");
		menu.init();
	}
}
