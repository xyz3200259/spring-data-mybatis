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

import org.springframework.data.mapping.MappingException;
import org.springframework.data.mybatis.repository.dialect.pagination.LimitHandler;

/**
 * Database localism.
 *
 * @author Jarvis Song
 */
public class Dialect {

	public static final String QUOTE = "`\"[";

	public static final String CLOSED_QUOTE = "`\"]";

	private TypeNames typeNames = new TypeNames();

	public Dialect() {
		registerColumnType(Types.BIT, "bit");
		registerColumnType(Types.BOOLEAN, "boolean");
		registerColumnType(Types.TINYINT, "tinyint");
		registerColumnType(Types.SMALLINT, "smallint");
		registerColumnType(Types.INTEGER, "integer");
		registerColumnType(Types.BIGINT, "bigint");
		registerColumnType(Types.FLOAT, "float($p)");
		registerColumnType(Types.DOUBLE, "double precision");
		registerColumnType(Types.NUMERIC, "numeric($p,$s)");
		registerColumnType(Types.REAL, "real");

		registerColumnType(Types.DATE, "date");
		registerColumnType(Types.TIME, "time");
		registerColumnType(Types.TIMESTAMP, "timestamp");

		registerColumnType(Types.VARBINARY, "bit varying($l)");
		registerColumnType(Types.LONGVARBINARY, "bit varying($l)");
		registerColumnType(Types.BLOB, "blob");

		registerColumnType(Types.CHAR, "char($l)");
		registerColumnType(Types.VARCHAR, "varchar($l)");
		registerColumnType(Types.LONGVARCHAR, "varchar($l)");
		registerColumnType(Types.CLOB, "clob");

		registerColumnType(Types.NCHAR, "nchar($l)");
		registerColumnType(Types.NVARCHAR, "nvarchar($l)");
		registerColumnType(Types.LONGNVARCHAR, "nvarchar($l)");
		registerColumnType(Types.NCLOB, "nclob");
	}

	public LimitHandler getLimitHandler() {
		return null;
	}

	public char openQuote() {
		return '"';
	}

	public char closeQuote() {
		return '"';
	}

	public String quote(String name) {
		if (name == null) {
			return null;
		}

		if (name.charAt(0) == '`') {
			return openQuote() + name.substring(1, name.length() - 1) + closeQuote();
		}
		else {
			return name;
		}
	}

	public String wrapTableName(String tableName) {
		return tableName;
	}

	public String wrapColumnName(String columnName) {
		return columnName;
	}

	public boolean supportsDeleteAlias() {
		return false;
	}

	protected void registerColumnType(int code, String name) {
		typeNames.put(code, name);
	}

	protected void registerColumnType(int code, long capacity, String name) {
		typeNames.put(code, capacity, name);
	}

	/**
	 * Get the name of the database type associated with the given
	 * {@link java.sql.Types} typecode.
	 *
	 * @param code The {@link java.sql.Types} typecode
	 * @return the database type name
	 * @throws MappingException If no mapping was specified for that type.
	 */
	public String getTypeName(int code) throws MappingException {
		final String result = typeNames.get(code);
		if (result == null) {
			throw new MappingException("No default type mapping for (java.sql.Types) " + code);
		}
		return result;
	}

	/**
	 * Get the name of the database type associated with the given
	 * {@link java.sql.Types} typecode with the given storage specification
	 * parameters.
	 *
	 * @param code The {@link java.sql.Types} typecode
	 * @param length The datatype length
	 * @param precision The datatype precision
	 * @param scale The datatype scale
	 * @return the database type name
	 * @throws MappingException If no mapping was specified for that type.
	 */
	public String getTypeName(int code, long length, int precision, int scale) throws MappingException {
		final String result = typeNames.get(code, length, precision, scale);
		if (result == null) {
			throw new MappingException(
					String.format("No type mapping for java.sql.Types code: %s, length: %s", code, length));
		}
		return result;
	}
}
