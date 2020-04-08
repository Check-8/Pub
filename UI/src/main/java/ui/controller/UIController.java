package ui.controller;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ui.ItemTodo;
import ui.TableTodo;
import ui.client.ChefTodoInterface;
import ui.client.MenuClient;
import ui.client.OpenTabInteface;
import ui.client.TabsInterface;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Controller
public class UIController {

	private static final Logger logger = LoggerFactory.getLogger(UIController.class);

	@Autowired
	@Qualifier("tabsClient")
	private TabsInterface tabs;

	@Autowired
	@Qualifier("chefClient")
	private ChefTodoInterface chef;

	@Autowired
	@Qualifier("openClient")
	private OpenTabInteface open;

	@Autowired
	@Qualifier("menuClient")
	private MenuClient menu;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView commandMenu() {
		return new ModelAndView("commandMenu");
	}

	@RequestMapping(value = "/opentab/", method = RequestMethod.GET)
	public ModelAndView openTabForm() {
		return new ModelAndView("opentabForm");
	}

	@RequestMapping(value = "/opentab/", method = RequestMethod.POST)
	public ModelAndView openTabForm(@RequestParam(name = "table_number") int tableNumber,
			@RequestParam(name = "waiter") String waiter) {
		tabs.openTab(tableNumber, waiter);
		return new ModelAndView("opentabForm");
	}

	@RequestMapping(value = "/close/", method = RequestMethod.GET)
	public ModelAndView chooseTableForClose() {
		ModelAndView mav = new ModelAndView("choosetable");
		mav.addObject("type", "close");
		return mav;
	}

	@RequestMapping(value = "/closetab/", method = RequestMethod.GET)
	public ModelAndView closeTabForm(@RequestParam(name = "table_number") int tableNumber) {
		ModelAndView mav = new ModelAndView("closetabForm");
		TableTodo tt = open.getByTableNumber(tableNumber);
		Double toPay = open.getToPayByTableNumber(tableNumber);
		mav.addObject("tt", tt);
		mav.addObject("toPay", toPay);
		return mav;
	}

	@RequestMapping(value = "/closetab/", method = RequestMethod.POST)
	public ModelAndView closeTabForm(@RequestParam(name = "id") long id,
			@RequestParam(name = "table_number") int tableNumber,
			@RequestParam(name = "amount_paid") double amountPaid) {
		tabs.closeTab(id, amountPaid);
		return closeTabForm(tableNumber);
	}

	@RequestMapping(value = "/markfood/", method = RequestMethod.GET)
	public ModelAndView chooseTableFood() {
		ModelAndView mav = new ModelAndView("choosetable");
		mav.addObject("type", "food");
		return mav;
	}

	@RequestMapping(value = "/markfoodserved/", method = RequestMethod.GET)
	public ModelAndView markFoodServedForm(@RequestParam(name = "table_number") int tableNumber) {
		ModelAndView mav = new ModelAndView("markfoodservedForm");
		TableTodo tt = open.getByTableNumber(tableNumber);
		mav.addObject("tt", tt);
		mav.addObject("food", tt.getFoodToServe());
		return mav;
	}

	@RequestMapping(value = "/markfoodserved/", method = RequestMethod.POST)
	public ModelAndView markFoodServedForm(@RequestParam(name = "id") long tabId,
			@RequestParam(name = "table_number") int tableNumber,
			@RequestParam(name = "food_served[]") int[] foodServed) {
		tabs.markFoodServed(tabId, foodServed);
		return markFoodServedForm(tableNumber);
	}

	@RequestMapping(value = "/markfoodprepared/", method = RequestMethod.GET)
	public ModelAndView markFoodPreparedForm() {
		ModelAndView mav = new ModelAndView("markfoodpreparedForm");
		mav.addObject("groups", chef.getTodoList());
		return mav;
	}

	@RequestMapping(value = "/markfoodprepared/", method = RequestMethod.POST)
	public ModelAndView markFoodPreparedForm(@RequestParam(name = "id") long tabId,
			@RequestParam(name = "food_prepared[]") int[] foodPrepared) {
		tabs.markFoodPrepared(tabId, foodPrepared);
		ModelAndView mav = new ModelAndView("markfoodpreparedForm");
		mav.addObject("groups", chef.getTodoList());
		return mav;
	}

	@RequestMapping(value = "/markdrinks/", method = RequestMethod.GET)
	public ModelAndView chooseTableDrinks() {
		ModelAndView mav = new ModelAndView("choosetable");
		mav.addObject("type", "drink");
		return mav;
	}

	@RequestMapping(value = "/markdrinksserved/", method = RequestMethod.GET)
	public ModelAndView markDrinksServedForm(@RequestParam(name = "table_number") int tableNumber) {
		ModelAndView mav = new ModelAndView("markdrinksservedForm");
		TableTodo tt = open.getByTableNumber(tableNumber);
		mav.addObject("tt", tt);
		mav.addObject("drinks", tt.getDrinkToServe());
		logger.info(tt.toString());
		return mav;
	}

	@RequestMapping(value = "/markdrinksserved/", method = RequestMethod.POST)
	public ModelAndView markDrinksServedForm(@RequestParam(name = "id") long tabId,
			@RequestParam(name = "table_number") int tableNumber,
			@RequestParam(name = "drinks_served[]") int[] drinksServed) {
		tabs.markDrinksServed(tabId, drinksServed);
		return markDrinksServedForm(tableNumber);
	}

	@RequestMapping(value = "/waiter/", method = RequestMethod.GET)
	public ModelAndView chooseWaiter() {
		return new ModelAndView("choosewaiter");
	}

	@RequestMapping(value = "/toserve/", method = RequestMethod.GET)
	public ModelAndView toServeByWaiter(@RequestParam(name = "waiter") String waiter) {
		ModelAndView mav = new ModelAndView("toservebywaiter");
		Map<Integer, List<ItemTodo>> map = open.getTodoListByWaiter(waiter);
		mav.addObject("items", map);
		mav.addObject("waiter", waiter);
		return mav;
	}

	@RequestMapping(value = "/order/", method = RequestMethod.GET)
	public ModelAndView chooseTableOrder() {
		ModelAndView mav = new ModelAndView("choosetable");
		mav.addObject("type", "order");
		return mav;
	}

	@RequestMapping(value = "/placeorder/", method = RequestMethod.GET)
	public ModelAndView orderItemForm(@RequestParam(name = "table_number") int tableNumber) {
		ModelAndView mav = new ModelAndView("orderitemForm");
		TableTodo tt = open.getByTableNumber(tableNumber);
		mav.addObject("tt", tt);
		mav.addObject("menu", menu.getMenu());
		return mav;
	}

	@RequestMapping(value = "/placeorder/", method = RequestMethod.POST)
	public ModelAndView orderItemForm(HttpServletRequest request) {
		Map<String, String[]> param = request.getParameterMap();
		String idString = request.getParameter("id");
		if (idString == null)
			throw new NullPointerException("ID can't be null");
		long id = Long.parseLong(idString);

		String tableNumberString = request.getParameter("table_number");
		if (tableNumberString == null)
			throw new NullArgumentException("table_number can't be null");
		int tableNumber = Integer.parseInt(tableNumberString);

		List<Integer> list = new ArrayList<>();
		for (Entry<String, String[]> entry : param.entrySet()) {
			if (entry.getKey().startsWith("quantity_")) {
				Integer menuNumber = Integer.parseInt(entry.getKey().substring(9).trim());
				int times = Integer.parseInt(entry.getValue()[0]);
				list.addAll(Collections.nCopies(times, menuNumber));
			}
		}
		logger.info("ORDERED ITEM: " + list);

		int[] orderedItem = list.stream().mapToInt(Integer::intValue).toArray();

		tabs.placeOrder(id, orderedItem);

		return orderItemForm(tableNumber);
	}

}
