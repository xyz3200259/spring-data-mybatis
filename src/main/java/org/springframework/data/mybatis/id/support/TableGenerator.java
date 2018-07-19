/*
 * Copyright (c) 2018-present the original author or authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.springframework.data.mybatis.id.support;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mybatis.id.IdentityGenerator;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.mybatis.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * 实现了 {@link javax.persistence.TableGenerator}.
 * 
 * @author 7cat
 * @since 1.0
 */
public class TableGenerator implements IdentityGenerator<Serializable>, InitializingBean {

	private Log logger = LogFactory.getLog(TableGenerator.class);

	private ConcurrentHashMap<String, GenerationState> generationStateMapping = new ConcurrentHashMap<>();

	/**
	 * The default {@link #SEGMENT_LENGTH_PARAM} value
	 */
	public static final int DEF_SEGMENT_LENGTH = 255;
	
	private String tableName;

	private String segmentColumnName;

	private int segmentValueLength = DEF_SEGMENT_LENGTH;

	private String valueColumnName;

	private int initialValue;

	private int incrementSize;

	private DataSource dataSource;

	private Dialect dialect;

	private JdbcTemplate template;

	public TableGenerator(DataSource dataSource, Dialect dialect, TableGeneratorConfig tableGeneratorConfig) {
		this.dataSource = dataSource;
		this.dialect = dialect;
		DelegatingDataSource ds = new DelegatingDataSource();
		ds.setTargetDataSource(dataSource);
		// 避免受到事务传播性影响
		this.template = new JdbcTemplate(ds);
		if (null != tableGeneratorConfig) {
			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.getTable())) {
				if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.getSchema())) {
					this.tableName = StringUtils.qualify(tableGeneratorConfig.getSchema(), tableGeneratorConfig.getTable());
				} else if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.getCatalog())) {
					this.tableName = StringUtils.qualify(tableGeneratorConfig.getSchema(), tableGeneratorConfig.getCatalog());
				} else {
					this.tableName = tableGeneratorConfig.getTable();
				}
			}

			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.getPkColumnName())) {
				segmentColumnName = tableGeneratorConfig.getPkColumnName();
			}

			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.getValueColumnName())) {
				valueColumnName = tableGeneratorConfig.getValueColumnName();
			}
			if (tableGeneratorConfig.getInitialValue() > 0) {
				this.initialValue = tableGeneratorConfig.getInitialValue();
			}
			this.incrementSize = tableGeneratorConfig.getAllocationSize();
		}
	}

	@Override
	public Serializable generate(PersistentProperty<?> persistentProperty) {
		javax.persistence.TableGenerator tableGeneratorConfig = persistentProperty
				.findAnnotation(javax.persistence.TableGenerator.class);

		String segment = StringUtils.qualify(persistentProperty.getOwner().getName(), persistentProperty.getName());

		String tableName = this.tableName;

		String segmentColumnName = this.segmentColumnName;

		String valueColumnName = this.valueColumnName;

		int initialValue = this.initialValue;

		int incrementSize = this.incrementSize;

		if (null != tableGeneratorConfig) {
			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.pkColumnValue())) {
				segment = tableGeneratorConfig.pkColumnValue();
			}
			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.table())) {
				if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.schema())) {
					tableName = StringUtils.qualify(tableGeneratorConfig.schema(), tableGeneratorConfig.table());
				} else if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.catalog())) {
					tableName = StringUtils.qualify(tableGeneratorConfig.schema(), tableGeneratorConfig.catalog());
				} else {
					tableName = tableGeneratorConfig.table();
				}
			}

			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.pkColumnName())) {
				segmentColumnName = tableGeneratorConfig.pkColumnName();
			}

			if (!org.springframework.util.StringUtils.isEmpty(tableGeneratorConfig.valueColumnName())) {
				valueColumnName = tableGeneratorConfig.valueColumnName();
			}
			if (tableGeneratorConfig.initialValue() > 0) {
				initialValue = tableGeneratorConfig.initialValue();
			}
			incrementSize = tableGeneratorConfig.allocationSize();
		}

		// 如果不存在 GenerateState 则创建一个
		if (!generationStateMapping.containsKey(segment)) {
			generationStateMapping.putIfAbsent(segment, new GenerationState());
		}

		GenerationState state = generationStateMapping.get(segment);

		synchronized (state) {
			if (state.value.get() < state.upperLimitValue.get()) {
				return state.value.getAndIncrement();
			} else {
				int rows = 0;
				do {
					Long value = doQueryValue(tableName, valueColumnName, segmentColumnName, segment);
					if (null == value) {
						try {
							template.update(buildInsertQuery(tableName, valueColumnName, segmentColumnName), segment, initialValue);
							value = new Long(initialValue);
						} catch (DuplicateKeyException e) {
							// ignore
						}
					}

					rows = template.update(buildUpdateQuery(tableName, valueColumnName, segmentColumnName), value + incrementSize,
							value, segment);

					if (rows > 0) {
						state.value = new AtomicLong(value);
						state.upperLimitValue = new AtomicLong(value + incrementSize);
						return state.value.getAndIncrement();
					} else {
						continue;
					}
				} while (rows == 0);
			}
			throw new IdentityGenerationException("Generate identity fail.");
		}

	}

	private Long doQueryValue(String tableName, String valueColumnName, String segmentColumnName, String segment) {
		try {
			return template.queryForObject(buildSelectQuery(tableName, valueColumnName, segmentColumnName),
					new Object[] { segment }, Long.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private String buildSelectQuery(String tableName, String valueColumnName, String segmentColumnName) {
		final String alias = "tbl";
		return "select " + StringUtils.qualify(alias, valueColumnName) + " from " + tableName + ' ' + alias + " where "
				+ StringUtils.qualify(alias, segmentColumnName) + "=?";
	}

	private String buildUpdateQuery(String tableName, String valueColumnName, String segmentColumnName) {
		return "update " + tableName + " set " + valueColumnName + "=? " + " where " + valueColumnName + "=? and "
				+ segmentColumnName + "=?";
	}

	private String buildInsertQuery(String tableName, String valueColumnName, String segmentColumnName) {
		return "insert into " + tableName + " (" + segmentColumnName + ", " + valueColumnName + ") " + " values (?,?)";
	}

	private String sqlCreateStrings() {
		return "create table " + tableName + " ( " + segmentColumnName + ' '
				+ dialect.getTypeName(Types.VARCHAR, segmentValueLength, 0, 0) + " not null " + ", " + valueColumnName + ' '
				+ dialect.getTypeName(Types.BIGINT) + ", primary key ( " + segmentColumnName + " ) )";
	}

	private static class GenerationState {
		// the current generator value
		private AtomicLong value = new AtomicLong();
		// the value at which we'll hit the db again
		private AtomicLong upperLimitValue = new AtomicLong();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			boolean result = (boolean) JdbcUtils.extractDatabaseMetaData(dataSource, (DatabaseMetaData action) -> {
				ResultSet rs = null;
				try {
					// 使用 DatabaseMetaData 查找 table
					rs = action.getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" });
					if (rs.next()) {
						return true;
					}
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
				return false;
			});

			if (!result) {
				try {
					// 尝试直接执行查询
					template.execute("select count(*) from " + tableName);
				} catch (Exception e1) {
					logger.debug("Init Sequence table " + tableName + " .");
					try {
						// 尝试新建表
						template.execute(sqlCreateStrings());
					} catch (Exception e2) {
						throw new IdentityGenerationException("Create Sequence table fail: ", e2);
					}
				}
			}
		} catch (MetaDataAccessException e) {
			throw new IdentityGenerationException("Init Sequence Table fail:", e);
		}
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setSegmentColumnName(String segmentColumnName) {
		this.segmentColumnName = segmentColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public void setIncrementSize(int incrementSize) {
		this.incrementSize = incrementSize;
	}
}
