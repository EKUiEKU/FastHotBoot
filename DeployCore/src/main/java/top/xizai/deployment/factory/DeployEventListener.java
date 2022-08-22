package top.xizai.deployment.factory;

import top.xizai.deployment.entity.DeployInfo;

import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/8/21
 * @DESCRIBE: 热部署的监听事件
 **/
public interface DeployEventListener {
    /**
     * 开始部署事件
     * @param definition
     */
    void onStartDeploy(DeployDefinition definition);

    /**
     * 热部署类对象中的方法事件
     * @param definition
     * @param method
     */
    void onDeployMethod(DeployDefinition definition, Method method);

    /**
     * 热部署类对象中的方法发生异常事件
     * @param definition
     * @param method
     * @param e
     */
    void onDeployMethodException(DeployDefinition definition, Method method, Exception e);

    /**
     * 完成部署事件
     * @param definition
     */
    void onFinishDeploy(DeployDefinition definition);

    /**
     * 回滚部署事件
     * @param definition
     */
    void onRollbackDeploy(DeployDefinition definition);

    /**
     * 校验消息的结果事件
     * @param success   是否成功
     * @param obj       校验的对象
     * @param cause     如果失败,失败的原因
     */
    void onValidateMessage(Boolean success, Object obj, String cause);

    /**
     * 校验即将要热部署的Class是否合法
     * @param success   是否成功
     * @param obj       校验的对象
     * @param cause     如果失败,失败的原因
     */
    void onValidateClass(Boolean success, Object obj, String cause);
}
