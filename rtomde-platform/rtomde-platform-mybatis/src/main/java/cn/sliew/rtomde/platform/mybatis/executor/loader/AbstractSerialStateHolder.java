package cn.sliew.rtomde.platform.mybatis.executor.loader;

import org.apache.ibatis.io.SerialFilterChecker;
import org.apache.ibatis.reflection.factory.ObjectFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSerialStateHolder implements Externalizable {

    private static final long serialVersionUID = 8940388717901644661L;
    private static final ThreadLocal<ObjectOutputStream> stream = new ThreadLocal<>();
    private byte[] userBeanBytes = new byte[0];
    private Object userBean;
    private Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
    private ObjectFactory objectFactory;
    private Class<?>[] constructorArgTypes;
    private Object[] constructorArgs;

    public AbstractSerialStateHolder() {
    }

    public AbstractSerialStateHolder(
            final Object userBean,
            final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
            final ObjectFactory objectFactory,
            List<Class<?>> constructorArgTypes,
            List<Object> constructorArgs) {
        this.userBean = userBean;
        this.unloadedProperties = new HashMap<>(unloadedProperties);
        this.objectFactory = objectFactory;
        this.constructorArgTypes = constructorArgTypes.toArray(new Class<?>[0]);
        this.constructorArgs = constructorArgs.toArray(new Object[0]);
    }

    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
        boolean firstRound = false;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = stream.get();
        if (os == null) {
            os = new ObjectOutputStream(baos);
            firstRound = true;
            stream.set(os);
        }

        os.writeObject(this.userBean);
        os.writeObject(this.unloadedProperties);
        os.writeObject(this.objectFactory);
        os.writeObject(this.constructorArgTypes);
        os.writeObject(this.constructorArgs);

        final byte[] bytes = baos.toByteArray();
        out.writeObject(bytes);

        if (firstRound) {
            stream.remove();
        }
    }

    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final Object data = in.readObject();
        if (data.getClass().isArray()) {
            this.userBeanBytes = (byte[]) data;
        } else {
            this.userBean = data;
        }
    }

    @SuppressWarnings("unchecked")
    protected final Object readResolve() throws ObjectStreamException {
        /* Second run */
        if (this.userBean != null && this.userBeanBytes.length == 0) {
            return this.userBean;
        }

        SerialFilterChecker.check();

        /* First run */
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.userBeanBytes))) {
            this.userBean = in.readObject();
            this.unloadedProperties = (Map<String, ResultLoaderMap.LoadPair>) in.readObject();
            this.objectFactory = (ObjectFactory) in.readObject();
            this.constructorArgTypes = (Class<?>[]) in.readObject();
            this.constructorArgs = (Object[]) in.readObject();
        } catch (final IOException ex) {
            throw (ObjectStreamException) new StreamCorruptedException().initCause(ex);
        } catch (final ClassNotFoundException ex) {
            throw (ObjectStreamException) new InvalidClassException(ex.getLocalizedMessage()).initCause(ex);
        }

        final Map<String, ResultLoaderMap.LoadPair> arrayProps = new HashMap<>(this.unloadedProperties);
        final List<Class<?>> arrayTypes = Arrays.asList(this.constructorArgTypes);
        final List<Object> arrayValues = Arrays.asList(this.constructorArgs);

        return this.createDeserializationProxy(userBean, arrayProps, objectFactory, arrayTypes, arrayValues);
    }

    protected abstract Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
                                                         List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
}
