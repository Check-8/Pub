package tab;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import tab.aggregate.TabAggregate;
import tab.aggregate.TabAggregateSharedMySql;

public class TabAggregatesSharedMySql implements TabAggregates {

	@Autowired
	private DataSource dataSource;

	private TabAggregateSharedMySql instance;

	public TabAggregatesSharedMySql() {
	}

	@PostConstruct
	public void init() {
		instance = new TabAggregateSharedMySql();
		instance.setDataSource(dataSource);
	}

	@Override
	public TabAggregate getAggregate(long id) {
		return instance;
	}

}
