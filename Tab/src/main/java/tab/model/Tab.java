package tab.model;

public class Tab {
    private Integer id;
    private Integer tableNumber;
    private String waiter;

    public Tab() {
    }

    public Tab(Integer id, Integer tableNumber, String waiter) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.waiter = waiter;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getWaiter() {
        return waiter;
    }

    public void setWaiter(String waiter) {
        this.waiter = waiter;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id=" + id +
                ", tableNumber=" + tableNumber +
                ", waiter='" + waiter + '\'' +
                '}';
    }
}
