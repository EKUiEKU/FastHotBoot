package top.xizai.agent.asm.cache;

import top.xizai.agent.HotDeploymentClassFileTransformer;
import top.xizai.deployment.entity.ByteObject;
import top.xizai.deployment.entity.ClassObject;
import top.xizai.deployment.entity.MethodObject;

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
     * TODO 有没有必要将缓存刷盘到磁盘上? 哪些信息需要缓存？
     * TODO 程序重启后,是否需要重新加载缓存?
     * TODO 是否需要提供接口.让后台管理系统可以访问到缓存的信息,比如版本号用于热部署的回滚？
     * TODO 服务器发布新版本后该有什么缓存清除策略,防止影响到新版本的功能
     * TODO 是否需要全局定义一个服务器的版本号？
     */

    /**
     * Method的缓存,用来映射
     * Key的命名规则: Class的权限定名 + '$' + MethodName + '$' + MethodParams(参数间$隔开)
     */
    public static Map<String, MethodObject> methodsCache = new HashMap<>();
    /**
     * 用来缓存热部署的Class对象
     */
    public static Map<String, ClassObject> deployClassCache = new HashMap<>();
    /**
     * 用来缓存实例化的对象
     */
    public static Map<String, Object> instanceCache = new HashMap<>();

    /**
     * 热部署字节码的路径
     */
    public static String classLoaderPath;
    /**
     * 类加载器的全限定名
     */
    public static String classLoaderFullName;
    /**
     * 类加载器对象
     */
    public static ClassLoader classLoader;
    /**
     * 缓存编辑后的字节码
     */
    public static Map<String, ByteObject> deploymentByteMap = new HashMap<>();
    /**
     * 缓存原始的字节码
     */
    public static Map<String, Stack<ByteObject>> originByteMap = new HashMap<>();

    public static Map<String, HotDeploymentClassFileTransformer> cacheClassFileTransformer = new HashMap<>();

}
