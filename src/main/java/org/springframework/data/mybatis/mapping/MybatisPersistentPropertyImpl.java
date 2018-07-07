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

package org.springframework.data.mybatis.mapping;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.ParsingUtils;
import org.springframework.util.StringUtils;

import static org.apache.ibatis.type.JdbcType.*;

/**
 * @author Jarvis Song
 */
public class MybatisPersistentPropertyImpl extends AnnotationBasedPersistentProperty<MybatisPersistentProperty>
		implements MybatisPersistentProperty {

	private static final Collection<Class<? extends Annotation>> ID_ANNOTATIONS;

	private static Map<Class<?>, JdbcType> javaTypesMappedToJdbcTypes = new HashMap<Class<?>, JdbcType>();

	static {
		javaTypesMappedToJdbcTypes.put(String.class, VARCHAR);
		javaTypesMappedToJdbcTypes.put(java.math.BigDecimal.class, NUMERIC);
		javaTypesMappedToJdbcTypes.put(boolean.class, BIT);
		javaTypesMappedToJdbcTypes.put(byte.class, TINYINT);
		javaTypesMappedToJdbcTypes.put(short.class, SMALLINT);
		javaTypesMappedToJdbcTypes.put(int.class, INTEGER);
		javaTypesMappedToJdbcTypes.put(long.class, BIGINT);
		javaTypesMappedToJdbcTypes.put(float.class, REAL);
		javaTypesMappedToJdbcTypes.put(double.class, DOUBLE);
		javaTypesMappedToJdbcTypes.put(byte[].class, VARBINARY);
		javaTypesMappedToJdbcTypes.put(java.util.Date.class, TIMESTAMP);
		javaTypesMappedToJdbcTypes.put(java.sql.Date.class, DATE);
		javaTypesMappedToJdbcTypes.put(java.sql.Time.class, TIME);
		javaTypesMappedToJdbcTypes.put(java.sql.Timestamp.class, TIMESTAMP);
		javaTypesMappedToJdbcTypes.put(Boolean.class, BIT);
		javaTypesMappedToJdbcTypes.put(Integer.class, INTEGER);
		javaTypesMappedToJdbcTypes.put(Long.class, BIGINT);
		javaTypesMappedToJdbcTypes.put(Float.class, REAL);
		javaTypesMappedToJdbcTypes.put(Double.class, DOUBLE);

		Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>();
		annotations.add(Id.class);
		annotations.add(EmbeddedId.class);
		ID_ANNOTATIONS = Collections.unmodifiableSet(annotations);
	}

	private final Lazy<Boolean> isIdProperty;

	public MybatisPersistentPropertyImpl(Property property, PersistentEntity<?, MybatisPersistentProperty> owner,
			SimpleTypeHolder simpleTypeHolder) {
		super(property, owner, simpleTypeHolder);
		this.isIdProperty = Lazy.of(() -> ID_ANNOTATIONS.stream().anyMatch(it -> isAnnotationPresent(it)));
	}

	@Override
	public boolean isIdProperty() {
		return isIdProperty.get();
	}

	@Override
	protected Association<MybatisPersistentProperty> createAssociation() {
		if (null != findAnnotation(Embedded.class)|| null!= findAnnotation(EmbeddedId.class)) {
			return new MybatisEmbeddedAssociation(this, null);
		}
		return null;
	}

	@Override
	public String getColumnName() {
		Column column = findAnnotation(Column.class);
		if (null != column && StringUtils.hasText(column.name())) {
			return column.name();
		}

		return ParsingUtils.reconcatenateCamelCase(getName(), "_");
	}

	/**
	 * Java Type	JDBC type
	 * String	VARCHAR or LONGVARCHAR
	 * java.math.BigDecimal	NUMERIC
	 * boolean	BIT
	 * byte	TINYINT
	 * short	SMALLINT
	 * int	INTEGER
	 * long	BIGINT
	 * float	REAL
	 * double	DOUBLE
	 * byte[]	VARBINARY or LONGVARBINARY
	 * java.sql.Date	DATE
	 * java.sql.Time	TIME
	 * java.sql.Timestamp	TIMESTAMP
	 * ----------------------------------------
	 * Java Object Type	JDBC Type
	 * String	VARCHAR or LONGVARCHAR
	 * java.math.BigDecimal	NUMERIC
	 * Boolean	BIT
	 * Integer	INTEGER
	 * Long	BIGINT
	 * Float	REAL
	 * Double	DOUBLE
	 * byte[]	VARBINARY or LONGVARBINARY
	 * java.sql.Date	DATE
	 * java.sql.Time	TIME
	 * java.sql.Timestamp	TIMESTAMP
	 *
	 * @return
	 */
	@Override
	public JdbcType getJdbcType() {

		org.springframework.data.mybatis.annotations.JdbcType jdbcType = findAnnotation(
				org.springframework.data.mybatis.annotations.JdbcType.class);
		if (null != jdbcType) {
			return jdbcType.value();
		}

		Class<?> type = getActualType();

		JdbcType t = javaTypesMappedToJdbcTypes.get(type);
		if (null != t) {
			return t;
		}

		return UNDEFINED;
	}

	@Override
	public Class<? extends TypeHandler<?>> getSpecifiedTypeHandler() {
		org.springframework.data.mybatis.annotations.TypeHandler typeHandler = findAnnotation(
				org.springframework.data.mybatis.annotations.TypeHandler.class);
		if (null != typeHandler) {
			return typeHandler.value();
		}
		return null;
	}
	
	@Override
	public boolean isAssociation() {
		return (null != findAnnotation(Embedded.class) || null != findAnnotation(EmbeddedId.class));
	}

	@Override
	public boolean isCompositeId() {
		return isIdProperty() && isEntity();
	}

	@Override
	public boolean isTransient() {
		if (isAnnotationPresent(Transient.class)) {
			return true;
		}
		else {
			return super.isTransient();
		}
	}
}
