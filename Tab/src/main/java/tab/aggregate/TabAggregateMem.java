package tab.aggregate;

import tab.commands.CloseTab;
import tab.commands.Command;
import tab.commands.MarkDrinksServed;
import tab.commands.MarkFoodPrepared;
import tab.commands.MarkFoodServed;
import tab.commands.OpenTab;
import tab.commands.PlaceOrder;
import tab.events.DrinksOrdered;
import tab.events.DrinksServed;
import tab.events.Event;
import tab.events.FoodOrdered;
import tab.events.FoodPrepared;
import tab.events.FoodServed;
import tab.events.OrderedItem;
import tab.events.TabClosed;
import tab.events.TabOpened;
import tab.exception.DrinksNotOutstanding;
import tab.exception.FoodNotOutstanding;
import tab.exception.FoodNotPrepared;
import tab.exception.NotEnough;
import tab.exception.NotEverythingServed;
import tab.exception.TabNotOpen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TabAggregateMem implements TabAggregate {

    private final List<CommandHandler> handlers;
    private final List<ApplyEvent> applier;

    private final List<OrderedItem> outstandingDrinks;
    private final List<OrderedItem> outstandingFood;
    private final List<OrderedItem> preparedFood;
    private boolean open;
    private double servedItemsValue;

    public TabAggregateMem() {
        handlers = List.of(new OpenTabHandler(), new PlaceOrderHandler(),
                           new MarkDrinksServedHandler(), new MarkFoodPreparedHandler(),
                           new MarkFoodServedHandler(), new CloseTabHandler());

        applier = List.of(new TabOpenedApplier(), new DrinksOrderedApplier(),
                          new DrinksServedApplier(), new FoodOrderedApplier(),
                          new FoodPreparedApplier(), new FoodServedApplier(),
                          new TabClosedApplier());

        open = false;
        servedItemsValue = 0;
        outstandingDrinks = new ArrayList<>();
        outstandingFood = new ArrayList<>();
        preparedFood = new ArrayList<>();
    }

    public boolean handles(Command c) {
        return true;
    }

    public boolean applies(Event e) {
        return true;
    }

    private static class OpenTabHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c instanceof OpenTab;
        }

        @Override
        public Collection<Event> handle(Command c) {
            OpenTab o = (OpenTab) c;
            TabOpened tabOpened = new TabOpened(o.getId(), o.getTableNumber(), o.getWaiter());
            return List.of(tabOpened);
        }
    }

    private class PlaceOrderHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c instanceof PlaceOrder;
        }

        @Override
        public Collection<Event> handle(Command c) {
            PlaceOrder p = (PlaceOrder) c;
            if (!open)
                throw new TabNotOpen();
            Collection<Event> events = new ArrayList<>();
            var drinks = p.getItems()
                          .stream()
                          .filter(OrderedItem::isDrink)
                          .collect(Collectors.toList());
            if (!drinks.isEmpty()) {
                events.add(new DrinksOrdered(c.getId(), drinks));
            }
            var food = p.getItems()
                        .stream()
                        .filter(Predicate.not(OrderedItem::isDrink))
                        .collect(Collectors.toList());
            if (!food.isEmpty()) {
                events.add(new FoodOrdered(c.getId(), food));
            }
            return events;
        }
    }

    private class MarkDrinksServedHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c instanceof MarkDrinksServed;
        }

        @Override
        public Collection<Event> handle(Command c) {
            MarkDrinksServed m = (MarkDrinksServed) c;
            if (!open)
                throw new TabNotOpen();
            if (!areDrinksOutstanding(m.getMenuItems()))
                throw new DrinksNotOutstanding();
            return List.of(new DrinksServed(m.getId(), m.getMenuItems()));
        }

    }

    private class MarkFoodPreparedHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c instanceof MarkFoodPrepared;
        }

        @Override
        public Collection<Event> handle(Command c) {
            MarkFoodPrepared m = (MarkFoodPrepared) c;
            if (!open)
                throw new TabNotOpen();
            if (!areFoodOutstanding(m.getMenuItems()))
                throw new FoodNotOutstanding();
            return List.of(new FoodPrepared(m.getId(), m.getMenuItems()));
        }
    }

    private class MarkFoodServedHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c instanceof MarkFoodServed;
        }

        @Override
        public Collection<Event> handle(Command c) {
            MarkFoodServed m = (MarkFoodServed) c;
            if (!open)
                throw new TabNotOpen();
            if (!areFoodPrepared(m.getMenuItems()))
                throw new FoodNotPrepared();
            return List.of(new FoodServed(m.getId(), m.getMenuItems()));
        }
    }

    private class CloseTabHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c instanceof CloseTab;
        }

        @Override
        public Collection<Event> handle(Command c) {
            CloseTab t = (CloseTab) c;
            if (!open)
                throw new TabNotOpen();
            if (!outstandingDrinks.isEmpty() || !outstandingFood.isEmpty() || !preparedFood.isEmpty())
                throw new NotEverythingServed();
            double ap = t.getAmountPaid();
            if (ap < servedItemsValue)
                throw new NotEnough();
            return List.of(new TabClosed(t.getId(), ap, servedItemsValue, ap - servedItemsValue));
        }
    }

    private class TabOpenedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof TabOpened;
        }

        @Override
        public void apply(Event event) {
            open = true;
        }
    }

    private class DrinksOrderedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof DrinksOrdered;
        }

        @Override
        public void apply(Event event) {
            DrinksOrdered d = (DrinksOrdered) event;
            outstandingDrinks.addAll(d.getItems());
        }
    }

    private class DrinksServedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof DrinksServed;
        }

        @Override
        public void apply(Event event) {
            DrinksServed d = (DrinksServed) event;
            for (Integer num : d.getMenuItems()) {
                outstandingDrinks.stream()
                                 .filter(i -> i.getMenuNumber() == num)
                                 .findFirst()
                                 .ifPresent((item) -> {
                                     outstandingDrinks.remove(item);
                                     servedItemsValue += item.getPrice();
                                 });
            }
        }
    }

    private class FoodOrderedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof FoodOrdered;
        }

        @Override
        public void apply(Event event) {
            FoodOrdered f = (FoodOrdered) event;
            outstandingFood.addAll(f.getItems());
        }
    }

    private class FoodPreparedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof FoodPrepared;
        }

        @Override
        public void apply(Event event) {
            FoodPrepared f = (FoodPrepared) event;
            for (Integer num : f.getMenuItems()) {
                outstandingFood.stream()
                               .filter(i -> i.getMenuNumber() == num)
                               .findFirst()
                               .ifPresent((item) -> {
                                   outstandingFood.remove(item);
                                   preparedFood.add(item);
                               });
            }
        }
    }

    private class FoodServedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof FoodServed;
        }

        @Override
        public void apply(Event event) {
            FoodServed f = (FoodServed) event;
            for (Integer num : f.getMenuItems()) {
                preparedFood.stream()
                            .filter(i -> i.getMenuNumber() == num)
                            .findFirst()
                            .ifPresent((item) -> {
                                preparedFood.remove(item);
                                servedItemsValue += item.getPrice();
                            });
            }
        }
    }

    private class TabClosedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event event) {
            return event instanceof TabClosed;
        }

        @Override
        public void apply(Event event) {
            open = false;
        }

    }

    @Override
    public Collection<Event> handle(Command command) {
        return handlers.stream()
                       .filter((handler) -> handler.handles(command))
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException("Command not supported: " + command.getClass()))
                       .handle(command);
    }

    @Override
    public void apply(Event event) {
        applier.stream()
               .filter((applier) -> applier.applies(event))
               .findAny()
               .orElseThrow(() -> new IllegalArgumentException("Command not supported: " + event.getClass()))
               .apply(event);
    }

    private boolean areDrinksOutstanding(List<Integer> menuNumbers) {
        var curOutstanding = outstandingDrinks.stream()
                                          .map(OrderedItem::getMenuNumber)
                                          .collect(Collectors.toCollection(ArrayList::new));
        for (Integer item : menuNumbers)
            if (!curOutstanding.remove(item))
                return false;
        return true;
    }

    private boolean areFoodOutstanding(List<Integer> menuNumbers) {
        var curOutstanding = outstandingFood.stream()
                                        .map(OrderedItem::getMenuNumber)
                                        .collect(Collectors.toCollection(ArrayList::new));
        for (Integer item : menuNumbers)
            if (!curOutstanding.remove(item))
                return false;
        return true;
    }

    private boolean areFoodPrepared(List<Integer> menuNumbers) {
        var curOutstanding = preparedFood.stream()
                                     .map(OrderedItem::getMenuNumber)
                                     .collect(Collectors.toCollection(ArrayList::new));
        for (Integer item : menuNumbers)
            if (!curOutstanding.remove(item))
                return false;
        return true;
    }

}
