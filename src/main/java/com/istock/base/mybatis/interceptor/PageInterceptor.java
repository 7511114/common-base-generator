package com.istock.base.mybatis.interceptor;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import com.istock.base.mybatis.executor.CommonExecutor;
import com.istock.base.mybatis.executor.MybatisBatchExecutor;
import com.istock.base.mybatis.executor.MybatisCachingExecutor;
import com.istock.base.mybatis.executor.MybatisReuseExecutor;
import com.istock.base.mybatis.executor.MybatisSimpleExecutor;

import net.sf.cglib.core.ReflectUtils;

@Intercepts({@Signature(
		type= Executor.class,
		method = "query",
		args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
				,CacheKey.class , BoundSql.class
				})})
public class PageInterceptor implements Interceptor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private CommonExecutor commonExecutor;
	
	public void setCommonExecutor(CommonExecutor commonExecutor){
		this.commonExecutor = commonExecutor;
	}
	
	public Object intercept(Invocation invocation) throws Throwable {
//		logger.info("==============intercept invoked:{}============" , invocation);
		
		/*RowBounds rowBounds = (RowBounds) invocation.getArgs()[3];
		if(dialect != null && rowBounds != null && rowBounds.getLimit() != RowBounds.NO_ROW_LIMIT){
			BoundSql boundSql = (BoundSql)invocation.getArgs()[5];
			
			MappedStatementWarp warp = new MappedStatementWarp(dialect , rowBounds);
			
			invocation.getArgs()[0] = warp.createProxy();
			
			String newSql = dialect.getLimitString(boundSql.getSql(), rowBounds.getOffset(), rowBounds.getLimit());
			
			Field field = ReflectionUtils.findField(BoundSql.class, "sql");
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, boundSql, newSql);
		}*/
	    return invocation.proceed();
	}

	public Object plugin(Object target) {
//		logger.info("==============plugin invoked:{}============" , target);
//		return Plugin.wrap(target, this);
//		return MybatisPlugin.wrap(target, this);
		
		Class clazz = target.getClass();
		if(Executor.class.isAssignableFrom(clazz)){
			/*MybatisPlugin plugin = new MybatisPlugin(target , this);
			
			try{
				Field transactionField = ReflectionUtils.findField(BaseExecutor.class, "transaction");
				ReflectionUtils.makeAccessible(transactionField);
				
				Field transactionField1 = ReflectionUtils.findField(MybatisPlugin.class, "transaction");
				ReflectionUtils.makeAccessible(transactionField1);
				ReflectionUtils.setField(transactionField1, plugin, transactionField.get(target));
				
				Field configurationField = ReflectionUtils.findField(BaseExecutor.class, "configuration");
				ReflectionUtils.makeAccessible(configurationField);
				Field configurationField1 = ReflectionUtils.findField(MybatisPlugin.class, "configuration");
				ReflectionUtils.makeAccessible(configurationField1);
				ReflectionUtils.setField(configurationField1, plugin, configurationField.get(target));
			}catch(Exception e){
				e.printStackTrace();
			}
			
			logger.info("=========reflact success==============");
			return plugin.wrap();*/
			try{
				if(Executor.class.isAssignableFrom(clazz)){
					Field transactionField = ReflectionUtils.findField(BaseExecutor.class, "transaction");
					ReflectionUtils.makeAccessible(transactionField);
					
					Field configurationField = ReflectionUtils.findField(BaseExecutor.class, "configuration");
					ReflectionUtils.makeAccessible(configurationField);
					if(target instanceof SimpleExecutor){
						MybatisSimpleExecutor result = (MybatisSimpleExecutor)ReflectUtils.newInstance(MybatisSimpleExecutor.class, new Class[]{Configuration.class,Transaction.class }, new Object[]{configurationField.get(target),transactionField.get(target)});
						result.setCommonExecutor(commonExecutor);
						BeanUtils.copyProperties(target, result);
						return result;
					}else if(target instanceof ReuseExecutor){
						MybatisReuseExecutor result = (MybatisReuseExecutor)ReflectUtils.newInstance(MybatisReuseExecutor.class, new Class[]{Configuration.class,Transaction.class }, new Object[]{configurationField.get(target),transactionField.get(target)});
						result.setCommonExecutor(commonExecutor);
						BeanUtils.copyProperties(target, result);
						return result;
					}else if(target instanceof BatchExecutor){
						MybatisBatchExecutor result = (MybatisBatchExecutor)ReflectUtils.newInstance(MybatisBatchExecutor.class, new Class[]{Configuration.class,Transaction.class }, new Object[]{configurationField.get(target),transactionField.get(target)});
						result.setCommonExecutor(commonExecutor);
						BeanUtils.copyProperties(target, result);
						return result;
					}else if(target instanceof CachingExecutor){
						Field delegateField = ReflectionUtils.findField(CachingExecutor.class, "delegate");
						ReflectionUtils.makeAccessible(delegateField);
						MybatisCachingExecutor result = (MybatisCachingExecutor)ReflectUtils.newInstance(MybatisCachingExecutor.class, new Class[]{Executor.class}, new Object[]{delegateField.get(target)});
						result.setCommonExecutor(commonExecutor);
						BeanUtils.copyProperties(target, result);
						return result;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
		/*logger.info("============setProperties invoked:{}==========" , properties);
		String dialectClass = properties.getProperty("dialect");
		try{
			Class clazz = Class.forName(dialectClass);
			if(Dialect.class.isAssignableFrom(clazz)){
				dialect = (Dialect)clazz.newInstance();
				commonExecutor = new CommonExecutor(dialect);
			}
		}catch(Exception e){
			e.printStackTrace();
		}*/
	}

}
