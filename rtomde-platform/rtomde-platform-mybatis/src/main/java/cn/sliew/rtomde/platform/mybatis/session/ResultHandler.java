package cn.sliew.rtomde.platform.mybatis.session;

public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}
