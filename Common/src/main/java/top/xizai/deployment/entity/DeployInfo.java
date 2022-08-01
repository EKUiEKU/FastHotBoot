package top.xizai.deployment.entity;

import top.xizai.deployment.enums.DeployType;

import java.util.List;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE:
 **/
public class DeployInfo {
    /**
     * 类的全限定名
     */
    private String classFullName;
    /**
     * 时间戳,作为Class的版本号
     */
    private Long timestamp;
    /**
     * 部署类型
     */
    private DeployType deployType;
    /**
     * 指定部署类型
     */
    private List<String> specifyMethods;
    /**
     * 热部署忽略的方法
     */
    private List<String> ignoreMethods;
    /**
     * 指定文件Class文件的HashCode
     */
    private String hashCode;

    public String getClassFullName() {
        return classFullName;
    }

    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public DeployType getDeployType() {
        return deployType;
    }

    public void setDeployType(DeployType deployType) {
        this.deployType = deployType;
    }

    public List<String> getSpecifyMethods() {
        return specifyMethods;
    }

    public void setSpecifyMethods(List<String> specifyMethods) {
        this.specifyMethods = specifyMethods;
    }

    public List<String> getIgnoreMethods() {
        return ignoreMethods;
    }

    public void setIgnoreMethods(List<String> ignoreMethods) {
        this.ignoreMethods = ignoreMethods;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }
}
