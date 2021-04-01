package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import cn.sliew.rtomde.service.bytecode.dispatcher.ServiceGenerator;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.ArrayList;
import java.util.List;

import static cn.sliew.milky.common.check.Ensures.notBlank;
import static cn.sliew.milky.common.check.Ensures.notEmpty;

public class DubboInterfaceGenerator implements ServiceGenerator {

    private static final Logger log = LoggerFactory.getLogger(DubboInterfaceGenerator.class);

    private ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);


    private final String namespace;
    private final List<MappedStatement> mappedStatements;
    private CtClass interfaceClazz;

    public DubboInterfaceGenerator(String namespace, List<MappedStatement> mappedStatements) {
        this.namespace = notBlank(namespace, "empty namespace");
        this.mappedStatements = notEmpty(mappedStatements, "empty mappedStatements");
    }

    public CtClass getInterfaceClazz() {
        if (interfaceClazz == null) {
            generate();
        }
        return interfaceClazz;
    }

    @Override
    public void generate() {
        interfaceClazz = classPool.makeInterface(namespace);
        try {
            CtMethod[] methods = generateServiceMethods(interfaceClazz, mappedStatements);
            for (CtMethod m : methods) {
                interfaceClazz.addMethod(m);
            }
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", interfaceClazz.getName(), e);
            throw new RuntimeException("create Service:" + interfaceClazz.getName() + " failed.", e);
        }
    }

    private CtMethod[] generateServiceMethods(CtClass declaring, List<MappedStatement> mappedStatements) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = doGenerateServiceMethod(declaring, ms);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod doGenerateServiceMethod(CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return new CtMethod(classPool.get(resultType.getName()), NameUtil.mappedStatementId(ms.getId()), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
