package tab.aggregate;

import java.util.Collection;

import tab.commands.Command;
import tab.events.Event;


public interface CommandHandler {
	boolean handles(Command c);

	Collection<Event> handle(Command command);
}
