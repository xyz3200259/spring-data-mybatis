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

package org.springframework.data.mybatis.repository.support;

import java.io.Serializable;

import javax.persistence.Entity;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mybatis.mapping.MybatisPersistentProperty;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.StringUtils;

/**
 * mybatis meta.
 *
 * @author Jarvis Song
 */
public class MybatisMetamodelEntityInformation<T, ID extends Serializable>
		extends MybatisEntityInformationSupport<T, ID> implements MybatisEntityInformation<T, ID> {

	private PersistentEntity<T, MybatisPersistentProperty> persistentEntity;
	
	/**
	 * Creates a new {@link AbstractEntityInformation} from the given domain class.
	 *
	 * @param domainClass must not be {@literal null}.
	 */
	protected MybatisMetamodelEntityInformation(PersistentEntity<T, MybatisPersistentProperty> persistentEntity, Class<T> domainClass) {
		super(persistentEntity, domainClass);
		this.persistentEntity = persistentEntity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID getId(T entity) {
		if (null == persistentEntity) {
			return null;
		}

		return (ID) persistentEntity.getIdentifierAccessor(entity).getIdentifier();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ID> getIdType() {
		if (null == persistentEntity) {
			return null;
		}

		PersistentProperty<?> idProperty = persistentEntity.getIdProperty();
		if (null == idProperty) {
			return null;
		}
		return (Class<ID>) idProperty.getType();
	}

    @Override
    public String getEntityName() {
        Class<T> domainClass = getJavaType();
        Entity entity = domainClass.getAnnotation(Entity.class);
        if (null != entity && StringUtils.hasText(entity.name())) {
            return entity.name();
        }

        return domainClass.getSimpleName();
    }

	@Override
	public PersistentEntity<T, MybatisPersistentProperty> getPersistentEntity() {
		return persistentEntity;
	}
}
