package top.xizai.deployment.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE:
 **/
public class AgentParams implements Serializable {
    /**
     * ClassLoader的加载文件根目录
     */
    private String classLoaderPath;
    /**
     * ClassLoader的全限定名
     */
    private String classLoaderFullName;
    /**
     * 部署的类
     */
    private List<DeployInfo> deployments;
    /**
     * 签名
     */
    private String sign;

    public String getClassLoaderPath() {
        return classLoaderPath;
    }

    public void setClassLoaderPath(String classLoaderPath) {
        this.classLoaderPath = classLoaderPath;
    }

    public String getClassLoaderFullName() {
        return classLoaderFullName;
    }

    public void setClassLoaderFullName(String classLoaderFullName) {
        this.classLoaderFullName = classLoaderFullName;
    }

    public List<DeployInfo> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeployInfo> deployments) {
        this.deployments = deployments;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
