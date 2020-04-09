package tab.model;

import java.util.List;

public class Order {

    private long id;
    private List<Integer> orderItems;

    public Order() {
    }

    public Order(long id, List<Integer> orderItems) {
        this.id = id;
        this.orderItems = orderItems;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Integer> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<Integer> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderItems=" + orderItems +
                '}';
    }
}
