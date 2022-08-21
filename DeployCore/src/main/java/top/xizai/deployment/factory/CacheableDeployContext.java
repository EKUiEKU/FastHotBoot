package top.xizai.deployment.factory;

import top.xizai.deployment.entity.MethodObject;
import top.xizai.deployment.enums.DeployType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE:
 **/
public abstract class CacheableDeployContext
                        extends AbstractDeployContext<DeployDefinition, DeployClassLoader> implements SecurityFactory {

    @Override
    public String getClassKey(Class clazz) {
        String classKey = "";
        if (clazz != null) {
            classKey = clazz.getName();
        }

        return classKey;
    }

    @Override
    public String getMethodKey(Class clazz, Method method) {
        String methodParams = Arrays.stream(method.getParameterTypes())
                .map(m -> m.getName())
                .collect(Collectors.joining("$"));
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName())
                .append("$")
                .append(method.getName())
                .append("$")
                .append(methodParams);

        return sb.toString();
    }

    @Override
    public void setDeployDefinition(DeployDefinition definition) {
        resolveClassCacheIfNecessary(definition);
        if (definition != null && deployClassCache.containsKey(definition.getFullPackageName())) {

            Class clazz = deployClassCache.get(definition.getFullPackageName());

            if (clazz != null) {
                put(getClassKey(clazz), definition);
            }
        }
    }

    @Override
    public DeployDefinition getDeployDefinition(Class clazz) {
        return get(getClassKey(clazz));
    }

    @Override
    public DeployDefinition getDeployDefinition(String name) {
        return get(name);
    }

    /**
     * 根据热部署的信息加载对应的对象
     * 假如是REPLACE_CLASS 模式则解析类中的所有合法的Method对象
     * 假如是REPLACE_METHOD 模式则解析类中指定的Method对象
     */
    private void resolveClassCacheIfNecessary(DeployDefinition definition) {
        try {
            DeployClassLoader classLoader = DeployClassLoader.getInstance();
            Class<?> clazz = Class.forName(definition.getFullPackageName(), true, classLoader);
            deployClassCache.put(getClassKey(clazz), clazz);

            DeployType type = definition.getDeployType();
            List<String> specifyMethods = definition.getSpecifyMethods();
            List<String> ignoreMethods = definition.getIgnoreMethods();

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                String methodKey = getMethodKey(clazz, method);

                if (type.equals(DeployType.ROLLBACK)) {
                    // 回滚
                    if (methodsCache.containsKey(methodKey)) {
                        methodsCache.remove(methodKey);
                    }
                } else {
                    // 全部署或者部分部署
                    if (type.equals(DeployType.REPLACE_METHOD) && (!specifyMethods.contains(method.getName())) || ignoreMethods.contains(method.getName())) {
                        continue;
                    }

                    methodsCache.put(methodKey, method);
                }
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将上下文的数据刷入磁盘中
     */
    public abstract void flushCacheInDisk();



}
