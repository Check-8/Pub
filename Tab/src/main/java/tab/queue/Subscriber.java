package tab.queue;

import tab.commands.Command;
import tab.events.Event;

public interface Subscriber {
	void onMessage(Command command);

	void onEvent(Event event);
}
