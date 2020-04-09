package tab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tab.aggregate.TabAggregate;
import tab.commands.Command;
import tab.commands.Commands;
import tab.events.Event;
import tab.events.Events;
import tab.exception.TabException;
import tab.queue.ConsumatoreCoda;
import tab.queue.ProduttoreCoda;
import tab.queue.Subscriber;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Component
@Qualifier("messageDispatcher")
public class MessageDispatcher implements Subscriber {
	private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

	@Autowired
	@Qualifier("tabAggregates")
	private TabAggregates aggregates;

	@Autowired
	@Qualifier("events")
	private Events events;

	@Autowired
	@Qualifier("commands")
	private Commands commands;

	@Autowired
	@Qualifier("produttoreCoda")
	private ProduttoreCoda producer;

	@Autowired
	@Qualifier("consumatoreCoda")
	private ConsumatoreCoda consumer;

	public MessageDispatcher() {
	}

	@PostConstruct
	public void init() {
		consumer.subscribe(this);
	}

	@Override
	public void onMessage(Command command) {
		logger.debug("Ricevuto comando: {} con ID: {}", command.getClass(), command.getId());
		commands.save(command);
		long id = command.getId();
		TabAggregate tab = aggregates.getAggregate(id);
		Collection<Event> events = null;
		try {
			events = tab.handle(command);
		} catch (TabException e) {
			logger.info("Command: {} on ID: {} {}",command.getClass(), command.getId(), e.getMessage());
			logger.error("Command could not be handled", e);
			throw e;
		}
		producer.post(events);
		this.events.saveAll(events);
	}

	@Override
	public void onEvent(Event event) {
		TabAggregate tab = aggregates.getAggregate(event.getId());
		tab.apply(event);
	}

}
