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

package org.springframework.data.mybatis.repository.support;

import org.springframework.data.mybatis.mapping.MybatisPersistentEntity;
import org.springframework.data.mybatis.mapping.MybatisPersistentProperty;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.Part.Type;

/**
 * SQL 生成器生来生成  'findByExample' 风格的 SQL.
 * @author 7cat
 * @since 1.0
 */
public class ExampleGenerator {
	
	private MybatisMapperGenerator generator;
	
	private Dialect dialect;
	
	private MybatisPersistentEntity<?> persistentEntity;
	
	public ExampleGenerator(MybatisMapperGenerator generator,Dialect dialect,MybatisPersistentEntity<?> persistentEntity) {
		this.generator =generator;
		this.dialect = dialect;
		this.persistentEntity = persistentEntity;
	}
	
	public String generate(MybatisPersistentProperty property) {
		StringBuilder builder = new StringBuilder();
		String columnName = dialect.wrapColumnName(property.getColumnName());
    	String alias= persistentEntity.getEntityName();
		if(property.getActualType().equals(String.class)) {
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value==null  and _example."+property.getName()+".includeNull\">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.IS_NULL));
    		builder.append(generator.buildConditionCaluse(Type.IS_NULL, null, null));
    		builder.append("</if>");
    		
    		// EXTRACT
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='EXACT' and _example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append("upper(").append(quota(dialect,alias)).append(".").append(columnName).append(")");
    		builder.append(generator.buildConditionOperate(Type.SIMPLE_PROPERTY));
    		builder.append(generator.buildConditionCaluse(Type.SIMPLE_PROPERTY, IgnoreCaseType.WHEN_POSSIBLE, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='EXACT' and !_example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.SIMPLE_PROPERTY));
    		builder.append(generator.buildConditionCaluse(Type.SIMPLE_PROPERTY, IgnoreCaseType.NEVER, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		// CONTAINING
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='CONTAINING' and _example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append("upper(").append(quota(dialect,alias)).append(".").append(columnName).append(")");
    		builder.append(generator.buildConditionOperate(Type.CONTAINING));
    		builder.append(generator.buildConditionCaluse(Type.CONTAINING, IgnoreCaseType.WHEN_POSSIBLE, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='CONTAINING' and !_example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.CONTAINING));
    		builder.append(generator.buildConditionCaluse(Type.CONTAINING, IgnoreCaseType.NEVER, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		// STARTING_WITH
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='STARTING' and _example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append("upper(").append(quota(dialect,alias)).append(".").append(columnName).append(")");
    		builder.append(generator.buildConditionOperate(Type.STARTING_WITH));
    		builder.append(generator.buildConditionCaluse(Type.STARTING_WITH, IgnoreCaseType.WHEN_POSSIBLE, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='STARTING' and !_example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.STARTING_WITH));
    		builder.append(generator.buildConditionCaluse(Type.STARTING_WITH, IgnoreCaseType.NEVER, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		// ENDING_WITH
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='ENDING' and _example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append("upper(").append(quota(dialect,alias)).append(".").append(columnName).append(")");
    		builder.append(generator.buildConditionOperate(Type.ENDING_WITH));
    		builder.append(generator.buildConditionCaluse(Type.ENDING_WITH, IgnoreCaseType.WHEN_POSSIBLE, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    		
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='ENDING' and !_example."+property.getName()+".ignoreCase \">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.ENDING_WITH));
    		builder.append(generator.buildConditionCaluse(Type.ENDING_WITH, IgnoreCaseType.NEVER, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    	} else {
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value==null  and _example."+property.getName()+".includeNull\">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.IS_NULL));
    		builder.append(generator.buildConditionCaluse(Type.IS_NULL, null, null));
    		builder.append("</if>");
    		
    		// EXTRACT
    		builder.append("<if test=\"_example."+property.getName()+" != null and _example."+property.getName()+".value!=null  and _example."+property.getName()+".matcher=='EXACT' \">");
    		builder.append(" and ").append(quota(dialect,alias)).append(".").append(columnName);
    		builder.append(generator.buildConditionOperate(Type.SIMPLE_PROPERTY));
    		builder.append(generator.buildConditionCaluse(Type.SIMPLE_PROPERTY, IgnoreCaseType.NEVER, new String [] {"_example."+property.getName()+".value"}));
    		builder.append("</if>");
    	}
		return builder.toString();
	}
	
	private String quota(Dialect dialect,String alias) {
		return dialect.openQuote() + alias + dialect.closeQuote();
	}
}
