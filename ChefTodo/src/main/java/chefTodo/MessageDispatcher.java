package chefTodo;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import chefTodo.events.Event;
import chefTodo.queue.ConsumatoreCoda;
import chefTodo.queue.Subscriber;
import chefTodo.readModel.ReadModel;

@Component
@Qualifier("messageDispatcher")
public class MessageDispatcher implements Subscriber {
	@Autowired
	@Qualifier("consumatoreCoda")
	private ConsumatoreCoda consumer;

	@Autowired
	@Qualifier("chefTodo")
	private ReadModel chefTodo;

	public MessageDispatcher() {
	}

	@PostConstruct
	public void init() {
		consumer.subscribe(this);
	}

	@Override
	public void onEvent(Event event) {
		chefTodo.apply(event);
	}

}
