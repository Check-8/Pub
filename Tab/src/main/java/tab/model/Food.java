package tab.model;

import java.util.List;

public class Food {

    public enum State {PREPARED, SERVED}

    private long id;
    private List<Integer> foodItems;
    private State state;

    public Food() {
    }

    public Food(long id, List<Integer> foodItems) {
        this.id = id;
        this.foodItems = foodItems;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Integer> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<Integer> foodItems) {
        this.foodItems = foodItems;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
