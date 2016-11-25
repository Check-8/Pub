package tab.commands;

import java.util.Collection;
import java.util.List;

public interface Commands {
	public void save(Command c);

	public void saveAll(Collection<Command> c);

	public List<Command> getAllCommand();

	public List<Command> getAllCommandForId(long id);
}
