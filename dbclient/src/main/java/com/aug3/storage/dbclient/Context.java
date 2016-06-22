package com.aug3.storage.dbclient;

import org.apache.log4j.Logger;

public class Context {

	private final static Logger log = Logger.getLogger(Context.class);

	private DBType dbType = DBType.MYSQL;

	private String dbKey;

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}

	public void setDbType(String dbType) {

		if (DBType.MYSQL.name().equalsIgnoreCase(dbType)) {
			this.dbType = DBType.MYSQL;
		} else if (DBType.ORACLE.name().equalsIgnoreCase(dbType)) {
			this.dbType = DBType.ORACLE;
		} else {
			log.info("Illegal db type defined : supported : mysql, oracle : use default mysql");
		}
	}

	public String getDbKey() {
		return dbKey;
	}

	public void setDbKey(String dbKey) {
		this.dbKey = dbKey;
	}

	public enum DBType {

		MYSQL, ORACLE;

	}

	@Override
	public String toString() {
		return dbType.name() + dbKey;
	}

	@Override
	public int hashCode() {
		return this.toString().toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Context))
			return false;
		return (this.toString().equalsIgnoreCase(obj.toString()));
	}

}
