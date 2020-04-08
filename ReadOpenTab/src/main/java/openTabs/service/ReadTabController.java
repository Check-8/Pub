package openTabs.service;

import openTabs.readModel.ItemTodo;
import openTabs.readModel.OpenTabsQueries;
import openTabs.readModel.TableTodo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ReadTabController {

    private static final Logger logger = LoggerFactory.getLogger(ReadTabController.class);

    @Autowired
    @Qualifier("openTabs")
    private OpenTabsQueries openTabs;

    @GetMapping(value = "/tab/id/{id}")
    public @ResponseBody
    TableTodo getByTab(@PathVariable("id") long id) {
        TableTodo res = openTabs.getByTab(id);
        logger.info("getByTab: {} {}", id, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/table/{table_number}")
    public @ResponseBody
    TableTodo getOpenByTableNumber(@PathVariable("table_number") int tableNumber) {
        TableTodo res = openTabs.getOpenByTableNumber(tableNumber);
        logger.info("getByTableNumber: {} {}", tableNumber, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/{waiter}")
    public @ResponseBody
    Map<Integer, List<ItemTodo>> getTodoListByWaiter(@PathVariable("waiter") String waiter) {
        Map<Integer, List<ItemTodo>> res = openTabs.getTodoListForWaiter(waiter);
        logger.info("getTodoListByWaiter: {} {}", waiter, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/topay/{table_number}")
    public @ResponseBody
    Double getToPayByTableNumber(
            @PathVariable("table_number") int tableNumber) {
        Double toPay = openTabs.getPriceToPayByTableNumber(tableNumber);
        logger.info("getToPayByTableNumber: {} {}", tableNumber, toPay);
        return toPay;
    }

}
