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

import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mybatis.mapping.MybatisMappingContext;
import org.springframework.data.mybatis.mapping.MybatisPersistentProperty;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.Assert;

/**
 * mybatis entity information support.
 *
 * @author Jarvis Song
 */
public abstract class MybatisEntityInformationSupport<T, ID extends Serializable> extends AbstractEntityInformation<T, ID>{


    protected final PersistentEntity<T, ?> persistentEntity;

    /**
     * Creates a new {@link MybatisEntityInformationSupport} from the given domain class.
     *
     * @param auditDateAware
     * @param domainClass    must not be {@literal null}.
     */
    protected MybatisEntityInformationSupport(PersistentEntity<T, ?> persistentEntity,Class<T> domainClass) {
        super(domainClass);
        this.persistentEntity = persistentEntity;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T, ID extends Serializable> MybatisEntityInformation<T, ID> getEntityInformation(MybatisMappingContext mappingContext,
                                                                                                    Class<T> domainClass) {
        Assert.notNull(domainClass,"DomainClass must not be null!");
        PersistentEntity<T, MybatisPersistentProperty> persistentEntity = (PersistentEntity<T, MybatisPersistentProperty>) mappingContext.getPersistentEntity(domainClass);
        if (Persistable.class.isAssignableFrom(domainClass)) {
            return new MybatisPersistableEntityInformation(persistentEntity, domainClass);
        }

        return new MybatisMetamodelEntityInformation<T, ID>(persistentEntity, domainClass);
    }

}
