package com.example.hotdeploymentstarter.test;

import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/7/18
 * @DESCRIBE:
 **/
public class SimpleService {
    public void say() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String aa = new SayService().say();
        System.out.println(aa);

        // Class<?> clazz = HotDeploymentClassLoader.getInstance().findClass("com.example.hotdeploymentstarter.test.SayService");
        // Object instance = clazz.newInstance();
        // Method say = clazz.getMethod("say");
        // String aa = (String) say.invoke(instance);
        // System.out.println(aa);
    }
}
