/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package cn.rtomde.template.cursor.defaults;

import cn.rtomde.template.cursor.Cursor;
import cn.rtomde.template.executor.result.ResultContext;
import cn.rtomde.template.executor.result.ResultHandler;
import cn.rtomde.template.executor.resultset.DefaultResultSetHandler;
import cn.rtomde.template.executor.resultset.ResultSetWrapper;
import cn.rtomde.template.mapping.ResultMap;
import cn.rtomde.template.session.RowBounds;
import io.cloudevents.CloudEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is the default implementation of a MyBatis Cursor.
 * This implementation is not thread safe.
 */
public class DefaultCursor implements Cursor<CloudEvent> {

    // ResultSetHandler stuff
    private final DefaultResultSetHandler resultSetHandler;
    private final ResultMap resultMap;
    private final ResultSetWrapper rsw;
    private final RowBounds rowBounds;
    protected final ObjectWrapperResultHandler<CloudEvent> objectWrapperResultHandler = new ObjectWrapperResultHandler<>();

    private final CursorIterator cursorIterator = new CursorIterator();
    private boolean iteratorRetrieved;

    private CursorStatus status = CursorStatus.CREATED;
    private int indexWithRowBound = -1;

    private enum CursorStatus {

        /**
         * A freshly created cursor, database ResultSet consuming has not started.
         */
        CREATED,
        /**
         * A cursor currently in use, database ResultSet consuming has started.
         */
        OPEN,
        /**
         * A closed cursor, not fully consumed.
         */
        CLOSED,
        /**
         * A fully consumed cursor, a consumed cursor is always closed.
         */
        CONSUMED
    }

    public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw, RowBounds rowBounds) {
        this.resultSetHandler = resultSetHandler;
        this.resultMap = resultMap;
        this.rsw = rsw;
        this.rowBounds = rowBounds;
    }

    @Override
    public boolean isOpen() {
        return status == CursorStatus.OPEN;
    }

    @Override
    public boolean isConsumed() {
        return status == CursorStatus.CONSUMED;
    }

    @Override
    public int getCurrentIndex() {
        return rowBounds.getOffset() + cursorIterator.iteratorIndex;
    }

    @Override
    public Iterator<CloudEvent> iterator() {
        if (iteratorRetrieved) {
            throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
        }
        if (isClosed()) {
            throw new IllegalStateException("A Cursor is already closed.");
        }
        iteratorRetrieved = true;
        return cursorIterator;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }

        ResultSet rs = rsw.getResultSet();
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        } finally {
            status = CursorStatus.CLOSED;
        }
    }

    protected CloudEvent fetchNextUsingRowBound() {
        CloudEvent result = fetchNextObjectFromDatabase();
        while (objectWrapperResultHandler.fetched && indexWithRowBound < rowBounds.getOffset()) {
            result = fetchNextObjectFromDatabase();
        }
        return result;
    }

    protected CloudEvent fetchNextObjectFromDatabase() {
        if (isClosed()) {
            return null;
        }

        try {
            objectWrapperResultHandler.fetched = false;
            status = CursorStatus.OPEN;
            if (!rsw.getResultSet().isClosed()) {
                resultSetHandler.handleRowValues(rsw, resultMap, objectWrapperResultHandler, RowBounds.DEFAULT);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        CloudEvent next = objectWrapperResultHandler.result;
        if (objectWrapperResultHandler.fetched) {
            indexWithRowBound++;
        }
        // No more object or limit reached
        if (!objectWrapperResultHandler.fetched || getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit()) {
            close();
            status = CursorStatus.CONSUMED;
        }
        objectWrapperResultHandler.result = null;

        return next;
    }

    private boolean isClosed() {
        return status == CursorStatus.CLOSED || status == CursorStatus.CONSUMED;
    }

    private int getReadItemsCount() {
        return indexWithRowBound + 1;
    }

    protected static class ObjectWrapperResultHandler<T> implements ResultHandler<T> {

        protected T result;
        protected boolean fetched;

        @Override
        public void handleResult(ResultContext<? extends T> context) {
            this.result = context.getResultObject();
            context.stop();
            fetched = true;
        }
    }

    protected class CursorIterator implements Iterator<CloudEvent> {

        /**
         * Holder for the next object to be returned.
         */
        CloudEvent object;

        /**
         * Index of objects returned using next(), and as such, visible to users.
         */
        int iteratorIndex = -1;

        @Override
        public boolean hasNext() {
            if (!objectWrapperResultHandler.fetched) {
                object = fetchNextUsingRowBound();
            }
            return objectWrapperResultHandler.fetched;
        }

        @Override
        public CloudEvent next() {
            // Fill next with object fetched from hasNext()
            CloudEvent next = object;

            if (!objectWrapperResultHandler.fetched) {
                next = fetchNextUsingRowBound();
            }

            if (objectWrapperResultHandler.fetched) {
                objectWrapperResultHandler.fetched = false;
                object = null;
                iteratorIndex++;
                return next;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from Cursor");
        }
    }
}
