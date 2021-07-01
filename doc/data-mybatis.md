# data-mybatis

数据引擎需要解决如何从存储系统中读取数据的问题。

存储系统从关系型数据库到 NoSQL 系列如 redis、mongodb、hbase，消息队列 kafka、pulsar，搜索引擎 elasticsearch、solr，大数据存储 HDFS、对象存储，以及 OLAP 系统如 hive，clickhouse，presto 等，面对琳琅满目的存储系统，该如何提供支持呢？

随着对 SQL 标准的共识，越来越多的存储系统会在自定义查询语言的基础之外提供 SQL 查询功能而推出自己的 JDBC 驱动实现，另外如 presto、drill 等分析引擎对使用 SQL 进行联邦查询的技术的成熟，通过 presto 等分析引擎查询不支持 SQL 功能的存储系统成为了可能，数据引擎首要是对 SQL 查询提供支持。

在 java 生态中 `ORM` 框架流行的有 `hibernate` 这样的完全 `ORM` 框架，还有 `mybatis` 这样的半 `ORM` 框架。数据引擎通过对 `mybatis` 进行二次开发实现对 `JDBC` 增删改查功能的支持。

## 服务域/实体域/会话域

在任何框架或组件中，都会有自己的核心领域模型，如 spring 框架中的 bean。这个核心领域模型及其组成部分称为实体域，它代表着我们要操作的目标本身。服务域也就是行为域，它是组件的功能集，同时也负责实体域和会话域的生命周期管理， 比如 spring 的 ApplicationContext。服务域的对象通常会比较重，而且是线程安全的，并以单一实例服务于所有调用。

什么是会话？就是一次交互过程。会话中重要的概念是上下文，上下文通常持有交互过程中的状态变量等。会话对象通常较轻，每次请求都重新创建实例，请求结束后销毁。简而言之：把元信息交由实体域持有，把一次请求中的临时状态由会话域持有，由服务域贯穿整个过程。

`mybatis` 框架可以简单地分为两部分：配置和执行，接下来按照*服务域/实体域/会话域* 的概念简单地分析一下 `mybatis` 的源码结构。

## 配置

通常使用 `mybatis` 时会采用 `xml` 的方式提供 `sql` 与 `Mapper` 接口的方式，在 `mybatis` 启动的时候会解析配置的 `xml` 文件，构建核心的 `MappedStatement`，通过对 `Mapper` 接口进行代理，实现 `MappedStatement` 与 `Mapper` 接口方法的绑定。

### MappedStatement

`MappedStatement` 是属于配置的实体域，它包含请求参数、结果的映射，待执行的 sql。

```java
public final class MappedStatement {

  // .......
  private Configuration configuration;
  private String id;
  private StatementType statementType;
  private SqlSource sqlSource;
  private ParameterMap parameterMap;
  private List<ResultMap> resultMaps;
  private SqlCommandType sqlCommandType;
  private Log statementLog;
  private LanguageDriver lang;
  // ......
}
```

* id。`MappedStatement` 的应用内唯一标识。
* statementType。JDBC 的 Statement 类型，有 `STATEMENT`、`PREPARED`、`CALLABLE` 三种，默认为 `PREPARED`。
* sqlCommandType。sql 的执行类型，主要为 `INSERT`、`UPDATE`、`DELETE`、`SELECT`。
* sqlSource。未解析的 sql，使用参数对象解析后获得绑定的待执行的 SQL。
* lang。可以创建 `SqlSource` 以及执行对 `Statement` 进行参数赋值的 `ParameterHandler`。
* statementLog。用于在 `MappedStatement` 执行期间输出日志信息。
* parameterMap。参数映射关系。
* resultMaps。返回结果映射关系。

### Configuration

`Configuration` 是配置的服务域，包含解析的所有的  `MappedStatement`、`ParameterMap`、`ResultMap` 等。

```java
public class Configuration {
	// ......
  protected Environment environment;

  protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
  protected final InterceptorChain interceptorChain = new InterceptorChain();
  protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
  protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

  protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection")
      .conflictMessageProducer((savedValue, targetValue) ->
          ". please check " + savedValue.getResource() + " and " + targetValue.getResource());
  protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");
  protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");
  protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");
  // ......
}
```

### Parameter

参数主要描述字段属性用于 `ParameterHandler` 对 `Statement` 进行参数传递。

```java
public class ParameterMap {
	// ......
  private String id;
  private Class<?> type;
  private List<ParameterMapping> parameterMappings;
  // ......
}

public class ParameterMapping {
	// ......
  private Configuration configuration;

  private String property;
  private Class<?> javaType = Object.class;
  private JdbcType jdbcType;
  private TypeHandler<?> typeHandler;
  // ......
}
```

### Result

结果映射主要描述如何将 sql 返回结果映射为返回结果对象。

```java
public class ResultMap {
  // ......
  private Configuration configuration;

  private String id;
  private Class<?> type;
  private List<ResultMapping> resultMappings;
  // ......
}

public class ResultMapping {
	
  // ......
  private Configuration configuration;
  private String property;
  private String column;
  private Class<?> javaType;
  private JdbcType jdbcType;
  private TypeHandler<?> typeHandler;
  // ......
}
```

### SqlSource

`SqlSource` 是为绑定的 sql，它的来源有两个：`xml` 文件和 `provider` 注解。经过参数解析绑定后可以得到 `BoundSql`。

```java
public interface SqlSource {

  BoundSql getBoundSql(Object parameterObject);
}

public class BoundSql {

  private final String sql;
  private final List<ParameterMapping> parameterMappings;
  private final Object parameterObject;
  private final Map<String, Object> additionalParameters;
  private final MetaObject metaParameters;
}
```

`BoundSql` 即为一个会话域。

## 执行查询

执行查询到入口为 `SqlSession`，主要的执行功能代理给 `Executor` 执行，`SqlSessionFactory` 用于创建 `SqlSession`。`SqlSession` 线程不安全，无需回收，`SqlSessionManager` 采用 `ThreadLocal` 缓存了 `SqlSession`，避免了 `SqlSession` 的无限创建。

### SqlSessionFactory

`SqlSessionFactory` 为服务域，用于创建 `SqlSession`。

```java
public interface SqlSessionFactory {

  SqlSession openSession();

  SqlSession openSession(boolean autoCommit);

  SqlSession openSession(Connection connection);

  SqlSession openSession(TransactionIsolationLevel level);

  SqlSession openSession(ExecutorType execType);

  SqlSession openSession(ExecutorType execType, boolean autoCommit);

  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

  SqlSession openSession(ExecutorType execType, Connection connection);

  Configuration getConfiguration();
}
```

### SqlSession

`SqlSession` 为实体域，使用参数对象执行 `MappedStatement`，返回执行结果。

```java
public interface SqlSession extends Closeable {

  <T> T selectOne(String statement);
  <T> T selectOne(String statement, Object parameter);

  <E> List<E> selectList(String statement);
  <E> List<E> selectList(String statement, Object parameter);
  <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

  <K, V> Map<K, V> selectMap(String statement, String mapKey);
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);

  <T> Cursor<T> selectCursor(String statement);
  <T> Cursor<T> selectCursor(String statement, Object parameter);
  <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds);

  void select(String statement, Object parameter, ResultHandler handler);
  void select(String statement, ResultHandler handler);
  void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

  int insert(String statement);
  int insert(String statement, Object parameter);

  int update(String statement);
  int update(String statement, Object parameter);

  int delete(String statement);
  int delete(String statement, Object parameter);

  void commit();
  void commit(boolean force);
  void rollback();
  void rollback(boolean force);

  List<BatchResult> flushStatements();

  @Override
  void close();

  void clearCache();

  Configuration getConfiguration();

  <T> T getMapper(Class<T> type);

  Connection getConnection();
}
```

### Executor

`Executor` 与 `SqlSession` 具有相同的生命周期，在 `#update` 方法中执行插入，更新和删除请求，在 `#query` 中执行查询请求，`#queryCursor` 执行流式查询。

`Executor` 内部还提供了缓存功能和延迟加载功能。

```java
public interface Executor {

  ResultHandler NO_RESULT_HANDLER = null;

  int update(MappedStatement ms, Object parameter) throws SQLException;

  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;
  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

  <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

  List<BatchResult> flushStatements() throws SQLException;

  void commit(boolean required) throws SQLException;

  void rollback(boolean required) throws SQLException;

  CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);
  boolean isCached(MappedStatement ms, CacheKey key);
  void clearLocalCache();

  void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

  Transaction getTransaction();

  void close(boolean forceRollback);

  boolean isClosed();

  void setExecutorWrapper(Executor executor);

}
```

### StatementHandler

`StatementHandler` 为执行的核心会话域，负责创建 `Statement`，参数赋值，执行查询，对 SQL 查询结果进行转换映射。

```java
public interface StatementHandler {

  Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException;

  void parameterize(Statement statement) throws SQLException;

  void batch(Statement statement) throws SQLException;

  int update(Statement statement) throws SQLException;

  <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

  <E> Cursor<E> queryCursor(Statement statement) throws SQLException;

  BoundSql getBoundSql();

  ParameterHandler getParameterHandler();
}
```