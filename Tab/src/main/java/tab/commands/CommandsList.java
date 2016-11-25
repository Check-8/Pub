package tab.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class CommandsList implements Commands {
	private Queue<Command> commands;

	public CommandsList() {
		commands = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void save(Command c) {
		commands.add(c);
	}

	@Override
	public void saveAll(Collection<Command> cc) {
		commands.addAll(cc);
	}

	@Override
	public List<Command> getAllCommand() {
		return new ArrayList<>(commands);
	}

	@Override
	public List<Command> getAllCommandForId(long id) {
		return commands.stream().filter(c -> c.getId() == id).collect(Collectors.toList());
	}

}
