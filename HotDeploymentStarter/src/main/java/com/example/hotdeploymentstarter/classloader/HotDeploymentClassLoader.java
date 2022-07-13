package com.example.hotdeploymentstarter.classloader;

import java.io.*;

/**
 * @author: WSC
 * @DATE: 2022/7/11
 * @DESCRIBE:
 **/
public class HotDeploymentClassLoader extends ClassLoader{
    static {
        registerAsParallelCapable();
    }

    private final String basePath;

    public HotDeploymentClassLoader(String basePath) {
        if (!basePath.isEmpty()
                && basePath.charAt(basePath.length() - 1) != File.separatorChar) {
            basePath += File.separatorChar;
        }
        this.basePath = basePath;
    }

    public HotDeploymentClassLoader(String basePath, ClassLoader classLoader) {
        super(classLoader);
        if (!basePath.isEmpty()
                && basePath.charAt(basePath.length() - 1) != File.separatorChar) {
            basePath += File.separatorChar;
        }
        this.basePath = basePath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = loadClassFromFile(name);
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private byte[] loadClassFromFile(String name) throws IOException {
        String fileName = name.replace('.', File.separatorChar) + ".class";
        String filePath = this.basePath + fileName;

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
        return super.loadClass(name, resolve);
    }
}
