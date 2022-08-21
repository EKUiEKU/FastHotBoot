package top.xizai.deployment.entity;

import top.xizai.deployment.factory.DeployDefinition;

import java.io.Serializable;
import java.util.List;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE:
 **/
public class AgentParams implements Serializable {
    /**
     * 部署的类
     */
    private List<DeployDefinition> deployments;
    /**
     * 签名
     */
    private String sign;

    public List<DeployDefinition> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeployDefinition> deployments) {
        this.deployments = deployments;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
