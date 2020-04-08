package tab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tab.MessageDispatcher;
import tab.client.MenuClient;
import tab.commands.CloseTab;
import tab.commands.MarkDrinksServed;
import tab.commands.MarkFoodPrepared;
import tab.commands.MarkFoodServed;
import tab.commands.OpenTab;
import tab.commands.PlaceOrder;
import tab.events.OrderedItem;
import tab.exception.TabException;
import tab.exception.TabNotOpen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TabController {
	private static final Logger logger = LoggerFactory.getLogger(TabController.class);

	@Autowired
	@Qualifier("messageDispatcher")
	private MessageDispatcher md;

	@Autowired
	@Qualifier("menuClient")
	private MenuClient menu;

	@PostMapping(value = "/opentab")
	public @ResponseBody void openTab(@RequestParam(value = "id", required = false) Long id,
			@RequestParam("table_number") int tableNumber, @RequestParam("waiter") String waiter) {
		logger.debug("id: {} table_number: {} waiter: {}", id, tableNumber, waiter);
		if (id == null)
			id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
		md.receiveCommand(new OpenTab(id, tableNumber, waiter));
	}

	@PostMapping(value = "/opentab", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void openTab(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		Long id = null;
		if (temp != null)
			id = temp.longValue();
		int tableNumber = (Integer) param.get("table_number");
		String waiter = (String) param.get("waiter");
		openTab(id, tableNumber, waiter);
	}

	@PostMapping(value = "/closetab")
	public @ResponseBody void closeTab(@RequestParam("id") long id, @RequestParam("amount_paid") double amountPaid) {
		md.receiveCommand(new CloseTab(id, amountPaid));
	}

	@PostMapping(value = "/closetab", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void closeTab(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		long id = temp.longValue();
		double amountPaid = (Double) param.get("amount_paid");
		closeTab(id, amountPaid);
	}

	@PostMapping(value = "/placeorder")
	public @ResponseBody void placeOrder(@RequestParam("id") long id,
			@RequestParam("ordered_item[]") Integer[] orderedItem) {
		List<OrderedItem> list = new ArrayList<>();
		for (Integer menuNumber : orderedItem) {
			list.add(menu.getMenuItem(menuNumber));
		}
		logger.info(list.toString());
		md.receiveCommand(new PlaceOrder(id, list));
	}

	@PostMapping(value = "/placeorder", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void placeOrder(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		long id = temp.longValue();
		List<Integer> orderedItem = (List) param.get("ordered_item[]");
		placeOrder(id, orderedItem.toArray(new Integer[0]));
	}

	@PostMapping(value = "/markdrinksserved")
	public @ResponseBody void markDrinksServed(@RequestParam("id") long id,
			@RequestParam("drinks_served[]") Integer[] orderedItem) {
		md.receiveCommand(new MarkDrinksServed(id, Arrays.asList(orderedItem)));
	}

	@PostMapping(value = "/markdrinksserved", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void markDrinksServed(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		long id = temp.longValue();
		List<Integer> orderedItem = (List) param.get("drinks_served[]");
		markDrinksServed(id, orderedItem.toArray(new Integer[0]));
	}

	@PostMapping(value = "/markfoodprepared")
	public @ResponseBody void markFoodPrepared(@RequestParam("id") long id,
			@RequestParam("food_prepared[]") Integer[] orderedItem) {
		md.receiveCommand(new MarkFoodPrepared(id, Arrays.asList(orderedItem)));
	}

	@PostMapping(value = "/markfoodprepared", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void markFoodPrepared(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		long id = temp.longValue();
		List<Integer> orderedItem = (List) param.get("food_prepared[]");
		markFoodPrepared(id, orderedItem.toArray(new Integer[0]));
	}

	@PostMapping(value = "/markfoodserved")
	public @ResponseBody void markFoodServed(@RequestParam("id") long id,
			@RequestParam("food_served[]") Integer[] orderedItem) {
		md.receiveCommand(new MarkFoodServed(id, Arrays.asList(orderedItem)));
	}

	@PostMapping(value = "/markfoodserved", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void markFoodServed(@RequestBody Map<String, Object> param) {
		logger.debug(param.toString());
		Number temp = (Number) param.get("id");
		long id = temp.longValue();
		List<Integer> orderedItem = (List) param.get("food_served[]");
		markFoodServed(id, orderedItem.toArray(new Integer[0]));
	}

	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Tab not found, probably not open")
	@ExceptionHandler(TabNotOpen.class)
	public void notOpen() {
	}

	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Operation could not be done.")
	@ExceptionHandler(TabException.class)
	public void conflict() {
	}

}
