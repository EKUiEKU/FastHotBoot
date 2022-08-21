package top.xizai.deployment.factory;

import top.xizai.deployment.entity.MethodObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE: 热部署上下文
 **/
public abstract class AbstractDeployContext<T, I> extends ConcurrentHashMap<String, T> implements DeployFactory <T>{
    /**
     * 用来缓存热部署的Class对象
     */
    public static Map<String, Class> deployClassCache = new ConcurrentHashMap<>(16);
    /**
     * 反射Method缓存
     */
    public static Map<String, Method> methodsCache = new ConcurrentHashMap<>(16);
    /**
     * 用来缓存实例化的对象
     */
    public static Map<String, Object> instanceCache = new ConcurrentHashMap<>(16);
    /**
     * 热部署的类加载器
     */
    public I deployClassLoader;


    /**
     * 设置热部署上下文的类加载器
     */
    public void setContextClassLoader(I classloader) {
        deployClassLoader = classloader;
    }

    /**
     * 获取热部署上下文的类加载器
     * @return
     */
    public I getContextClassLoader() {
        return deployClassLoader;
    }

    /**
     * 根据name和类对象获取对应的deployClassCache的Key
     * @param clazz
     * @return
     */
    public abstract String getClassKey(Class clazz);

    /**
     * 根据name和方法对象获取对应的methodsCache的Key
     * @param clazz
     * @param method
     * @return
     */
    public abstract String getMethodKey(Class clazz, Method method);

    @Override
    public Class<?> getDeployClass(String name) {
        return deployClassCache.get(name);
    }

    @Override
    public Method getDeployMethod(String name) {
        return methodsCache.get(name);
    }

    @Override
    public boolean containsDefinition(Class clazz) {
        return contains(getClassKey(clazz));
    }

    /**
     * 获取Class的实例化对象
     * @param clazz
     * @return
     */
    @Override
    public Object getInstance(Class clazz) {
        return instanceCache.get(getClassKey(clazz));
    }

    @Override
    public void putInstance(Class clazz, Object inst) {
        instanceCache.put(getClassKey(clazz), inst);
    }
}
