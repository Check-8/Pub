package tab.model;

import java.util.List;

public class Drink {

    private long id;
    private List<Integer> drinksServed;

    public Drink() {
    }

    public Drink(long id, List<Integer> drinksServed) {
        this.id = id;
        this.drinksServed = drinksServed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Integer> getDrinksServed() {
        return drinksServed;
    }

    public void setDrinksServed(List<Integer> drinksServed) {
        this.drinksServed = drinksServed;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "id=" + id +
                ", drinksServed=" + drinksServed +
                '}';
    }
}
