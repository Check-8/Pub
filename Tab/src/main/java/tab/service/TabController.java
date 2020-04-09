package tab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import tab.exception.TabException;
import tab.exception.TabNotOpen;
import tab.model.Drink;
import tab.model.Food;
import tab.model.Order;
import tab.model.Tab;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class TabController {
    private static final Logger logger = LoggerFactory.getLogger(TabController.class);

    @Autowired
    @Qualifier("messageDispatcher")
    private MessageDispatcher md;

    @Autowired
    @Qualifier("menuClient")
    private MenuClient menu;

    @PostMapping(path = "/tabs")
    public void openTabRest(@RequestBody Tab tab) {
        logger.debug(tab.toString());
        if (tab.getId() == null) {
            tab.setId(ThreadLocalRandom.current()
                                       .nextInt());
        }
        md.onMessage(new OpenTab(tab));
    }

    @DeleteMapping(path = "/tabs/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void closeTabRest(@PathVariable long id, @RequestParam double amountPaid) {
        md.onMessage(new CloseTab(id, amountPaid));
    }

    @PostMapping(path = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void placeOrderRest(@RequestBody Order order) {
        logger.info(order.toString());
        var list = order.getOrderItems()
                        .stream()
                        .map(menu::getMenuItem)
                        .collect(Collectors.toList());
        md.onMessage(new PlaceOrder(order.getId(), list));
    }

    @PostMapping(path = "/drinks", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void drinksRest(@RequestBody Drink drinks) {
        logger.debug(drinks.toString());
        md.onMessage(new MarkDrinksServed(drinks.getId(), drinks.getDrinksServed()));
    }

    @PostMapping(path = "/foods", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void foodsRest(@RequestBody Food foods) {
        logger.info(foods.toString());
        switch (foods.getState()) {
            case SERVED:
                md.onMessage(new MarkFoodServed(foods.getId(), foods.getFoodItems()));
                break;
            case PREPARED:
                md.onMessage(new MarkFoodPrepared(foods.getId(), foods.getFoodItems()));
                break;
        }

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
