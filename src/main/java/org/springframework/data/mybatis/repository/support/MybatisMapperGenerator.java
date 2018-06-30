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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mybatis.mapping.MybatisPersistentEntity;
import org.springframework.data.mybatis.mapping.MybatisPersistentProperty;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.util.StringUtils;

import static org.springframework.data.repository.query.parser.Part.IgnoreCaseType.*;
import static org.springframework.data.repository.query.parser.Part.Type.*;

/**
 * @author Jarvis Song
 */
public class MybatisMapperGenerator {


    private final Dialect                    dialect;
    private final MybatisPersistentEntity<?> persistentEntity;

    public MybatisMapperGenerator(Dialect dialect, MybatisPersistentEntity<?> persistentEntity) {

        this.dialect = dialect;
        this.persistentEntity = persistentEntity;
    }

    public String buildConditionCaluse(Type type, IgnoreCaseType ignoreCaseType, String[] properties) {
        StringBuilder builder = new StringBuilder();
        switch (type) {
        	case BETWEEN:
        		builder.append(" #{" + properties[0] + "} and #{" + properties[1] + "}");
        		break;
        	case LIKE:
        	case NOT_LIKE:
            case CONTAINING:
            case NOT_CONTAINING:
                if (ignoreCaseType == ALWAYS || ignoreCaseType == WHEN_POSSIBLE) {
                    builder.append("concat('%',upper(#{" + properties[0] + "}),'%')");
                } else {
                    builder.append("concat('%',#{" + properties[0] + "},'%')");
                }
                break;
            case STARTING_WITH:
                if (ignoreCaseType == ALWAYS || ignoreCaseType == WHEN_POSSIBLE) {
                    builder.append("concat(upper(#{" + properties[0] + "}),'%')");
                } else {
                    builder.append("concat(#{" + properties[0] + "},'%')");
                }
                break;
            case ENDING_WITH:
                if (ignoreCaseType == ALWAYS || ignoreCaseType == WHEN_POSSIBLE) {
                    builder.append("concat('%',upper(#{" + properties[0] + "}))");
                } else {
                    builder.append("concat('%',#{" + properties[0] + "})");
                }
                break;
            case IN:
            case NOT_IN:
                builder.append("<foreach item=\"item\" index=\"index\" collection=\"" + properties[0] + "\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>");
                break;
            case IS_NOT_NULL:
                builder.append(" is not null");
                break;
            case IS_NULL:
                builder.append(" is null");
                break;

            case IS_EMPTY:
            case IS_NOT_EMPTY:
            	builder.append("''");
            	break;
            case TRUE:
                builder.append("=true");
                break;
            case FALSE:
                builder.append("=false");
                break;
            default:
                if (ignoreCaseType == ALWAYS || ignoreCaseType == WHEN_POSSIBLE) {
                    builder.append("upper(#{" + properties[0] + "})");
                } else {
                    builder.append("#{" + properties[0] + "}");
                }
                break;
        }

        return builder.toString();
    }

    public String buildConditionOperate(Type type) {
        StringBuilder builder = new StringBuilder();
        switch (type) {
        	case BETWEEN:
        		builder.append(" between");
        		break;
        	case IS_EMPTY:
            case SIMPLE_PROPERTY:
                builder.append("=");
                break;
            case IS_NOT_EMPTY:    
            case NEGATING_SIMPLE_PROPERTY:
                builder.append("<![CDATA[<>]]>");
                break;
            case LESS_THAN:
            case BEFORE:
                builder.append("<![CDATA[<]]>");
                break;
            case LESS_THAN_EQUAL:
                builder.append("<![CDATA[<=]]>");
                break;
            case GREATER_THAN:
            case AFTER:
                builder.append("<![CDATA[>]]>");
                break;
            case GREATER_THAN_EQUAL:
                builder.append("<![CDATA[>=]]>");
                break;

            case LIKE:
            case NOT_LIKE:
            case STARTING_WITH:
            case ENDING_WITH:
                if (type == NOT_LIKE) {
                    builder.append(" not");
                }
                builder.append(" like ");
                break;
            case CONTAINING:
            case NOT_CONTAINING:
                if (type == NOT_CONTAINING) {
                    builder.append(" not");
                }
                builder.append(" like ");
                break;
            case IN:
            case NOT_IN:
                if (type == NOT_IN) {
                    builder.append(" not");
                }
                builder.append(" in ");
                break;

        }
        return builder.toString();
    }

    public String buildSelectColumns() {
        final StringBuilder builder = new StringBuilder();

        persistentEntity.doWithProperties(new SimplePropertyHandler() {
            @Override
            public void doWithPersistentProperty(PersistentProperty<?> pp) {
            	MybatisPersistentProperty mpp = (MybatisPersistentProperty) pp;
                builder.append(quota(persistentEntity.getEntityName()) + "." + dialect.wrapColumnName(mpp.getColumnName())).append(" as ").append(quota(pp.getName())).append(",");
            }
        });

        if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }


    public String buildFrom() {
        StringBuilder builder = new StringBuilder();
        builder.append(dialect.wrapTableName(persistentEntity.getTableName())).append(" ").append(quota(persistentEntity.getEntityName()));
        return builder.toString();
    }

 

    private String quota(String alias) {
        return dialect.openQuote() + alias + dialect.closeQuote();
    }

    public String buildSorts(Sort sort) {
        StringBuilder builder = new StringBuilder();

		if (null != sort && Sort.unsorted() != sort) {
            Map<String, String> map = new HashMap<String, String>();
            String[] arr = buildSelectColumns().split(",");
            for (String s : arr) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String[] ss = s.split(" as ");
                String key = ss[ss.length - 1];
                String val = ss[0];
                key = key.replace(String.valueOf(dialect.openQuote()), "").replace(String.valueOf(dialect.closeQuote()), "");
                map.put(key, val);
            }

            builder.append(" order by ");
            for (Iterator<Sort.Order> iterator = sort.iterator(); iterator.hasNext(); ) {
                Sort.Order order = iterator.next();
                String p = map.get(order.getProperty());
                builder.append((StringUtils.isEmpty(p) ? order.getProperty() : p) + " " + order.getDirection().name() + ",");
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
        } else {
            builder.append("<if test=\"_sorts != null\">");
            builder.append("<bind name=\"_columnsMap\" value='#{");
            String[] arr = buildSelectColumns().split(",");
            for (String s : arr) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String[] ss = s.split(" as ");
                String key = ss[ss.length - 1];
                String val = ss[0];
                key = key.replace(String.valueOf(dialect.openQuote()), "").replace(String.valueOf(dialect.closeQuote()), "");
                val = val.replace("\"", "\\\"");
                builder.append(String.format("\"%s\":\"%s\",", key, val));
            }
            if (builder.charAt(builder.length() - 1) == ',') {
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.append("}' />");
            builder.append(" order by ");
            builder.append("<foreach item=\"item\" index=\"idx\" collection=\"_sorts\" open=\"\" separator=\",\" close=\"\">");
            builder.append("<if test=\"item.ignoreCase\">lower(</if>").append("${_columnsMap[item.property]}").append("<if test=\"item.ignoreCase\">)</if>").append(" ${item.direction}");
            builder.append("</foreach>");
            builder.append("</if>");
        }
        return builder.toString();
    }
}
