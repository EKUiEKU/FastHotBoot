package top.xizai.deployment.factory;

import java.lang.reflect.Method;

/**
 * 热部署承载热部署信息的容器接口
 *
 * @author: WSC
 * @DATE: 2022/8/20
 **/
public interface DeployFactory<T> {

    /**
     * 设置热部署类的信息
     * @param definition 类的定义信息
     */
    void setDeployDefinition(T definition);


    /**
     * 根据类获取到热部署类的信息
     * @param clazz
     * @return
     */
    T getDeployDefinition(Class clazz);

    /**
     * 根据类名称获取到热部署类的信息
     * @param name
     * @return
     */
    T getDeployDefinition(String name);

    /**
     * 获取热部署的类
     * @param name
     * @return
     */
    Class<?> getDeployClass(String name);

    /**
     * 获取热部署的方法
     * @param name
     * @return
     */
    Method getDeployMethod(String name);

    /**
     * 容器中是否包含热部署类的信息
     * @param clazz
     * @return
     */
    boolean containsDefinition(Class clazz);

    /**
     * 获取类的实例对象
     * @param clazz
     * @return
     */
    Object getInstance(Class clazz);

    /**
     * 根据Class设置类的实例对象
     * @param clazz
     * @param inst
     */
    void putInstance(Class clazz, Object inst);
}
