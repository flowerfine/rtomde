package cn.sliew.rtomde.bind;

/**
 * 它对标的是jdbc的PrepareStatement，将带有?占位符的sql设置到里面，
 * 然后对每个占位符一一设置参数，最后得到一个可以真正执行的Statement。
 */
public interface QueryStatement extends AutoCloseable {

    ResultStatement executeQuery(Query query);

    int getQueryTimeout();

    void setQueryTimeout(int seconds);

    void cancel();
}
