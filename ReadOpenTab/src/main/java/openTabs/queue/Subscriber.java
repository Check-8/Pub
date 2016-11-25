package openTabs.queue;

import openTabs.events.Event;

public interface Subscriber {

	public void onEvent(Event event);
}
