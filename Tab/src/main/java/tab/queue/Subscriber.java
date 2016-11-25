package tab.queue;

import tab.commands.Command;
import tab.events.Event;

public interface Subscriber {
	public void onMessage(Command command);

	public void onEvent(Event event);
}
