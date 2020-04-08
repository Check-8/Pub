package tab.queue;

import java.util.Collection;

import tab.events.Event;

public interface ProduttoreCoda {
	void post(Collection<Event> events);
}
