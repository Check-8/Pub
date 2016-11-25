package chefTodo.queue;

import chefTodo.events.Event;

public interface Subscriber {

	public void onEvent(Event event);
}
