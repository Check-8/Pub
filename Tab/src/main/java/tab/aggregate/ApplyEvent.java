package tab.aggregate;

import tab.events.Event;

public interface ApplyEvent {
    boolean applies(Event event);

    void apply(Event event);
}
