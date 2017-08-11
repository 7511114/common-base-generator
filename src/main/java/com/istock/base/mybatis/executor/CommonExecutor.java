package com.istock.base.mybatis.executor;

import java.sql.SQLException;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.istock.base.ibatis.Dialect;

public final class CommonExecutor {

	private Dialect dialect;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public CommonExecutor(Dialect dialect){
		this.dialect = dialect;
	}
	
	public void setDialect(Dialect dialect){
		this.dialect = dialect;
	}

	public String queryString(RowBounds rowBounds, BoundSql boundSql) throws SQLException {
		if(dialect != null && rowBounds != null && rowBounds.getLimit() != RowBounds.NO_ROW_LIMIT){
			return dialect.getLimitString(boundSql.getSql(), rowBounds.getOffset(), rowBounds.getLimit());
		}
		return boundSql.getSql();
	}
}
