package com.example.hotdeploymentstarter.autoconfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wsc
 */
@Component
@ConfigurationProperties(prefix = "boot.deploy")
public class HotDeployProperties {
    /**
     * 是否开启热部署
     */
    private Boolean enable;
    /**
     * 开启远程更新,监听的端口
     */
    private Integer port = 9821;
    /**
     * 热更新文件保存的路径
     */
    private String classFilePath = "~";

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClassFilePath() {
        return classFilePath;
    }

    public void setClassFilePath(String classFilePath) {
        this.classFilePath = classFilePath;
    }
}
