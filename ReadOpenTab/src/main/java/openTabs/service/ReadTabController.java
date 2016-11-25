package openTabs.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import openTabs.readModel.ItemTodo;
import openTabs.readModel.OpenTabsQueries;
import openTabs.readModel.TableTodo;

@Controller
public class ReadTabController {

	private Logger logger = LoggerFactory.getLogger(ReadTabController.class);

	@Autowired
	@Qualifier("openTabs")
	private OpenTabsQueries openTabs;

	@RequestMapping(value = "/tab/id/{id}", method = RequestMethod.GET)
	public @ResponseBody TableTodo getByTab(@PathVariable("id") long id) {
		TableTodo res = openTabs.getByTab(id);
		logger.info("getByTab: " + id + " " + res.toString());
		return res;
	}

	@RequestMapping(value = "/tab/table/{table_number}", method = RequestMethod.GET)
	public @ResponseBody TableTodo getOpenByTableNumber(@PathVariable("table_number") int tableNumber) {
		TableTodo res = openTabs.getOpenByTableNumber(tableNumber);
		logger.info("getByTableNumber: " + tableNumber + " " + res.toString());
		return res;
	}

	@RequestMapping(value = "/tab/{waiter}", method = RequestMethod.GET)
	public @ResponseBody Map<Integer, List<ItemTodo>> getTodoListByWaiter(@PathVariable("waiter") String waiter) {
		Map<Integer, List<ItemTodo>> res = openTabs.getTodoListForWaiter(waiter);
		logger.info("getTodoListByWaiter: " + waiter + " " + res.toString());
		return res;
	}

	@RequestMapping(value = "/tab/topay/{table_number}", method = RequestMethod.GET)
	public @ResponseBody Double getToPayByTableNumber(
			@PathVariable("table_number") int tableNumber) {
		Double toPay = openTabs.getPriceToPayByTableNumber(tableNumber);
		logger.info("getToPayByTableNumber: " + tableNumber + " " + toPay);
		return toPay;
	}

}
