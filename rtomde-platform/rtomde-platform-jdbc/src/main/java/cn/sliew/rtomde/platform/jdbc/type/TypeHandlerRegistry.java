package cn.sliew.rtomde.platform.jdbc.type;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class TypeHandlerRegistry {

  private final Map<JdbcType, TypeHandler<?>>  jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
  private final TypeHandler<Object> unknownTypeHandler;
  private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();

  private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

  private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

  /**
   * The default constructor.
   */
  public TypeHandlerRegistry() {
    this(new Configuration());
  }

  /**
   * The constructor that pass the MyBatis configuration.
   *
   * @param configuration a MyBatis configuration
   * @since 3.5.4
   */
  public TypeHandlerRegistry(Configuration configuration) {
    this.unknownTypeHandler = UnknownTypeHandler(configuration);

    register(Boolean.class, new BooleanTypeHandler());
    register(boolean.class, new BooleanTypeHandler());
    register(org.apache.ibatis.type.JdbcType.BOOLEAN, new BooleanTypeHandler());
    register(org.apache.ibatis.type.JdbcType.BIT, new BooleanTypeHandler());

    register(Byte.class, new ByteTypeHandler());
    register(byte.class, new ByteTypeHandler());
    register(org.apache.ibatis.type.JdbcType.TINYINT, new ByteTypeHandler());

    register(Short.class, new org.apache.ibatis.type.ShortTypeHandler());
    register(short.class, new org.apache.ibatis.type.ShortTypeHandler());
    register(org.apache.ibatis.type.JdbcType.SMALLINT, new org.apache.ibatis.type.ShortTypeHandler());

    register(Integer.class, new org.apache.ibatis.type.IntegerTypeHandler());
    register(int.class, new org.apache.ibatis.type.IntegerTypeHandler());
    register(org.apache.ibatis.type.JdbcType.INTEGER, new org.apache.ibatis.type.IntegerTypeHandler());

    register(Long.class, new org.apache.ibatis.type.LongTypeHandler());
    register(long.class, new org.apache.ibatis.type.LongTypeHandler());

    register(Float.class, new org.apache.ibatis.type.FloatTypeHandler());
    register(float.class, new org.apache.ibatis.type.FloatTypeHandler());
    register(org.apache.ibatis.type.JdbcType.FLOAT, new org.apache.ibatis.type.FloatTypeHandler());

    register(Double.class, new org.apache.ibatis.type.DoubleTypeHandler());
    register(double.class, new org.apache.ibatis.type.DoubleTypeHandler());
    register(org.apache.ibatis.type.JdbcType.DOUBLE, new org.apache.ibatis.type.DoubleTypeHandler());

    register(Reader.class, new org.apache.ibatis.type.ClobReaderTypeHandler());
    register(String.class, new org.apache.ibatis.type.StringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.CHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.CLOB, new org.apache.ibatis.type.ClobTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.VARCHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.LONGVARCHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.NVARCHAR, new org.apache.ibatis.type.NStringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.NCHAR, new org.apache.ibatis.type.NStringTypeHandler());
    register(String.class, org.apache.ibatis.type.JdbcType.NCLOB, new org.apache.ibatis.type.NClobTypeHandler());
    register(org.apache.ibatis.type.JdbcType.CHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(org.apache.ibatis.type.JdbcType.VARCHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(org.apache.ibatis.type.JdbcType.CLOB, new org.apache.ibatis.type.ClobTypeHandler());
    register(org.apache.ibatis.type.JdbcType.LONGVARCHAR, new org.apache.ibatis.type.StringTypeHandler());
    register(org.apache.ibatis.type.JdbcType.NVARCHAR, new org.apache.ibatis.type.NStringTypeHandler());
    register(org.apache.ibatis.type.JdbcType.NCHAR, new org.apache.ibatis.type.NStringTypeHandler());
    register(org.apache.ibatis.type.JdbcType.NCLOB, new org.apache.ibatis.type.NClobTypeHandler());

    register(Object.class, org.apache.ibatis.type.JdbcType.ARRAY, new org.apache.ibatis.type.ArrayTypeHandler());
    register(org.apache.ibatis.type.JdbcType.ARRAY, new org.apache.ibatis.type.ArrayTypeHandler());

    register(BigInteger.class, new org.apache.ibatis.type.BigIntegerTypeHandler());
    register(org.apache.ibatis.type.JdbcType.BIGINT, new org.apache.ibatis.type.LongTypeHandler());

    register(BigDecimal.class, new org.apache.ibatis.type.BigDecimalTypeHandler());
    register(org.apache.ibatis.type.JdbcType.REAL, new org.apache.ibatis.type.BigDecimalTypeHandler());
    register(org.apache.ibatis.type.JdbcType.DECIMAL, new org.apache.ibatis.type.BigDecimalTypeHandler());
    register(org.apache.ibatis.type.JdbcType.NUMERIC, new org.apache.ibatis.type.BigDecimalTypeHandler());

    register(InputStream.class, new org.apache.ibatis.type.BlobInputStreamTypeHandler());
    register(Byte[].class, new org.apache.ibatis.type.ByteObjectArrayTypeHandler());
    register(Byte[].class, org.apache.ibatis.type.JdbcType.BLOB, new org.apache.ibatis.type.BlobByteObjectArrayTypeHandler());
    register(Byte[].class, org.apache.ibatis.type.JdbcType.LONGVARBINARY, new org.apache.ibatis.type.BlobByteObjectArrayTypeHandler());
    register(byte[].class, new org.apache.ibatis.type.ByteArrayTypeHandler());
    register(byte[].class, org.apache.ibatis.type.JdbcType.BLOB, new org.apache.ibatis.type.BlobTypeHandler());
    register(byte[].class, org.apache.ibatis.type.JdbcType.LONGVARBINARY, new org.apache.ibatis.type.BlobTypeHandler());
    register(org.apache.ibatis.type.JdbcType.LONGVARBINARY, new org.apache.ibatis.type.BlobTypeHandler());
    register(org.apache.ibatis.type.JdbcType.BLOB, new org.apache.ibatis.type.BlobTypeHandler());

    register(Object.class, unknownTypeHandler);
    register(Object.class, org.apache.ibatis.type.JdbcType.OTHER, unknownTypeHandler);
    register(org.apache.ibatis.type.JdbcType.OTHER, unknownTypeHandler);

    register(Date.class, new org.apache.ibatis.type.DateTypeHandler());
    register(Date.class, org.apache.ibatis.type.JdbcType.DATE, new org.apache.ibatis.type.DateOnlyTypeHandler());
    register(Date.class, org.apache.ibatis.type.JdbcType.TIME, new org.apache.ibatis.type.TimeOnlyTypeHandler());
    register(org.apache.ibatis.type.JdbcType.TIMESTAMP, new org.apache.ibatis.type.DateTypeHandler());
    register(org.apache.ibatis.type.JdbcType.DATE, new org.apache.ibatis.type.DateOnlyTypeHandler());
    register(org.apache.ibatis.type.JdbcType.TIME, new org.apache.ibatis.type.TimeOnlyTypeHandler());

    register(java.sql.Date.class, new org.apache.ibatis.type.SqlDateTypeHandler());
    register(java.sql.Time.class, new org.apache.ibatis.type.SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new org.apache.ibatis.type.SqlTimestampTypeHandler());

    register(String.class, org.apache.ibatis.type.JdbcType.SQLXML, new org.apache.ibatis.type.SqlxmlTypeHandler());

    register(Instant.class, new org.apache.ibatis.type.InstantTypeHandler());
    register(LocalDateTime.class, new org.apache.ibatis.type.LocalDateTimeTypeHandler());
    register(LocalDate.class, new org.apache.ibatis.type.LocalDateTypeHandler());
    register(LocalTime.class, new org.apache.ibatis.type.LocalTimeTypeHandler());
    register(OffsetDateTime.class, new org.apache.ibatis.type.OffsetDateTimeTypeHandler());
    register(OffsetTime.class, new org.apache.ibatis.type.OffsetTimeTypeHandler());
    register(ZonedDateTime.class, new org.apache.ibatis.type.ZonedDateTimeTypeHandler());
    register(Month.class, new org.apache.ibatis.type.MonthTypeHandler());
    register(Year.class, new org.apache.ibatis.type.YearTypeHandler());
    register(YearMonth.class, new org.apache.ibatis.type.YearMonthTypeHandler());
    register(JapaneseDate.class, new org.apache.ibatis.type.JapaneseDateTypeHandler());

    // issue #273
    register(Character.class, new org.apache.ibatis.type.CharacterTypeHandler());
    register(char.class, new org.apache.ibatis.type.CharacterTypeHandler());
  }

  /**
   * Set a default {@link org.apache.ibatis.type.TypeHandler} class for {@link Enum}.
   * A default {@link org.apache.ibatis.type.TypeHandler} is {@link org.apache.ibatis.type.EnumTypeHandler}.
   * @param typeHandler a type handler class for {@link Enum}
   * @since 3.4.5
   */
  public void setDefaultEnumTypeHandler(Class<? extends org.apache.ibatis.type.TypeHandler> typeHandler) {
    this.defaultEnumTypeHandler = typeHandler;
  }

  public boolean hasTypeHandler(Class<?> javaType) {
    return hasTypeHandler(javaType, null);
  }

  public boolean hasTypeHandler(org.apache.ibatis.type.TypeReference<?> javaTypeReference) {
    return hasTypeHandler(javaTypeReference, null);
  }

  public boolean hasTypeHandler(Class<?> javaType, org.apache.ibatis.type.JdbcType jdbcType) {
    return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
  }

  public boolean hasTypeHandler(org.apache.ibatis.type.TypeReference<?> javaTypeReference, org.apache.ibatis.type.JdbcType jdbcType) {
    return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
  }

  public org.apache.ibatis.type.TypeHandler<?> getMappingTypeHandler(Class<? extends org.apache.ibatis.type.TypeHandler<?>> handlerType) {
    return allTypeHandlersMap.get(handlerType);
  }

  public <T> org.apache.ibatis.type.TypeHandler<T> getTypeHandler(Class<T> type) {
    return getTypeHandler((Type) type, null);
  }

  public <T> org.apache.ibatis.type.TypeHandler<T> getTypeHandler(org.apache.ibatis.type.TypeReference<T> javaTypeReference) {
    return getTypeHandler(javaTypeReference, null);
  }

  public org.apache.ibatis.type.TypeHandler<?> getTypeHandler(org.apache.ibatis.type.JdbcType jdbcType) {
    return jdbcTypeHandlerMap.get(jdbcType);
  }

  public <T> org.apache.ibatis.type.TypeHandler<T> getTypeHandler(Class<T> type, org.apache.ibatis.type.JdbcType jdbcType) {
    return getTypeHandler((Type) type, jdbcType);
  }

  public <T> org.apache.ibatis.type.TypeHandler<T> getTypeHandler(org.apache.ibatis.type.TypeReference<T> javaTypeReference, org.apache.ibatis.type.JdbcType jdbcType) {
    return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
  }

  @SuppressWarnings("unchecked")
  private <T> org.apache.ibatis.type.TypeHandler<T> getTypeHandler(Type type, org.apache.ibatis.type.JdbcType jdbcType) {
    if (ParamMap.class.equals(type)) {
      return null;
    }
    Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
    org.apache.ibatis.type.TypeHandler<?> handler = null;
    if (jdbcHandlerMap != null) {
      handler = jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = jdbcHandlerMap.get(null);
      }
      if (handler == null) {
        // #591
        handler = pickSoleHandler(jdbcHandlerMap);
      }
    }
    // type drives generics here
    return (org.apache.ibatis.type.TypeHandler<T>) handler;
  }

  private Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> getJdbcHandlerMap(Type type) {
    Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
    if (NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap)) {
      return null;
    }
    if (jdbcHandlerMap == null && type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      if (Enum.class.isAssignableFrom(clazz)) {
        Class<?> enumClass = clazz.isAnonymousClass() ? clazz.getSuperclass() : clazz;
        jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(enumClass, enumClass);
        if (jdbcHandlerMap == null) {
          register(enumClass, getInstance(enumClass, defaultEnumTypeHandler));
          return typeHandlerMap.get(enumClass);
        }
      } else {
        jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
      }
    }
    typeHandlerMap.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
    return jdbcHandlerMap;
  }

  private Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> getJdbcHandlerMapForEnumInterfaces(Class<?> clazz, Class<?> enumClazz) {
    for (Class<?> iface : clazz.getInterfaces()) {
      Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(iface);
      if (jdbcHandlerMap == null) {
        jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(iface, enumClazz);
      }
      if (jdbcHandlerMap != null) {
        // Found a type handler regsiterd to a super interface
        HashMap<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> newMap = new HashMap<>();
        for (Entry<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
          // Create a type handler instance with enum type as a constructor arg
          newMap.put(entry.getKey(), getInstance(enumClazz, entry.getValue().getClass()));
        }
        return newMap;
      }
    }
    return null;
  }

  private Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
    Class<?> superclass =  clazz.getSuperclass();
    if (superclass == null || Object.class.equals(superclass)) {
      return null;
    }
    Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(superclass);
    if (jdbcHandlerMap != null) {
      return jdbcHandlerMap;
    } else {
      return getJdbcHandlerMapForSuperclass(superclass);
    }
  }

  private org.apache.ibatis.type.TypeHandler<?> pickSoleHandler(Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> jdbcHandlerMap) {
    org.apache.ibatis.type.TypeHandler<?> soleHandler = null;
    for (org.apache.ibatis.type.TypeHandler<?> handler : jdbcHandlerMap.values()) {
      if (soleHandler == null) {
        soleHandler = handler;
      } else if (!handler.getClass().equals(soleHandler.getClass())) {
        // More than one type handlers registered.
        return null;
      }
    }
    return soleHandler;
  }

  public org.apache.ibatis.type.TypeHandler<Object> getUnknownTypeHandler() {
    return unknownTypeHandler;
  }

  public void register(org.apache.ibatis.type.JdbcType jdbcType, org.apache.ibatis.type.TypeHandler<?> handler) {
    jdbcTypeHandlerMap.put(jdbcType, handler);
  }

  //
  // REGISTER INSTANCE
  //

  // Only handler

  @SuppressWarnings("unchecked")
  public <T> void register(org.apache.ibatis.type.TypeHandler<T> typeHandler) {
    boolean mappedTypeFound = false;
    org.apache.ibatis.type.MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(org.apache.ibatis.type.MappedTypes.class);
    if (mappedTypes != null) {
      for (Class<?> handledType : mappedTypes.value()) {
        register(handledType, typeHandler);
        mappedTypeFound = true;
      }
    }
    // @since 3.1.0 - try to auto-discover the mapped type
    if (!mappedTypeFound && typeHandler instanceof org.apache.ibatis.type.TypeReference) {
      try {
        org.apache.ibatis.type.TypeReference<T> typeReference = (org.apache.ibatis.type.TypeReference<T>) typeHandler;
        register(typeReference.getRawType(), typeHandler);
        mappedTypeFound = true;
      } catch (Throwable t) {
        // maybe users define the TypeReference with a different type and are not assignable, so just ignore it
      }
    }
    if (!mappedTypeFound) {
      register((Class<T>) null, typeHandler);
    }
  }

  // java type + handler

  public <T> void register(Class<T> javaType, org.apache.ibatis.type.TypeHandler<? extends T> typeHandler) {
    register((Type) javaType, typeHandler);
  }

  private <T> void register(Type javaType, org.apache.ibatis.type.TypeHandler<? extends T> typeHandler) {
    org.apache.ibatis.type.MappedJdbcTypes mappedJdbcTypes = typeHandler.getClass().getAnnotation(org.apache.ibatis.type.MappedJdbcTypes.class);
    if (mappedJdbcTypes != null) {
      for (org.apache.ibatis.type.JdbcType handledJdbcType : mappedJdbcTypes.value()) {
        register(javaType, handledJdbcType, typeHandler);
      }
      if (mappedJdbcTypes.includeNullJdbcType()) {
        register(javaType, null, typeHandler);
      }
    } else {
      register(javaType, null, typeHandler);
    }
  }

  public <T> void register(org.apache.ibatis.type.TypeReference<T> javaTypeReference, org.apache.ibatis.type.TypeHandler<? extends T> handler) {
    register(javaTypeReference.getRawType(), handler);
  }

  // java type + jdbc type + handler

  // Cast is required here
  @SuppressWarnings("cast")
  public <T> void register(Class<T> type, org.apache.ibatis.type.JdbcType jdbcType, org.apache.ibatis.type.TypeHandler<? extends T> handler) {
    register((Type) type, jdbcType, handler);
  }

  private void register(Type javaType, org.apache.ibatis.type.JdbcType jdbcType, org.apache.ibatis.type.TypeHandler<?> handler) {
    if (javaType != null) {
      Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> map = typeHandlerMap.get(javaType);
      if (map == null || map == NULL_TYPE_HANDLER_MAP) {
        map = new HashMap<>();
      }
      map.put(jdbcType, handler);
      typeHandlerMap.put(javaType, map);
    }
    allTypeHandlersMap.put(handler.getClass(), handler);
  }

  //
  // REGISTER CLASS
  //

  // Only handler type

  public void register(Class<?> typeHandlerClass) {
    boolean mappedTypeFound = false;
    org.apache.ibatis.type.MappedTypes mappedTypes = typeHandlerClass.getAnnotation(org.apache.ibatis.type.MappedTypes.class);
    if (mappedTypes != null) {
      for (Class<?> javaTypeClass : mappedTypes.value()) {
        register(javaTypeClass, typeHandlerClass);
        mappedTypeFound = true;
      }
    }
    if (!mappedTypeFound) {
      register(getInstance(null, typeHandlerClass));
    }
  }

  // java type + handler type

  public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
    register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
  }

  public void register(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    register(javaTypeClass, getInstance(javaTypeClass, typeHandlerClass));
  }

  // java type + jdbc type + handler type

  public void register(Class<?> javaTypeClass, org.apache.ibatis.type.JdbcType jdbcType, Class<?> typeHandlerClass) {
    register(javaTypeClass, jdbcType, getInstance(javaTypeClass, typeHandlerClass));
  }

  // Construct a handler (used also from Builders)

  @SuppressWarnings("unchecked")
  public <T> org.apache.ibatis.type.TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    if (javaTypeClass != null) {
      try {
        Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
        return (org.apache.ibatis.type.TypeHandler<T>) c.newInstance(javaTypeClass);
      } catch (NoSuchMethodException ignored) {
        // ignored
      } catch (Exception e) {
        throw new org.apache.ibatis.type.TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
      }
    }
    try {
      Constructor<?> c = typeHandlerClass.getConstructor();
      return (org.apache.ibatis.type.TypeHandler<T>) c.newInstance();
    } catch (Exception e) {
      throw new org.apache.ibatis.type.TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
    }
  }

  // scan

  public void register(String packageName) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(org.apache.ibatis.type.TypeHandler.class), packageName);
    Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
    for (Class<?> type : handlerSet) {
      //Ignore inner classes and interfaces (including package-info.java) and abstract classes
      if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
        register(type);
      }
    }
  }

  // get information

  /**
   * Gets the type handlers.
   *
   * @return the type handlers
   * @since 3.2.2
   */
  public Collection<org.apache.ibatis.type.TypeHandler<?>> getTypeHandlers() {
    return Collections.unmodifiableCollection(allTypeHandlersMap.values());
  }

}
