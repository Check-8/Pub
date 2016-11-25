package tab.events;

import java.util.Collection;
import java.util.List;

public interface Events {
	public void save(Event e);

	public void saveAll(Collection<Event> ee);

	public List<Event> getAllEvent();

	public List<Event> getAllEventForId(long id);
}
