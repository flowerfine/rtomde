//package cn.sliew.rtomde.service.springmvc.controller;
//
//import cn.sliew.rtomde.executor.mapper.MapperInvoker;
//import cn.sliew.rtomde.executor.mapper.MapperMethod;
//import cn.sliew.rtomde.executor.mapper.PlainMapperInvoker;
//import javassist.*;
//import javassist.bytecode.*;
//import javassist.bytecode.annotation.Annotation;
//import javassist.bytecode.annotation.ArrayMemberValue;
//import javassist.bytecode.annotation.StringMemberValue;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.context.support.GenericWebApplicationContext;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
//
//import javax.annotation.PostConstruct;
//import java.util.Collection;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//@Slf4j
//@Configuration
//public class AnotherDispatcher {
//
//    private static ClassPool classPool = ClassPool.getDefault();
//
//    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);
//
//    @Autowired
//    private GenericWebApplicationContext ac;
//    @Autowired
//    private RequestMappingHandlerMapping mappingRegistry;
//    @Autowired
//    private SqlSessionFactory sqlSessionFactory;
//
//    static {
//        ClassClassPath path = new ClassClassPath(AnotherDispatcher.class);
//        classPool.insertClassPath(path);
//    }
//
//    @PostConstruct
//    public void register() {
//        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
//        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
//        for (String mappedStatementName : mappedStatementNames) {
//            MapperMethod mapperMethod = new MapperMethod(configuration, mappedStatementName);
//            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
//        }
//        for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
//            String key = entry.getKey();
//            if (!key.equals("selectByPrimaryKey")) {
//                continue;
//            }
//            registerController("cn.sliew.rtomde.executor.mapper.SysUserMapper");
//        }
//    }
//
//
//    public Class<?> registerController(String interfaceClass) throws Exception {
//        CtClass controllerClass = classPool.makeClass(interfaceClass + "Controller");
//        ClassFile ccFile = controllerClass.getClassFile();
//
//        ConstPool constpool = ccFile.getConstPool();
//        // 类上添加@RestController注解
//        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
//        Annotation controller = new Annotation("org.springframework.web.bind.annotation.RestController", constpool);
//        classAttr.addAnnotation(controller);
//
//        // 类上添加@RequestMapping注解
//        Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
//        //Mapping路径(使用Mapper接口的名称)
//        ArrayMemberValue memberValue = new ArrayMemberValue(constpool);
//        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("/SysUserMapper", constpool)});
//        requestMapping.addMemberValue("path", memberValue);
//        classAttr.addAnnotation(requestMapping);
//
//        ccFile.addAttribute(classAttr);
//        try {
//            // 添加成员变量:调度器
//            controllerClass.addField(makeAutowiredField(controllerClass, constpool));
//
//            // 添加方法
//            CtMethod[] methods = makeRequestMapping(interfaceClass, controllerClass, constpool);
//            for (CtMethod m : methods) {
//                controllerClass.addMethod(m);
//            }
//            return controllerClass.toClass();
//        } catch (CannotCompileException e) {
//            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
//            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
//        }
//    }
//
//    private CtField makeAutowiredField(CtClass declaring, ConstPool constpool) throws Exception {
//        try {
//            CtField ctField = new CtField(classPool.get(sqlSessionFactory.getClass().getName()), "sqlSessionFactory", declaring);
//            ctField.setModifiers(Modifier.PRIVATE);
//            FieldInfo fieldInfo = ctField.getFieldInfo();
//            // 成员变量添加@Autowired注解
//            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
//            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
//            fieldAttr.addAnnotation(autowired);
//            fieldInfo.addAttribute(fieldAttr);
//            return ctField;
//        } catch (CannotCompileException | NotFoundException e) {
//            log.error("make dispatcher field failed.", e);
//            throw new RuntimeException("make dispatcher field failed.", e);
//        }
//    }
//
//    private CtMethod[] makeRequestMapping(Class<?> interfaceClass, CtClass declaring, ConstPool constpool) {
//        try {
//            CtClass parentCtClass = classPool.get(interfaceClass.getName());
//            CtMethod[] declaredMethods = parentCtClass.getDeclaredMethods();
//            CtMethod[] methods = new CtMethod[declaredMethods.length];
//            for (int i = 0; i < declaredMethods.length; i++) {
//                // 生成方法体
//                CtMethod method = generatedMetaMethod(declaredMethods[i], declaring);
//                // 方法上添加注解
//                MethodInfo info = method.getMethodInfo();
//                AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
//                // 添加 @RequestMapping注解
//                Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
//                //Mapping路径(使用方法名)
//                ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
//                pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + declaredMethods[i].getName(), constpool)});
//                requestMapping.addMemberValue("path", pathValue);
//                methodAttr.addAnnotation(requestMapping);
//                // 添加@ResponseBody注解
//                Annotation responseBody = new Annotation("org.springframework.web.bind.annotation.ResponseBody", constpool);
//                methodAttr.addAnnotation(responseBody);
//                //参数上注解@RequestBody
//                Annotation requestBody = new Annotation("org.springframework.web.bind.annotation.RequestBody", constpool);
//                ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
//                javassist.bytecode.annotation.Annotation[][] anno = new javassist.bytecode.annotation.Annotation[][]{{requestBody}};
//                parameterAnnotationsAttribute.setAnnotations(anno);
//
//                info.addAttribute(methodAttr);
//                info.addAttribute(parameterAnnotationsAttribute);
//                methods[i] = method;
//            }
//            return methods;
//        } catch (CannotCompileException | NotFoundException e) {
//            log.error("create request mapping failed for interface:[{}]", interfaceClass.getName(), e);
//            throw new RuntimeException("create request mapping failed for interface:" + interfaceClass.getName(), e);
//        }
//    }
//
//    public CtMethod generatedMetaMethod(CtMethod ctMethod, CtClass declaring) throws NotFoundException, CannotCompileException {
//        CtMethod method = new CtMethod(ctMethod.getReturnType(), ctMethod.getName(), ctMethod.getParameterTypes(), declaring);
//        StringBuilder builder = new StringBuilder();
//        builder.append("{\n");
//        builder.append("com.xxx.xxx.Context respContext = null;\n");
//        builder.append("try{\n");
//        builder.append("com.xxx.xxx.Context reqContext = com.xxx.xxx.Context.createContext($1);\n");
//        builder.append("respContext = coreTrnDispatcher.execute(reqContext);\n");
//        builder.append("}").append("catch(Throwable t){\n");
//        builder.append("throw new RuntimeException(t);").append("}\n");
//        builder.append("return ($r)com.xxx.xxx.BeanUtil.Map2Bean(respContext,").append(ctMethod.getReturnType().getName())
//                .append(".class);\n}");
//        method.setBody(builder.toString());
//        return method;
//    }
//
//}