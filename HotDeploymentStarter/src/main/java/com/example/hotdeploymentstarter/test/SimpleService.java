package com.example.hotdeploymentstarter.test;

import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/7/18
 * @DESCRIBE: 模拟一个Component
 **/
public class SimpleService {
    public void say() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        /**
         * 调用一个待更新的对象
         */

        /**
         * 将SayService变成一个接口ISayService
         * 加载SayService字节,并且通过ASM继承ISayService接口,最后实例并且强转成ISayService
         * 调用ISayService接口 即可
         */
        String aa = new SayService().say();
        System.out.println(aa);

        String s = new String("==============");
        System.out.println(s);
        // Class<?> clazz = HotDeploymentClassLoader.getInstance().findClass("com.example.hotdeploymentstarter.test.SayService");
        // Object instance = clazz.newInstance();
        // Method say = clazz.getMethod("say");
        // String aa = (String) say.invoke(instance);
        // System.out.println(aa);
    }
}
