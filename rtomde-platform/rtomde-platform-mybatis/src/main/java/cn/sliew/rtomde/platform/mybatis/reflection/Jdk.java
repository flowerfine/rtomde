package cn.sliew.rtomde.platform.mybatis.reflection;

import cn.sliew.rtomde.platform.mybatis.io.Resources;

/**
 * To check the existence of version dependent classes.
 */
public class Jdk {


    static {
        try {
            Resources.classForName("java.lang.reflect.Parameter");
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    static {
        try {
            Resources.classForName("java.time.Clock");
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    static {
        try {
            Resources.classForName("java.util.Optional");
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    private Jdk() {
        super();
    }
}
