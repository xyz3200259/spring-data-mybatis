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
 * An SQL dialect for H2.
 *
 * @author Jarvis Song
 */
public class H2Dialect extends Dialect {

	  public H2Dialect() {
	        super();

	        registerColumnType(Types.BOOLEAN, "boolean");
	        registerColumnType(Types.BIGINT, "bigint");
	        registerColumnType(Types.BINARY, "binary");
	        registerColumnType(Types.BIT, "boolean");
	        registerColumnType(Types.CHAR, "char($l)");
	        registerColumnType(Types.DATE, "date");
	        registerColumnType(Types.DECIMAL, "decimal($p,$s)");
	        registerColumnType(Types.NUMERIC, "decimal($p,$s)");
	        registerColumnType(Types.DOUBLE, "double");
	        registerColumnType(Types.FLOAT, "float");
	        registerColumnType(Types.INTEGER, "integer");
	        registerColumnType(Types.LONGVARBINARY, "longvarbinary");
	        // H2 does define "longvarchar", but it is a simple alias to "varchar"
	        registerColumnType(Types.LONGVARCHAR, String.format("varchar(%d)", Integer.MAX_VALUE));
	        registerColumnType(Types.REAL, "real");
	        registerColumnType(Types.SMALLINT, "smallint");
	        registerColumnType(Types.TINYINT, "tinyint");
	        registerColumnType(Types.TIME, "time");
	        registerColumnType(Types.TIMESTAMP, "timestamp");
	        registerColumnType(Types.VARCHAR, "varchar($l)");
	        registerColumnType(Types.VARBINARY, "binary($l)");
	        registerColumnType(Types.BLOB, "blob");
	        registerColumnType(Types.CLOB, "clob");
	    }
	
    private static final AbstractLimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {
        @Override
        public boolean supportsLimit() {
            return true;
        }

        @Override
        public boolean bindLimitParametersInReverseOrder() {
            return true;
        }


        @Override
        public String processSql(boolean hasFirstRow, String columns, String from, String condition, String sorts) {
            String sql = "select " + columns + from + condition + sorts;
            return sql + (hasFirstRow ? " limit #{pageSize} offset #{offset}" : " limit #{pageSize}");
        }
    };

    @Override
    public LimitHandler getLimitHandler() {
        return LIMIT_HANDLER;
    }
}
