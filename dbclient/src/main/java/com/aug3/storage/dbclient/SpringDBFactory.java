package com.aug3.storage.dbclient;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aug3.storage.dbclient.ds.DataSourceManager;

/**
 * This DBFactory is used to new a spring JdbcTemplate instance. do it like
 * this:
 * 
 * <code>
	<bean id="dbcontext" class="com.aug3.storage.dbclient.Context">
		<property />
		<property />
	</bean>
	
	<bean id="mysqlFactory" class="com.aug3.storage.dbclient.SpringDBFactory">
		<property name="context" ref="dbcontext" />
    </bean>
    
	<bean id="jdbcTemplate" factory-bean="mysqlFactory" factory-method="getDBTempldate" />
 * </code>
 * 
 * @author John.sun
 * 
 */
public class SpringDBFactory {

	private Context context;

	public SpringDBFactory() {
	}

	public JdbcTemplate getJDBCTempldate(Context ctx) {
		context = ctx;
		JdbcTemplate jdbcTemplate = new JdbcTemplate();

		jdbcTemplate.setDataSource(DataSourceManager.getDataSource(context));

		return jdbcTemplate;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
