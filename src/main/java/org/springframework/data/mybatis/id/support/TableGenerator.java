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
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mybatis.id.IdentityGenerator;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.mybatis.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * 实现了 {@link javax.persistence.TableGenerator}.
 * 
 * @author 7cat
 * @since 1.0
 */
public class TableGenerator implements IdentityGenerator<Serializable> {

	private Log logger = LogFactory.getLog(TableGenerator.class);

	/**
	 * The default {@link #TABLE_PARAM} value
	 */
	public static final String DEF_TABLE = "mybatis_sequences";

	/**
	 * The default {@link #VALUE_COLUMN_PARAM} value
	 */
	public static final String DEF_VALUE_COLUMN = "next_val";

	/**
	 * The default {@link #SEGMENT_COLUMN_PARAM} value
	 */
	public static final String DEF_SEGMENT_COLUMN = "sequence_name";

	/**
	 * The default {@link #SEGMENT_LENGTH_PARAM} value
	 */
	public static final int DEF_SEGMENT_LENGTH = 255;

	/**
	 * The default {@link #INITIAL_PARAM} value
	 */
	public static final int DEFAULT_INITIAL_VALUE = 1;

	/**
	 * Indicates the increment size to use. The default value is {@link #DEFAULT_INCREMENT_SIZE}
	 */
	public static final String INCREMENT_PARAM = "increment_size";

	/**
	 * The default {@link #INCREMENT_PARAM} value
	 */
	public static final int DEFAULT_INCREMENT_SIZE = 1;

	private String tableName = DEF_TABLE;

	private String segmentColumnName = DEF_SEGMENT_COLUMN;

	private int segmentValueLength = DEF_SEGMENT_LENGTH;

	private String valueColumnName = DEF_VALUE_COLUMN;

	private int initialValue = DEFAULT_INITIAL_VALUE;

	private int incrementSize = DEFAULT_INCREMENT_SIZE;

	private DataSource dataSource;

	private Dialect dialect;

	private JdbcTemplate template;

	public TableGenerator(DataSource dataSource, Dialect dialect) {
		this.dataSource = dataSource;
		this.dialect = dialect;
		// 避免受到事务传播性影响
		this.template = new JdbcTemplate(dataSource);
		initTable();
	}

	private void initTable() {
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
					logger.debug("Init Sequence table " + DEF_TABLE + " .");
					try {
						// 尝试新建表
						template.execute(sqlCreateStrings());
					} catch (Exception e2) {
						throw new MappingException("Create Sequence table fail: ", e2);
					}
				}
			}
		} catch (MetaDataAccessException e) {
			throw new MappingException("Init Sequence Table fail:", e);
		}
	}

	@Override
	public Serializable generate(PersistentProperty<?> persistentProperty) {

		int rows = 0;
		Long generatedValue = null;
		do {
			String segment = StringUtils.qualify(persistentProperty.getOwner().getName(), persistentProperty.getName());
			Long value = doQueryValue(segment);
			if (null == value) {
				template.update(buildInsertQuery(), segment, initialValue);
				value = new Long(initialValue);
			}
			AtomicLong seq = new AtomicLong(value);
			generatedValue = seq.getAndAdd(incrementSize);
			rows = template.update(buildUpdateQuery(), seq.get(), value, segment);
		} while (rows == 0);
		return generatedValue;
	}

	private Long doQueryValue(String segment) {
		try {
			return template.queryForObject(buildSelectQuery(), new Object[] { segment }, Long.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private String buildSelectQuery() {
		final String alias = "tbl";
		return "select " + StringUtils.qualify(alias, valueColumnName) + " from " + tableName + ' ' + alias + " where "
				+ StringUtils.qualify(alias, segmentColumnName) + "=?";
	}

	private String buildUpdateQuery() {
		return "update " + tableName + " set " + valueColumnName + "=? " + " where " + valueColumnName + "=? and "
				+ segmentColumnName + "=?";
	}

	private String buildInsertQuery() {
		return "insert into " + tableName + " (" + segmentColumnName + ", " + valueColumnName + ") " + " values (?,?)";
	}

	private String sqlCreateStrings() {
		return "create table " + tableName + " ( " + segmentColumnName + ' '
				+ dialect.getTypeName(Types.VARCHAR, segmentValueLength, 0, 0) + " not null " + ", " + valueColumnName + ' '
				+ dialect.getTypeName(Types.BIGINT) + ", primary key ( " + segmentColumnName + " ) )";
	}
}
