package tab.aggregate;

import java.util.Collection;

import tab.commands.Command;
import tab.events.Event;


@FunctionalInterface
public interface CommandHandler<T extends Command> {
	public Collection<Event> handle(T command);
}
