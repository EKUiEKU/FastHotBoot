package top.xizai.agent.asm.cache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author: WSC
 * @DATE: 2022/7/31
 * @DESCRIBE: 全局反射缓存类
 **/
public class GlobalProxyCache {
    /**
     * Method的缓存,用来映射
     * Key的命名规则: Class的权限定名 + '$' + MethodName + '$' + MethodParams(参数间$隔开)
     */
    public static Map<String, Method> methodsCache = new HashMap<>();
    /**
     * 用来缓存热部署的Class对象
     */
    public static Map<String, Object> deployClassCache = new HashMap<>();
    /**
     * 用来缓存实例化的对象
     */
    public static Map<String, Object> instanceCache = new HashMap<>();

    /**
     * 热部署字节码的路径
     */
    public static String classLoaderPath = "C:\\DevEnv";
    /**
     * 类加载器的全限定名
     */
    public static String classLoaderFullName = "com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader";
    /**
     * 类加载器对象
     */
    public static ClassLoader classLoader;
    /**
     * 缓存编辑后的字节码
     */
    public static Map<String, Object> deploymentByteMap = new HashMap<>();
    /**
     * 缓存原始的字节码
     */
    public static Map<String, Stack<Object>> originByteMap = new HashMap<>();

}
