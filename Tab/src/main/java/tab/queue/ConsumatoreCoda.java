package tab.queue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsumatoreCoda {
	private List<Subscriber> subscribers;

	public ConsumatoreCoda() {
		subscribers = new CopyOnWriteArrayList<>();
	}

	public void subscribe(Subscriber subscriber) {
		subscribers.add(subscriber);
	}

	public List<Subscriber> getSubscriber() {
		return subscribers;
	}

}
