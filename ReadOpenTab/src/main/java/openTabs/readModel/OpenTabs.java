package openTabs.readModel;

import openTabs.ApplyEvent;
import openTabs.events.DrinksOrdered;
import openTabs.events.DrinksServed;
import openTabs.events.Event;
import openTabs.events.FoodOrdered;
import openTabs.events.FoodPrepared;
import openTabs.events.FoodServed;
import openTabs.events.OrderedItem;
import openTabs.events.TabClosed;
import openTabs.events.TabOpened;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenTabs implements ApplyEvent, OpenTabsQueries, ReadModel {
    private final List<ApplyEvent> appliers;
    private final Map<Long, LockAndTableTodo> todoByTab;

    public OpenTabs() {
        appliers = List.of(new TabOpenedApplier(), new FoodOrderedApplier(), new DrinksOrderedApplier(),
                           new FoodPreparedApplier(), new FoodServedApplier(), new DrinksServedApplier(),
                           new TabClosedApplier());

        todoByTab = new ConcurrentHashMap<>();
    }

    @Override
    public boolean applies(Event e) {
        return true;
    }

    private class TabOpenedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof TabOpened;
        }

        @Override
        public void apply(Event event) {
            TabOpened t = (TabOpened) event;
            TableTodo tt = new TableTodo(t.getId(), t.getTableNumber(), t.getWaiter());
            todoByTab.put(event.getId(), LockAndTableTodo.lockAndTable(tt));
        }
    }

    private class FoodOrderedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof FoodOrdered;
        }

        @Override
        public void apply(Event event) {
            FoodOrdered f = (FoodOrdered) event;
            LockAndTableTodo ltt = todoByTab.get(f.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                for (OrderedItem oi : f.getItems()) {
                    ItemTodo item = new ItemTodo(oi.getMenuNumber(), oi.getDescription(), oi.getPrice());
                    tt.addInPreparation(item);
                }
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    private class DrinksOrderedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof DrinksOrdered;
        }

        @Override
        public void apply(Event event) {
            DrinksOrdered d = (DrinksOrdered) event;
            LockAndTableTodo ltt = todoByTab.get(d.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                for (OrderedItem oi : d.getItems()) {
                    ItemTodo item = new ItemTodo(oi.getMenuNumber(), oi.getDescription(), oi.getPrice());
                    tt.addDrinkToServe(item);
                }
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    private class FoodPreparedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof FoodPrepared;
        }

        @Override
        public void apply(Event event) {
            FoodPrepared f = (FoodPrepared) event;
            LockAndTableTodo ltt = todoByTab.get(f.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                for (Integer num : f.getMenuItems()) {
                    ItemTodo item = tt.removeByMenuNumberInPreparation(num);
                    tt.addFoodToServe(item);
                }
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    private class FoodServedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof FoodServed;
        }

        @Override
        public void apply(Event event) {
            FoodServed f = (FoodServed) event;
            LockAndTableTodo ltt = todoByTab.get(f.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                for (Integer num : f.getMenuItems()) {
                    ItemTodo servedItem = tt.removeByMenuNumberFoodToServe(num);
                    tt.addServed(servedItem);
                }
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    private class DrinksServedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof DrinksServed;
        }

        @Override
        public void apply(Event event) {
            DrinksServed d = (DrinksServed) event;
            LockAndTableTodo ltt = todoByTab.get(d.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                for (Integer num : d.getMenuItems()) {
                    ItemTodo servedItem = tt.removeByMenuNumberDrinkToServe(num);
                    tt.addServed(servedItem);
                }
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    private class TabClosedApplier implements ApplyEvent {

        @Override
        public boolean applies(Event e) {
            return e instanceof TabClosed;
        }

        @Override
        public void apply(Event event) {
            TabClosed t = (TabClosed) event;
            LockAndTableTodo ltt = todoByTab.get(t.getId());
            TableTodo tt = ltt.getTableTodo();
            ltt.getLock()
               .lock();
            try {
                tt.setAmountPaid(t.getAmountPaid());
                tt.setToPay(t.getOrderValue());
                tt.setTip(t.getTipValue());
                tt.setClosed(true);
            } finally {
                ltt.getLock()
                   .unlock();
            }
        }
    }

    @Override
    public void apply(Event event) {
        appliers.stream()
                .filter((applier) -> applier.applies(event))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Event not supported: " + event.getClass()))
                .apply(event);
    }

    @Override
    public TableTodo getByTab(long idTab) {
        LockAndTableTodo ltt = todoByTab.get(idTab);
        TableTodo tt = ltt.getTableTodo();
        ltt.getLock()
           .lock();
        try {
            return tt.clone();
        } finally {
            ltt.getLock()
               .unlock();
        }
    }

    private Stream<TableTodo> getStreamByParams(QueryParams params) {
        return todoByTab.values()
                        .stream()
                        .map(LockAndTableTodo::getTableTodo)
                        .filter(tt -> params.getOpen().equals(!tt.isClosed())
                                && params.getWaiter().map(w -> w.equalsIgnoreCase(tt.getWaiter())).orElse(true)
                                && params.getTableNumber().map(t -> t.equals(tt.getTableNumber())).orElse(true))
                        .map(TableTodo::clone);
    }

    @Override
    public List<TableTodo> getByParams(QueryParams params) {
        return getStreamByParams(params).collect(Collectors.toList());
    }

    @Override
    public TableTodo getOpenByTableNumber(int tableNumber) {
        QueryParams qp = new QueryParams();
        qp.setTableNumber(tableNumber);
        return getStreamByParams(qp)
                .findAny()
                .map(TableTodo::clone)
                .orElse(null);
    }

    @Override
    public Map<Integer, List<ItemTodo>> getTodoListForWaiter(String waiter) {
        QueryParams qp = new QueryParams();
        qp.setWaiter(waiter);
        return getStreamByParams(qp)
                .collect(Collectors.toMap(TableTodo::getTableNumber, TableTodo::getToServe));
    }

    private Double getToPay(TableTodo tt) {
        if (tt == null)
            return null;

        return tt.getAllItems()
                 .stream()
                 .map(ItemTodo::getPrice)
                 .reduce(Double::sum)
                 .orElse(0d);
    }

    @Override
    public Double getPriceToPayByTab(long idTab) {
        TableTodo tt = getByTab(idTab);
        return getToPay(tt);
    }

    @Override
    public Double getPriceToPayByTableNumber(int tableNumber) {
        TableTodo tt = getOpenByTableNumber(tableNumber);
        return getToPay(tt);
    }

    private static class LockAndTableTodo {
        private final Lock lock;
        private final TableTodo tableTodo;

        public LockAndTableTodo(Lock lock, TableTodo tableTodo) {
            this.lock = lock;
            this.tableTodo = tableTodo;
        }

        public Lock getLock() {
            return lock;
        }

        public TableTodo getTableTodo() {
            return tableTodo;
        }

        public static LockAndTableTodo lockAndTable(TableTodo tt) {
            return new LockAndTableTodo(new ReentrantLock(), tt);
        }
    }
}
