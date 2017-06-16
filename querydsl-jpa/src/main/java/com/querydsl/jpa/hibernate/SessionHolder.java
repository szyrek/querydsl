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

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SharedSessionContract;

import com.google.common.reflect.Invokable;

/**
 * Abstraction for different Hibernate Session signatures
 *
 * @author tiwe
 *
 */
public interface SessionHolder {

    /**
     * Workaround for Hibernate 5.2's return type changes.
     *
     * Because we can't have the return type in source code form, we have to use reflection.
     * This means that the method lookup happens just by name.
     */
    public static class MethodHack {

        public static final Invokable<SharedSessionContract, Query> createQuery = hackMethod(Query.class, SharedSessionContract.class, "createQuery");
        public static final Invokable<SharedSessionContract, SQLQuery> createSqlQuery = hackMethod(SQLQuery.class, SharedSessionContract.class, "createSQLQuery");

        @SuppressWarnings("unchecked") // carefully coded
        private static <T, R> Invokable<T, R> hackMethod(Class<R> returnType, Class<T> declaringType, String methodName) {
            try {
                return (Invokable<T, R>) Invokable.from(declaringType.getMethod(methodName, String.class))
                        .returning(returnType); // not totally necessary, but just a sanity check to fail fast
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Create a JPQL query for the given query string
     *
     * @param queryString JPQL query string
     * @return query
     */
    Query createQuery(String queryString);

    /**
     * Create an SQL query for the given query string
     *
     * @param queryString JPQL query string
     * @return query
     */
    SQLQuery createSQLQuery(String queryString);

}
