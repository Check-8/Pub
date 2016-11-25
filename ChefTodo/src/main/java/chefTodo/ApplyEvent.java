package chefTodo;

import chefTodo.events.Event;

@FunctionalInterface
public interface ApplyEvent<T extends Event> {
	public void apply(T event);
}
