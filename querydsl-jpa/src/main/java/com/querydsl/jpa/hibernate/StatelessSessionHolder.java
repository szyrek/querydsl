/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.jpa.hibernate;

import static com.querydsl.jpa.hibernate.SessionHolder.MethodHack.hackMethod;

import java.lang.reflect.InvocationTargetException;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;

import com.google.common.reflect.Invokable;

/**
 * SessionHolder implementation using StatelessSession
 *
 * @author tiwe
 *
 */
public class StatelessSessionHolder implements SessionHolder {

    private static final Invokable<StatelessSession, Query> createQuery = hackMethod(Query.class, StatelessSession.class, "createQuery");
    private static final Invokable<StatelessSession, SQLQuery> createSqlQuery = hackMethod(SQLQuery.class, StatelessSession.class, "createSQLQuery");

    private final StatelessSession session;

    public StatelessSessionHolder(StatelessSession session) {
        this.session = session;
    }

    @Override
    public Query createQuery(String queryString) {
        try {
            return createQuery.invoke(session, queryString);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SQLQuery createSQLQuery(String queryString) {
        try {
            return createSqlQuery.invoke(session, queryString);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
