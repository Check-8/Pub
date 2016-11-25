package tab;

import java.util.HashMap;
import java.util.Map;

import tab.aggregate.TabAggregate;
import tab.aggregate.TabAggregateMem;

public class TabAggregatesMap implements TabAggregates {
	private Map<Long, TabAggregateMem> aggregates;

	public TabAggregatesMap() {
		aggregates = new HashMap<>();
	}

	@Override
	public TabAggregate getAggregate(long id) {
		synchronized (aggregates) {
			TabAggregateMem tab = aggregates.get(id);
			if (tab == null) {
				tab = new TabAggregateMem();
				aggregates.put(id, tab);
			}
			return tab;
		}
	}
}
