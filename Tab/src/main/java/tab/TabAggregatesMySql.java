package tab;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import tab.aggregate.TabAggregate;
import tab.aggregate.TabAggregateMySql;

public class TabAggregatesMySql implements TabAggregates {

	@Autowired
	private DataSource dataSource;

	private TabAggregateMySql instance;

	public TabAggregatesMySql() {
	}

	@PostConstruct
	public void init() {
		instance = new TabAggregateMySql();
		instance.setDataSource(dataSource);
	}

	@Override
	public TabAggregate getAggregate(long id) {
		return instance;
	}

}
