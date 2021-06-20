//package cn.sliew.rtomde.platform.mybatis.builder;
//
//import cn.sliew.milky.test.MilkyTestCase;
//import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
//import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
//import cn.sliew.rtomde.platform.mybatis.domain.blog.Author;
//import cn.sliew.rtomde.platform.mybatis.domain.blog.Blog;
//import cn.sliew.rtomde.platform.mybatis.domain.jpetstore.Cart;
//import cn.sliew.rtomde.platform.mybatis.io.JBoss6VFS;
//import cn.sliew.rtomde.platform.mybatis.io.Resources;
//import cn.sliew.rtomde.platform.mybatis.scripting.defaults.RawLanguageDriver;
//import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.XMLLanguageDriver;
//import cn.sliew.rtomde.platform.mybatis.session.AutoMappingBehavior;
//import cn.sliew.rtomde.platform.mybatis.session.AutoMappingUnknownColumnBehavior;
//import cn.sliew.rtomde.platform.mybatis.type.*;
//import org.junit.jupiter.api.Test;
//
//import java.io.InputStream;
//import java.io.StringReader;
//import java.math.RoundingMode;
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Properties;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class XMLMetadataBuilderTest extends MilkyTestCase {
//
//    @Test
//    void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
//        String resource = "cn/sliew/rtomde/platform/mybatis/builder/MinimalMapperConfig.xml";
//        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
//            XMLMetadataBuilder builder = new XMLMetadataBuilder(inputStream);
//            MybatisPlatformOptions platform = builder.parse();
//            assertNotNull(platform);
//
//            assertThat(platform.isSafeRowBoundsEnabled()).isFalse();
//            assertThat(platform.isSafeResultHandlerEnabled()).isTrue();
//            assertThat(platform.isAggressiveLazyLoading()).isFalse();
//            assertThat(platform.isMultipleResultSetsEnabled()).isTrue();
//            assertThat(platform.isUseColumnLabel()).isTrue();
//            assertThat(platform.isCallSettersOnNulls()).isFalse();
//            assertThat(platform.isUseActualParamName()).isTrue();
//            assertThat(platform.isReturnInstanceForEmptyRow()).isFalse();
//            assertThat(platform.isShrinkWhitespacesInSql()).isFalse();
//
//            assertThat(platform.getJdbcTypeForNull()).isEqualTo(JdbcType.OTHER);
//            assertThat(platform.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
//
//            assertThat(platform.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.PARTIAL);
//            assertThat(platform.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.NONE);
//
////            assertNull(platform.getDefaultStatementTimeout());
//            assertThat(platform.getDefaultScriptingLanguageInstance()).isInstanceOf(XMLLanguageDriver.class);
//            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumTypeHandler.class);
//        }
//    }
//
//    enum MyEnum {
//        ONE, TWO
//    }
//
//    public static class EnumOrderTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
//
//        private E[] constants;
//
//        public EnumOrderTypeHandler(Class<E> javaType) {
//            constants = javaType.getEnumConstants();
//        }
//
//        @Override
//        public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
//            ps.setInt(i, parameter.ordinal() + 1); // 0 means NULL so add +1
//        }
//
//        @Override
//        public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
//            int index = rs.getInt(columnName) - 1;
//            return index < 0 ? null : constants[index];
//        }
//
//        @Override
//        public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//            int index = rs.getInt(rs.getInt(columnIndex)) - 1;
//            return index < 0 ? null : constants[index];
//        }
//
//        @Override
//        public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//            int index = cs.getInt(columnIndex) - 1;
//            return index < 0 ? null : constants[index];
//        }
//    }
//
//    @Test
//    void registerJavaTypeInitializingTypeHandler() {
//        final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
//                + "<!DOCTYPE metadata PUBLIC \"-//sliew.cn//DTD Metadata 1.0//EN\" \"http://sliew.cn/dtd/mybatis-metadata-1.dtd\">\n"
//                + "<metadata name=\"mybatis\">\n"
//                + "  <typeHandlers>\n"
//                + "    <typeHandler javaType=\"cn.sliew.rtomde.platform.mybatis.builder.XMLMetadataBuilderTest$MyEnum\"\n"
//                + "      handler=\"cn.sliew.rtomde.platform.mybatis.builder.XMLMetadataBuilderTest$EnumOrderTypeHandler\"/>\n"
//                + "  </typeHandlers>\n"
//                + "</metadata>\n";
//
//        XMLMetadataBuilder builder = new XMLMetadataBuilder(new StringReader(MAPPER_CONFIG));
//        MybatisPlatformOptions platform = builder.parse();
//
//        TypeHandlerRegistry typeHandlerRegistry = platform.getTypeHandlerRegistry();
//        TypeHandler<MyEnum> typeHandler = typeHandlerRegistry.getTypeHandler(MyEnum.class);
//
//        assertTrue(typeHandler instanceof EnumOrderTypeHandler);
//        assertArrayEquals(MyEnum.values(), ((EnumOrderTypeHandler<MyEnum>) typeHandler).constants);
//    }
//
//    @Test
//    void shouldSuccessfullyLoadXMLConfigFile() throws Exception {
//        String resource = "cn/sliew/rtomde/platform/mybatis/builder/CustomizedSettingsMapperConfig.xml";
//        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
//            Properties props = new Properties();
//            props.put("prop2", "cccc");
//            XMLMetadataBuilder builder = new XMLMetadataBuilder(inputStream, null, props);
//            MybatisPlatformOptions platform = builder.parse();
//
//            assertThat(platform.isSafeRowBoundsEnabled()).isTrue();
//            assertThat(platform.isSafeResultHandlerEnabled()).isFalse();
//            assertThat(platform.isAggressiveLazyLoading()).isTrue();
//            assertThat(platform.isMultipleResultSetsEnabled()).isFalse();
//            assertThat(platform.isUseColumnLabel()).isFalse();
//            assertThat(platform.isCallSettersOnNulls()).isTrue();
//            assertThat(platform.isUseActualParamName()).isTrue();
//            assertThat(platform.isReturnInstanceForEmptyRow()).isFalse();
//            assertThat(platform.isShrinkWhitespacesInSql()).isTrue();
//
//            assertThat(platform.getVfsImpl().getName()).isEqualTo(JBoss6VFS.class.getName());
//            assertThat(platform.getJdbcTypeForNull()).isEqualTo(JdbcType.NULL);
//            assertThat(platform.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")));
//
//            assertThat(platform.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.NONE);
//            assertThat(platform.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.WARNING);
//
////            assertThat(platform.getDefaultStatementTimeout()).isEqualTo(10);
//            assertThat(platform.getDefaultScriptingLanguageInstance()).isInstanceOf(RawLanguageDriver.class);
//
//            assertThat(platform.getTypeAliasRegistry().getTypeAliases().get("blogauthor")).isEqualTo(Author.class);
//            assertThat(platform.getTypeAliasRegistry().getTypeAliases().get("blog")).isEqualTo(Blog.class);
//
////            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(Integer.class)).isInstanceOf(CustomIntegerTypeHandler.class);
//            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(Long.class)).isInstanceOf(CustomLongTypeHandler.class);
//            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(String.class)).isInstanceOf(CustomStringTypeHandler.class);
//            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(String.class, JdbcType.VARCHAR)).isInstanceOf(CustomStringTypeHandler.class);
//            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumOrdinalTypeHandler.class);
//        }
//    }
//
//
//}
