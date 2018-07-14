/*
 *
 *   Copyright 2016 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.springframework.data.mybatis.repository.dialect;

import java.sql.Types;

import org.springframework.data.mybatis.repository.dialect.pagination.AbstractLimitHandler;
import org.springframework.data.mybatis.repository.dialect.pagination.LimitHandler;

/**
 * An SQL dialect for DB2.
 * 
 * @author 7cat
 * @since 1.0
 */
public class DB2Dialect extends Dialect {

	private static final AbstractLimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {

		@Override
		public String processSql(String columns, String from, String condition, String sorts) {
			final StringBuilder pagingSelect = new StringBuilder();
			pagingSelect.append(
					"select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ");
			pagingSelect.append("select " + columns + from + condition + sorts);
			pagingSelect.append(
					" ) as inner2_ )  where rownumber_ <![CDATA[<=]]> #{offsetEnd} and rownumber_ <![CDATA[>]]> #{offset}");
			return pagingSelect.toString();
		}

		@Override
		public boolean supportsLimit() {
			return true;
		}

		@Override
		public boolean useMaxForLimit() {
			return true;
		}

		@Override
		public String processSql(String sql, int pageSize, long offset, long offsetEnd) {
			final StringBuilder pagingSelect = new StringBuilder();
			pagingSelect.append(
					"select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) as inner2_ )  where rownumber_ <= " + offsetEnd + "   and rownumber_ > " + offset);
			return pagingSelect.toString();
		}
	};

	public DB2Dialect() {
		super();
		registerColumnType(Types.BIT, "smallint");
		registerColumnType(Types.BIGINT, "bigint");
		registerColumnType(Types.SMALLINT, "smallint");
		registerColumnType(Types.TINYINT, "smallint");
		registerColumnType(Types.INTEGER, "integer");
		registerColumnType(Types.CHAR, "char(1)");
		registerColumnType(Types.VARCHAR, "varchar($l)");
		registerColumnType(Types.FLOAT, "float");
		registerColumnType(Types.DOUBLE, "double");
		registerColumnType(Types.DATE, "date");
		registerColumnType(Types.TIME, "time");
		registerColumnType(Types.TIMESTAMP, "timestamp");
		registerColumnType(Types.VARBINARY, "varchar($l) for bit data");
		registerColumnType(Types.NUMERIC, "numeric($p,$s)");
		registerColumnType(Types.BLOB, "blob($l)");
		registerColumnType(Types.CLOB, "clob($l)");
		registerColumnType(Types.LONGVARCHAR, "long varchar");
		registerColumnType(Types.LONGVARBINARY, "long varchar for bit data");
		registerColumnType(Types.BINARY, "varchar($l) for bit data");
		registerColumnType(Types.BINARY, 254, "char($l) for bit data");
		registerColumnType(Types.BOOLEAN, "smallint");
	}

	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	@Override
	public char closeQuote() {
		return '"';
	}

	@Override
	public char openQuote() {
		return '"';
	}

	@Override
	public boolean supportsDeleteAlias() {
		return false;
	}
}
