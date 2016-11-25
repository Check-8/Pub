package chefTodo.readModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import chefTodo.ApplyEvent;
import chefTodo.events.Event;
import chefTodo.events.FoodOrdered;
import chefTodo.events.FoodPrepared;

@SuppressWarnings("rawtypes")
public class ChefTodoList implements ApplyEvent, ChefTodoListQueries, ReadModel {

	private final Map<Class<? extends Event>, ApplyEvent> applier;

	private Map<Long, TodoListGroup> todoMap;

	public ChefTodoList() {
		Map<Class<? extends Event>, ApplyEvent> aTemp = new HashMap<>();
		aTemp.put(FoodOrdered.class, new FoodOrderedApplier());
		aTemp.put(FoodPrepared.class, new FoodPreparedApplier());
		applier = Collections.unmodifiableMap(aTemp);

		todoMap = new HashMap<>();
	}

	private class FoodOrderedApplier implements ApplyEvent<FoodOrdered> {
		@Override
		public void apply(FoodOrdered event) {
			TodoListGroup group = null;
			List<TodoListItem> items = event.getItems().stream()
					.map(item -> new TodoListItem(item.getMenuNumber(), item.getDescription()))
					.collect(Collectors.toList());
			group = new TodoListGroup(event.getId(), items);
			synchronized (todoMap) {
				todoMap.put(group.getTab(), group);
			}
		}
	}

	private class FoodPreparedApplier implements ApplyEvent<FoodPrepared> {
		@Override
		public void apply(FoodPrepared event) {
			synchronized (todoMap) {
				TodoListGroup grp = null;
				grp = todoMap.get(event.getId());
				for (Integer num : event.getMenuItems()) {
					grp.removeByMenuNumber(num);
				}
				if (grp.getItems().isEmpty())
					todoMap.remove(grp);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void apply(Event event) {
		if (applier.containsKey(event.getClass())) {
			applier.get(event.getClass()).apply(event);
		} else {
			throw new IllegalArgumentException("Event not supported: " + event.getClass());
		}
	}

	@Override
	public List<TodoListGroup> getTodoList() {
		List<TodoListGroup> copy = null;
		copy = new ArrayList<>();
		synchronized (todoMap) {
			for (TodoListGroup tlg : todoMap.values()) {
				copy.add(new TodoListGroup(tlg.getTab(), new ArrayList<>(tlg.getItems())));
			}
		}
		return copy;
	}

}
