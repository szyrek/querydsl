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
package com.querydsl.jpa;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import javax.persistence.Query;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.Cursor;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.querydsl.core.types.FactoryExpression;

/**
 * {@code EclipseLinkHandler} is the {@link QueryHandler} implementation for EclipseLink
 *
 * @author tiwe
 *
 */
class EclipseLinkHandler implements QueryHandler {

  @Override
  public void addEntity(Query query, String alias, Class<?> type) {
    // do nothing
  }

  @Override
  public void addScalar(Query query, String alias, Class<?> type) {
    // do nothing
  }

  @Override
  public boolean createNativeQueryTyped() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> CloseableIterator<T> iterate(Query query, FactoryExpression<?> projection) {
    Iterator<T> iterator = null;
    Closeable closeable = null;
    if (query instanceof JpaQuery) {
      query.setHint(QueryHints.CURSOR, HintValues.TRUE);
      final Cursor cursor = (Cursor) query.getSingleResult();
      final int pageSize = cursor.getPageSize();
      closeable = new Closeable() {

        @Override
        public void close() throws IOException {
          cursor.close();
        }
      };
      iterator = new Iterator<T>() {

        private int rowsSinceLastClear = 0;

        @Override
        public boolean hasNext() {
          return cursor.hasNext();
        }

        @Override
        public T next() {
          if (rowsSinceLastClear++ == pageSize) {
            rowsSinceLastClear = 0;
            cursor.clear();
          }
          return (T) cursor.next();
        }
      };
    } else {
      iterator = query.getResultList().iterator();
    }
    if (projection != null) {
      return new TransformingIterator<T>(iterator, closeable, projection);
    } else {
      return new IteratorAdapter<T>(iterator, closeable);
    }
  }

  @Override
  public boolean transform(Query query, FactoryExpression<?> projection) {
    return false;
  }

  @Override
  public boolean wrapEntityProjections() {
    return false;
  }

}
