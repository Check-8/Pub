package tab.queue;

import java.util.Collection;

import tab.events.Event;

public interface ProduttoreCoda {
	public void post(Collection<Event> events);
}
