package com.example.hotdeploymentstarter.test;

import java.lang.reflect.InvocationTargetException;

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


        long start = System.currentTimeMillis();
        String aa = new SayService().say();
        System.out.println(aa);

        System.out.println("==============");
        System.out.println((System.currentTimeMillis() - start) + "ms.");

    }
}
