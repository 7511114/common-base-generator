package com.istock.base.mybatis.executor;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.util.ReflectionUtils;

public class MybatisBatchExecutor extends BatchExecutor {

	
	public MybatisBatchExecutor(Configuration configuration, Transaction transaction) {
		super(configuration, transaction);
	}

	private CommonExecutor commonExecutor;
	public void setCommonExecutor(CommonExecutor commonExecutor){
		this.commonExecutor = commonExecutor;
	}
	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,CacheKey key, BoundSql boundSql) throws SQLException {
		if(commonExecutor != null){
			String targetSql = commonExecutor.queryString(rowBounds, boundSql);
			Field field = ReflectionUtils.findField(BoundSql.class, "sql");
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, boundSql, targetSql);
		}
		return super.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
	}
	
}
