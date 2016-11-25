package tab.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class EventsList implements Events {
	private Queue<Event> events;

	public EventsList() {
		events = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void save(Event e) {
		events.add(e);
	}

	@Override
	public void saveAll(Collection<Event> ee) {
		events.addAll(ee);
	}

	@Override
	public List<Event> getAllEvent() {
		List<Event> list = null;
		list = new ArrayList<>(events);
		return list;
	}

	@Override
	public List<Event> getAllEventForId(long id) {
		return events.stream().filter(e -> e.getId() == id).collect(Collectors.toList());
	}
}
