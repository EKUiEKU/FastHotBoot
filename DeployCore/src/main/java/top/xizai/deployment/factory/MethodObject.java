package top.xizai.deployment.factory;

import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/8/4
 * @DESCRIBE: 方法对象(携带版本号)
 **/
public class MethodObject {
    /**
     * 反射里面的方法
     */
    private Method method;
    /**
     * 版本号
     */
    private Long version;
    /**
     * 方法名
     */
    private String methodName;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public MethodObject(Method method, Long version, String methodName) {
        this.method = method;
        this.version = version;
        this.methodName = methodName;
    }
}
