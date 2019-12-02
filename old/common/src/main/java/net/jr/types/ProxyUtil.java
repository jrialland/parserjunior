package net.jr.types;

import net.jr.text.ConsoleColors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyUtil {

    public static <T> T nullProxy(Class<T> clazz) {
        InvocationHandler ih = (o, method, objects) -> null;
        return (T) Proxy.newProxyInstance(ProxyUtil.class.getClassLoader(), new Class<?>[]{clazz}, ih);
    }

    /**
     * proxies an object by printing the method's name on stderr each time it is called
     *
     * @param iface
     * @param impl
     * @param <T>
     * @return
     */
    public static <T> T snitchingProxy(Class<T> iface, T impl) {
        InvocationHandler ih = new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                System.err.println(Thread.currentThread().getStackTrace()[3].toString() + "\t=>\t" + ConsoleColors.green(iface.getSimpleName() + "::" + method.getName()));
                return method.invoke(impl, objects);
            }
        };
        return (T) Proxy.newProxyInstance(impl.getClass().getClassLoader(), new Class<?>[]{iface}, ih);
    }
}
