package com.example.hotdeploymentstarter.classloader;

import cn.hutool.core.io.FileUtil;
import com.example.hotdeploymentstarter.utils.DeployUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: WSC
 * @DATE: 2022/7/11
 * @DESCRIBE:
 **/
public class HotDeploymentClassLoader extends ClassLoader{
    static {
        registerAsParallelCapable();
    }

    private String basePath;

    private ClassLoader parentClassLoader;

    private static Map<String, byte[]> cache = new HashMap<>();

    private static Map<String, Boolean> localClassExist = new HashMap<>();

    private static HotDeploymentClassLoader instance;

    public HotDeploymentClassLoader() {

    }

    public HotDeploymentClassLoader(String basePath) {
        if (!basePath.isEmpty()
                && basePath.charAt(basePath.length() - 1) != File.separatorChar) {
            basePath += File.separatorChar;
        }
        this.basePath = basePath;
        parentClassLoader = this.getClass().getClassLoader();
    }

    public HotDeploymentClassLoader(String basePath, ClassLoader classLoader) {
        super(classLoader);
        if (!basePath.isEmpty()
                && basePath.charAt(basePath.length() - 1) != File.separatorChar) {
            basePath += File.separatorChar;
        }
        this.basePath = basePath;
        parentClassLoader = this.getClass().getClassLoader();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            if (cache.containsKey(name)) {
                byte[] bytes = cache.get(name);
                return defineClass(bytes, 0, bytes.length);
            }

            byte[] b = loadClassFromFile(name);
            if (!Objects.isNull(b)) {
                Class<?> clazz = defineClass(name, b, 0, b.length);
                cache.put(name, b);
                return clazz;
            }

            //从父加载器找
            return parentClassLoader.loadClass(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private byte[] loadClassFromFile(String name) throws IOException {
        String fileName = DeployUtils.convertPackageName2Path(name) + ".class";
        String filePath = this.basePath + fileName;

        Boolean exist = localClassExist.get(filePath);
        if (exist != null && exist == Boolean.FALSE) {
            return null;
        }

        if (!FileUtil.exist(filePath)) {
            localClassExist.put(fileName, Boolean.FALSE);
            return null;
        }else {
            localClassExist.put(fileName, Boolean.TRUE);
        }

        try (InputStream inputStream = new FileInputStream(filePath);
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()
        ) {
            int nextValue;
            while ((nextValue = inputStream.read()) != -1) {
                byteStream.write(nextValue);
            }
            return byteStream.toByteArray();
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = this.findClass(name);

        if (c == null) {
            super.loadClass(name, resolve);
        }else {
            resolveClass(c);
        }
        return c;
    }

    public static HotDeploymentClassLoader getInstance() {
        // if (instance == null) {
        //     instance = new HotDeploymentClassLoader("C:\\DevEnv");
        // }
        // return instance;

        return new HotDeploymentClassLoader("C:\\DevEnv");
    }
}
