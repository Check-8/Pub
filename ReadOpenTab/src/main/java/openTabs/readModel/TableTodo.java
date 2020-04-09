package openTabs.readModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class TableTodo {
    private long tabId;
    private int tableNumber;
    private String waiter;
    private List<ItemTodo> drinksToServe;
    private List<ItemTodo> foodToServe;
    private List<ItemTodo> inPreparation;
    private List<ItemTodo> served;

    private double amountPaid;
    private double toPay;
    private double tip;
    private boolean closed;

    private TableTodo() {
    }

    public TableTodo(long tabId, int tableNumber, String waiter, List<ItemTodo> drinksToServe,
                     List<ItemTodo> foodToServe, List<ItemTodo> inPreparation, List<ItemTodo> served) {
        super();
        this.tabId = tabId;
        this.tableNumber = tableNumber;
        this.waiter = waiter;
        this.drinksToServe = drinksToServe;
        this.foodToServe = foodToServe;
        this.inPreparation = inPreparation;
        this.served = served;

        amountPaid = 0;
        toPay = 0;
        tip = 0;
        closed = false;
    }

    public TableTodo(long tabId, int tableNumber, String waiter) {
        this(tabId, tableNumber, waiter, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public long getTabId() {
        return tabId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public String getWaiter() {
        return waiter;
    }

    public List<ItemTodo> getDrinkToServe() {
        return drinksToServe;
    }

    public List<ItemTodo> getFoodToServe() {
        return foodToServe;
    }

    public List<ItemTodo> getToServe() {
        List<ItemTodo> temp = new ArrayList<>(drinksToServe);
        temp.addAll(foodToServe);
        return temp;
    }

    public List<ItemTodo> getServed() {
        return served;
    }

    @JsonIgnore
    public List<ItemTodo> getAllItems() {
        List<ItemTodo> temp = new ArrayList<>(getServed());
        temp.addAll(getToServe());
        return temp;
    }

    public List<ItemTodo> getInPreparation() {
        return inPreparation;
    }

    public void addDrinkToServe(ItemTodo item) {
        drinksToServe.add(item);
    }

    public void addFoodToServe(ItemTodo item) {
        foodToServe.add(item);
    }

    public void addInPreparation(ItemTodo item) {
        inPreparation.add(item);
    }

    public void addServed(ItemTodo item) {
        served.add(item);
    }


    public ItemTodo removeByMenuNumberDrinkToServe(Integer num) {
        ItemTodo item = drinksToServe.stream()
                                     .filter(i -> i.getMenuNumber() == num)
                                     .findFirst()
                                     .get();
        drinksToServe.remove(item);
        return item;
    }

    public ItemTodo removeByMenuNumberFoodToServe(Integer num) {
        ItemTodo item = foodToServe.stream()
                                   .filter(i -> i.getMenuNumber() == num)
                                   .findFirst()
                                   .get();
        foodToServe.remove(item);
        return item;
    }

    public ItemTodo removeByMenuNumberInPreparation(Integer num) {
        ItemTodo item = inPreparation.stream()
                                     .filter(i -> i.getMenuNumber() == num)
                                     .findFirst()
                                     .get();
        inPreparation.remove(item);
        return item;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setToPay(double toPay) {
        this.toPay = toPay;
    }

    public void setTip(double tip) {
        this.tip = tip;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getToPay() {
        return toPay;
    }

    public double getTip() {
        return tip;
    }

    public void setClosed(boolean state) {
        closed = state;
    }

    public boolean isClosed() {
        return closed;
    }

    public TableTodo clone() {
        TableTodo tt = new TableTodo();
        tt.tabId = this.tabId;
        tt.tableNumber = this.tableNumber;
        tt.waiter = this.waiter;
        tt.amountPaid = this.amountPaid;
        tt.toPay = this.toPay;
        tt.tip = this.tip;
        tt.closed = this.closed;

        List<ItemTodo> tempDrinkToServe = new ArrayList<>(drinksToServe);
        List<ItemTodo> tempFoodToServe = new ArrayList<>(foodToServe);
        List<ItemTodo> tempInPreparation = new ArrayList<>(inPreparation);
        List<ItemTodo> tempServed = new ArrayList<>(served);
        tt.drinksToServe = tempDrinkToServe;
        tt.foodToServe = tempFoodToServe;
        tt.inPreparation = tempInPreparation;
        tt.served = tempServed;
        return tt;
    }

    @Override
    public String toString() {
        return "TableTodo [tabId=" + tabId + ", tableNumber=" + tableNumber + ", waiter=" + waiter + ", drinksToServe="
                + drinksToServe + ", foodToServe=" + foodToServe + ", inPreparation=" + inPreparation + ", amountPaid="
                + amountPaid + ", toPay=" + toPay + ", tip=" + tip + ", closed=" + closed + "]";
    }

}
