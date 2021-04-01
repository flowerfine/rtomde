package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.service.bytecode.dispatcher.MapperDispatcher;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import cn.sliew.rtomde.service.bytecode.dispatcher.ServiceGenerator;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.ArrayList;
import java.util.List;

import static cn.sliew.milky.common.check.Ensures.*;

public class SpringmvcServiceGenerator implements ServiceGenerator {

    private static final Logger log = LoggerFactory.getLogger(SpringmvcServiceGenerator.class);

    private final String mapperDispatcherName = "mapperDispatcher";

    private ClassLoader classLoader = ClassUtils.getClassLoader(SpringmvcServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    private final MybatisApplicationOptions applicationOptions;
    private final String namespace;
    private final List<MappedStatement> mappedStatements;

    private CtClass controllerClazz;

    public SpringmvcServiceGenerator(MybatisApplicationOptions applicationOptions, String namespace, List<MappedStatement> mappedStatements) {
        this.applicationOptions = checkNotNull(applicationOptions, "null applicationOptions");
        this.namespace = notBlank(namespace, "empty namespace");
        this.mappedStatements = notEmpty(mappedStatements, "empty mappedStatements");
    }

    public CtClass getControllerClazz() {
        if (controllerClazz == null) {
            generate();
        }
        return controllerClazz;
    }

    @Override
    public void generate() {
        CtClass controllerClass = classPool.makeClass(namespace);
        ClassFile ccFile = controllerClass.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        // @RestController
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation controller = new Annotation("org.springframework.web.bind.annotation.RestController", constpool);
        classAttr.addAnnotation(controller);

        // @RequestMapping
        Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
        // mapper path
        ArrayMemberValue memberValue = new ArrayMemberValue(constpool);
        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + applicationOptions.getId(), constpool)});
        requestMapping.addMemberValue("path", memberValue);
        classAttr.addAnnotation(requestMapping);

        ccFile.addAttribute(classAttr);

        try {
            CtField autowiredField = generateAutowiredField(controllerClass, constpool);
            controllerClass.addField(autowiredField);
            // method
            CtMethod[] methods = generateRequestMappings(controllerClass, constpool);
            for (CtMethod m : methods) {
                controllerClass.addMethod(m);
            }
            this.controllerClazz = controllerClass;
        } catch (CannotCompileException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        }
    }

    private CtField generateAutowiredField(CtClass declaring, ConstPool constpool) {
        try {
            CtField ctField = new CtField(classPool.get(MapperDispatcher.class.getName()), mapperDispatcherName, declaring);
            ctField.setModifiers(Modifier.PRIVATE);
            FieldInfo fieldInfo = ctField.getFieldInfo();
            // @Autowired
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
            fieldAttr.addAnnotation(autowired);
            // @Qualifier
            Annotation qualifier = new Annotation("org.springframework.beans.factory.annotation.Qualifier", constpool);
            qualifier.addMemberValue("value", new StringMemberValue(MapperDispatcher.BEAN_NAME, constpool));
            fieldAttr.addAnnotation(qualifier);

            fieldInfo.addAttribute(fieldAttr);
            return ctField;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("make dispatcher field failed.", e);
            throw new RuntimeException("make @Autowired field failed.", e);
        }
    }

    private CtMethod[] generateRequestMappings(CtClass declaring, ConstPool constpool) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = doGenerateRequestMapping(declaring, ms);

            // 方法上添加注解
            MethodInfo info = method.getMethodInfo();

            AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            //@RequestMapping
            Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.GetMapping", constpool);
            ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
            pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + NameUtil.mappedStatementId(ms.getId()), constpool)});
            requestMapping.addMemberValue("path", pathValue);
            methodAttr.addAnnotation(requestMapping);
            //@ResponseBody
            Annotation responseBody = new Annotation("org.springframework.web.bind.annotation.ResponseBody", constpool);
            methodAttr.addAnnotation(responseBody);

            // 参数上添加注解
            ParameterAnnotationsAttribute paramAttr = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
            Annotation[][] paramAnnos = new Annotation[1][1];
            Annotation requestBody = new Annotation("org.springframework.web.bind.annotation.RequestBody", constpool);
            paramAnnos[0][0] = requestBody;
            paramAttr.setAnnotations(paramAnnos);

            info.addAttribute(methodAttr);
            info.addAttribute(paramAttr);

            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod doGenerateRequestMapping(CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return generateMapperMethodBody(resultType, paramType, ms.getId(), declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethodBody(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), NameUtil.mappedStatementId(id), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return " + mapperDispatcherName + ".execute(\"" + id + "\", \"" + applicationOptions.getId() + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

}
