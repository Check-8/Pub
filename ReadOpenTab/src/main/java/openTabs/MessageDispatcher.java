package openTabs;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import openTabs.events.Event;
import openTabs.queue.ConsumatoreCoda;
import openTabs.queue.Subscriber;
import openTabs.readModel.ReadModel;

@Component
@Qualifier("messageDispatcher")
public class MessageDispatcher implements Subscriber {
	@Autowired
	@Qualifier("consumatoreCoda")
	private ConsumatoreCoda consumer;

	@Autowired
	@Qualifier("openTabs")
	private ReadModel openTabs;

	public MessageDispatcher() {
	}

	@PostConstruct
	public void init() {
		consumer.subscribe(this);
	}

	@Override
	public void onEvent(Event event) {
		openTabs.apply(event);
	}

}
