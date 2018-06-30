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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Jarvis Song
 */
public class DialectFactoryBean implements FactoryBean<Dialect>, InitializingBean {

    private Dialect           dialect;
    private SqlSessionFactory sqlSessionFactory;
    public static final int NO_VERSION = -9999;

    @Override
    public Dialect getObject() throws Exception {

        if (null == dialect) {
            afterPropertiesSet();
        }
        return dialect;
    }

    @Override
    public Class<?> getObjectType() {
        return null == dialect ? Dialect.class : dialect.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(sqlSessionFactory, "SqlSessionFactory must not be null!");

        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();

            this.dialect = getDialect(metaData);

        } finally {
            if (null != conn) {
                conn.close();
            }
        }
    }

    private Dialect getDialect(DatabaseMetaData metaData) throws SQLException {
        final String databaseName = metaData.getDatabaseProductName();
        if ("H2".equals(databaseName)) {
            return new H2Dialect();
        }
        if ("MySQL".equals(databaseName)) {
//            if (majorVersion >= 5 ) {
//                return new MySQL5Dialect();
//            }
            return new MySQLDialect();
        }
        if (databaseName.startsWith("Microsoft SQL Server")) {
            return new SQLServerDialect();
        }
        if ("Oracle".equals(databaseName)) {
            return new OracleDialect();
        }

        if ("PostgreSQL".equals(databaseName)) {
            return new PostgreSQLDialect();
        }
        return null;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
