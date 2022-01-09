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
package cn.rtomde.template.executor;

import cn.sliew.mybatis.component.service.DataSourceService;
import cn.sliew.mybatis.cursor.Cursor;
import cn.sliew.mybatis.executor.result.ResultHandler;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    DataSourceService getDataSourceService();

    int update(ExecuteContext context) throws SQLException;

    <E> List<E> query(ExecuteContext context) throws SQLException;

    <E> Cursor<E> queryCursor(ExecuteContext context) throws SQLException;

    void close();
}
