package openTabs.service;

import openTabs.readModel.ItemTodo;
import openTabs.readModel.OpenTabsQueries;
import openTabs.readModel.QueryParams;
import openTabs.readModel.TableTodo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ReadTabController {

    private static final Logger logger = LoggerFactory.getLogger(ReadTabController.class);

    @Autowired
    @Qualifier("openTabs")
    private OpenTabsQueries openTabs;

    @GetMapping(path = "/tab/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableTodo getTab(@PathVariable("id") long id) {
        TableTodo res = openTabs.getByTab(id);
        logger.info("getByTab: {} {}", id, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/table/{table_number}")
    public TableTodo getOpenByTableNumber(@PathVariable("table_number") int tableNumber) {
        TableTodo res = openTabs.getOpenByTableNumber(tableNumber);
        logger.info("getByTableNumber: {} {}", tableNumber, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/{waiter}")
    public Map<Integer, List<ItemTodo>> getTodoListByWaiter(@PathVariable("waiter") String waiter) {
        Map<Integer, List<ItemTodo>> res = openTabs.getTodoListForWaiter(waiter);
        logger.info("getTodoListByWaiter: {} {}", waiter, res.toString());
        return res;
    }

    @GetMapping(value = "/tab/topay/{table_number}")
    public Double getToPayByTableNumber(@PathVariable("table_number") int tableNumber) {
        Double toPay = openTabs.getPriceToPayByTableNumber(tableNumber);
        logger.info("getToPayByTableNumber: {} {}", tableNumber, toPay);
        return toPay;
    }

    @GetMapping(path = "/tab")
    public List<TableTodo> queryTab(@RequestParam Map<String, String> params) {
        logger.debug("Query for tabs: {}", params);
        QueryParams qparams = QueryParams.convert(params);
        return openTabs.getByParams(qparams);
    }

}
