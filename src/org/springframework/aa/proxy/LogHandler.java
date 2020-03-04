package org.springframework.aa.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LogHandler implements InvocationHandler {
    Object target;  // 被代理的对象，实际的方法执行者

    public LogHandler(Object target) {
        this.target = target;
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        Object result = method.invoke(target, args);  // 调用 target 的 method 方法
        after();
        return result;  // 返回方法的执行结果
    }

    // 调用invoke方法之前执行
    private void before() {
        System.out.println("log start");
    }
    // 调用invoke方法之后执行
    private void after() {
        System.out.println("log end");
    }
}