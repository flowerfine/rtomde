package cn.sliew.rtomde.service.bytecode.dispatcher;

public final class NameUtil {

    private static final String delimiter = ".";

    private NameUtil() {
        throw new IllegalStateException("can't do this");
    }

    public static String namespace(String applyNamespaceId) {
        int index = applyNamespaceId.lastIndexOf(delimiter);
        if (index != -1) {
            return applyNamespaceId.substring(0, index);
        }
        return applyNamespaceId;
    }

    public static String mappedStatementId(String applyNamespaceId) {
        int index = applyNamespaceId.lastIndexOf(delimiter);
        if (index != -1) {
            return applyNamespaceId.substring(index + 1);
        }
        return applyNamespaceId;
    }

    public static void main(String[] args) {
        String name1 = "cn.sliew.index.UserMapper.select";
        String name2 = "select";
        System.out.println(namespace(name1));
        System.out.println(mappedStatementId(name1));
        System.out.println(namespace(name2));
        System.out.println(mappedStatementId(name2));
    }
}