package net.jr.types;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxyUtil {

    public static <T> T nullProxy(Class<T> clazz) {
        InvocationHandler ih = (o, method, objects) -> null;
        return (T) Proxy.newProxyInstance(ProxyUtil.class.getClassLoader(), new Class<?>[]{clazz}, ih);
    }
}
