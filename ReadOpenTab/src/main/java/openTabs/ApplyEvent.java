package openTabs;

import openTabs.events.Event;

public interface ApplyEvent {

	boolean applies(Event event);

	void apply(Event event);
}
