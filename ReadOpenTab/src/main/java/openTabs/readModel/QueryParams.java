package openTabs.readModel;

import java.util.Map;
import java.util.Optional;

public class QueryParams {
    private Boolean open = true;
    private Integer tableNumber;
    private String waiter;

    public QueryParams() {

    }

    public Optional<Integer> getTableNumber() {
        return Optional.ofNullable(tableNumber);
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Boolean getOpen() {
        return open == null ? true : open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Optional<String> getWaiter() {
        return Optional.ofNullable(waiter);
    }

    public void setWaiter(String waiter) {
        this.waiter = waiter;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "open=" + open +
                ", tableNumber=" + tableNumber +
                ", waiter='" + waiter + '\'' +
                '}';
    }

    public static QueryParams convert(Map<String, String> params) {
        QueryParams queryParams = new QueryParams();
        if (params.containsKey("open")) {
            queryParams.setOpen(Boolean.parseBoolean(params.get("open")));
        }
        if (params.containsKey("waiter")) {
            queryParams.setWaiter(params.get("waiter"));
        }
        if (params.containsKey("tableNumber")) {
            queryParams.setTableNumber(Integer.parseInt(params.get("tableNumber")));
        }
        return queryParams;
    }
}
