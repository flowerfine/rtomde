package cn.sliew.rtomde.executor.javassist;

import javassist.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

public class JavassistTestCase {

    @Test
    public void testGenerate() throws Exception {
        String userDir = System.getProperty("user.dir");
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeInterface("cn.sliew.rtomde.executor.javassist.UserMapper");

        //字段初始化可以后续通过ApplicationContext或者添加一个超类，超类含有sqlSessionFactory的引用，或者添加setter方法
        CtField sqlSessionFactoryMethod = CtField.make("private " + SqlSessionFactory.class.getCanonicalName() + " sqlSessionFactory;", cc);
        cc.addField(sqlSessionFactoryMethod);

        CtClass returnType = pool.get("cn.sliew.rtomde.executor.mapper.SysUser");
        pool.importPackage("org.apache.ibatis.session");
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("public cn.sliew.rtomde.executor.mapper.SysUser selectByPrimaryKey(Long id)");
        methodBody.append("{");
        methodBody.append("SqlSession sqlSession = sqlSessionFactory.openSession();");
        methodBody.append("List<Object> objects = sqlSession.selectList(\"selectByPrimaryKey\", $1);");
////        methodBody.append("\n");
//        methodBody.append("return ($r) objects.get(0);");
        methodBody.append("return null;");
        methodBody.append("}");

//        CtMethod selectByPrimaryKey1 = CtNewMethod.make(Modifier.PUBLIC, returnType, "selectByPrimaryKey", new CtClass[]{CtClass.longType}, null, methodBody.toString(), cc);
        CtMethod selectByPrimaryKey = CtNewMethod.make(methodBody.toString(), cc);

        cc.addMethod(selectByPrimaryKey);
        cc.writeFile(userDir);
    }
}