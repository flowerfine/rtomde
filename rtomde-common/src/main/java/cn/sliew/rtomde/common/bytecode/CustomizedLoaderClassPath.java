package cn.sliew.rtomde.common.bytecode;

import javassist.ClassPath;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * A class search-path representing a class loader.
 *
 * <p>It is used for obtaining a class file from the given
 * class loader by <code>getResourceAsStream()</code>.
 * The <code>LoaderClassPath</code> refers to the class loader through
 * <code>WeakReference</code>.  If the class loader is garbage collected,
 * the other search pathes are examined.
 *
 * <p>The given class loader must have both <code>getResourceAsStream()</code>
 * and <code>getResource()</code>.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Shigeru Chiba
 */
public class CustomizedLoaderClassPath implements ClassPath {
    private WeakReference clref;

    /**
     * Creates a search path representing a class loader.
     */
    public CustomizedLoaderClassPath(ClassLoader cl) {
        clref = new WeakReference(cl);
    }

    public String toString() {
        Object cl = null;
        if (clref != null)
            cl = clref.get();

        return cl == null ? "<null>" : cl.toString();
    }

    /**
     * Obtains a class file from the class loader.
     * This method calls <code>getResourceAsStream(String)</code>
     * on the class loader.
     */
    public InputStream openClassfile(String classname) {
        String cname = classname.replace('.', '/') + ".class";
        ClassLoader cl = (ClassLoader) clref.get();
        if (cl == null) {
            return null;        // not found
        } else {
            InputStream result = cl.getResourceAsStream(cname);
            if (result == null && (cl != this.getClass().getClassLoader())) {
                return this.getClass().getClassLoader().getResourceAsStream(cname);
            }
            return result;
        }
    }

    /**
     * Obtains the URL of the specified class file.
     * This method calls <code>getResource(String)</code>
     * on the class loader.
     *
     * @return null if the class file could not be found.
     */
    public URL find(String classname) {
        String cname = classname.replace('.', '/') + ".class";
        ClassLoader cl = (ClassLoader) clref.get();
        if (cl == null) {
            return null;        // not found
        } else {
            URL url = cl.getResource(cname);
            if (url == null && (cl != this.getClass().getClassLoader())) {
                return this.getClass().getClassLoader().getResource(cname);
            }
            return url;
        }
    }

    /**
     * Closes this class path.
     */
    public void close() {
        clref = null;
    }
}