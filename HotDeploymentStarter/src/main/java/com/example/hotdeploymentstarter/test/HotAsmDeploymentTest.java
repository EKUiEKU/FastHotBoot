package com.example.hotdeploymentstarter.test;


;


import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;

import java.io.IOException;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class HotAsmDeploymentTest{
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // new HotAsmDeploymentTest().start();

        // HotDeploymentClassLoader loader = new HotDeploymentClassLoader("C:\\DevEnv");
        // Class<?> clazz = Class.forName("top.xizai.test.asm.SimpleService", true, loader);
        // Object newInstance = clazz.newInstance();
        // Method say = clazz.getMethod("say");
        // say.invoke(newInstance);

        injectMyself2HotDeploy();

        while (true) {
            Thread.sleep(1000);
            new SimpleService().say();

        }
    }



    private static void injectMyself2HotDeploy() throws IOException {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] names = name.split("@");
        String pid = names[0];

        System.out.println(pid);
        String targetClass = "top.xizai.test.asm.SimpleService";

        // String args = targetClass + "-" + HotDeploymentClassLoader.class.getName();
        // String agentPath = ResourceUtils.getFile("HotDeploymentAgent-1.0.jar").getAbsolutePath();
        // String injectorPath = ResourceUtils.getFile("HotDeploymentInjector-1.0.jar").getAbsolutePath();


        // System.out.println(agentPath);
        // System.out.println(injectorPath);
        // System.out.println(args);
    }
}
