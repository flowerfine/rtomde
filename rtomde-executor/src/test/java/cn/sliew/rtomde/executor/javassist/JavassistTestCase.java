package cn.sliew.rtomde.executor.javassist;

import javassist.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

public class JavassistTestCase {

    @Test
    public void testGenerate() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeInterface("cn.sliew.rtomde.executor.javassist.UserMapper");

        //字段初始化可以后续通过ApplicationContext或者添加一个超类，超类含有sqlSessionFactory的引用，或者添加setter方法
        CtField sqlSessionFactoryMethod = CtField.make("private " + SqlSessionFactory.class.getCanonicalName() + " sqlSessionFactory;", cc);
        cc.addField(sqlSessionFactoryMethod);

        pool.importPackage("org.apache.ibatis.session");
        pool.importPackage("java.util");

        StringBuilder methodBody = new StringBuilder();
        methodBody.append("public Object selectByPrimaryKey(Long id)");
        methodBody.append("{");
        methodBody.append("SqlSession sqlSession = this.sqlSessionFactory.openSession();");
        methodBody.append("System.out.println(\"hhhhh\");");
        methodBody.append("List objects = sqlSession.selectList(\"selectByPrimaryKey\", $1);");
////        methodBody.append("\n");
        methodBody.append("return ($r) objects.get(0);");
//        methodBody.append("return null;");
        methodBody.append("}");

//        CtMethod selectByPrimaryKey1 = CtNewMethod.make(Modifier.PUBLIC, returnType, "selectByPrimaryKey", new CtClass[]{CtClass.longType}, null, methodBody.toString(), cc);
        CtMethod selectByPrimaryKey = CtNewMethod.make(methodBody.toString(), cc);
        cc.addMethod(selectByPrimaryKey);

        cc.writeFile();
    }
}