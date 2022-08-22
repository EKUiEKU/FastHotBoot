package top.xizai.agent.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SignUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import top.xizai.agent.HotDeploymentAgent;
import top.xizai.deployment.entity.AgentParams;
import top.xizai.deployment.enums.DeployType;
import top.xizai.deployment.factory.AsmCacheableDeployContext;
import top.xizai.deployment.factory.DeployDefinition;
import top.xizai.deployment.factory.DeployEventListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
public class ReceiveOperateHandler implements HttpHandler, DeployEventListener {
    private static Logger log = Logger.getLogger(HotDeploymentAgent.class.getSimpleName());
    private final String secret;
    private final Instrumentation inst;

    private AsmCacheableDeployContext deployContext;

    private String classFileLoaderPath;

    private CopyOnWriteArrayList<String> cacheOperaHistory = new CopyOnWriteArrayList<>();

    public ReceiveOperateHandler(Instrumentation inst, String secret, String classFileLoaderPath) {
        this.inst = inst;
        this.secret = secret;
        this.classFileLoaderPath = classFileLoaderPath;

        deployContext = new AsmCacheableDeployContext(inst, classFileLoaderPath);
        deployContext.addDeployEventListener(this);
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        InputStream requestBody = exchange.getRequestBody();

        if ("post".equalsIgnoreCase(requestMethod)) {
            try {
                String body = IoUtil.readUtf8(requestBody);
                AgentParams agentParams = JSON.parseObject(body, AgentParams.class);
                /**
                 * 校验发过来的参数签是否正确
                 */
                Map mapWithSign = JSON.parseObject(JSON.toJSONString(agentParams), Map.class);
                mapWithSign.remove("sign");
                String sign = SignUtil.signParamsMd5(mapWithSign);

                if (!sign.equals(agentParams.getSign())) {
                    System.out.println("sign:" + sign);
                    sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, "bad sign!");
                    return;
                }

                doHandler(agentParams);

                sendResponseMessage(exchange, HttpStatus.HTTP_OK, "success");
            } catch (Throwable e) {
                e.printStackTrace();
                sendResponseMessage(exchange, HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
            }
        } else {
            sendResponseMessage(exchange, HttpStatus.HTTP_UNSUPPORTED_TYPE, "unsupported this request type!");
            return;
        }
    }


    /**
     * 处理部署的事件
     * @param agentParams
     */
    public void doHandler(AgentParams agentParams) {
        List<DeployDefinition> deployments = agentParams.getDeployments();

        for (DeployDefinition deployment : deployments) {
            String deployClassFilePath = classFileLoaderPath
                    + File.separatorChar
                    + deployment.getFullPackageName().replace('.', File.separatorChar)
                    + ".class";

            doDeployment(deployment, deployClassFilePath);
        }
    }


    /**
     * 对待部署的类进行验证,确保本地的类没有被人动过
     * @param definition    部署文件的信息
     * @param classPath     部署文件的地址
     */
    public void doDeployment(DeployDefinition definition, String classPath) {
        // 校验预加载的Class的Hash是否一致
        String classFileHashCode = this.getFileHash(classPath);
        if (classFileHashCode.equals(definition.getHash())) {
            try {
                DeployType deployType = definition.getDeployType();
                switch (deployType) {
                    case REPLACE_METHOD:
                        // 检查是否有指定的方法名称,或者是指定的忽略的名称
                        // 没有的话直接部署整个Class文件,走下一步。
                    case REPLACE_CLASS:
                        break;
                    case ROLLBACK:
                        // 根据版本号回滚历史Class对象
                }

                this.doRealDeployment(definition);
            } catch (Exception e) {
                log.log(Level.WARNING, "deploying class file {0} occur some error, error message is: {1}",
                        new Object[]{definition.getFullPackageName(), e.getMessage()});
                e.printStackTrace();
            }
        } else {
            log.log(Level.WARNING, "class file {0} is be modified!", definition.getFullPackageName());
        }
    }

    /**
     * 执行热部署
     *
     * @param definition
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     */
    public void doRealDeployment(DeployDefinition definition) throws ClassNotFoundException, UnmodifiableClassException {
        // if (GlobalProxyCache.cacheClassFileTransformer.containsKey(deployInfo.getClassFullName())) {
        //     HotDeploymentClassFileTransformer transformer = GlobalProxyCache.cacheClassFileTransformer.get(deployInfo.getClassFullName());
        //     inst.removeTransformer(transformer);
        // }
        // HotDeploymentClassFileTransformer latestTransformer = new HotDeploymentClassFileTransformer(deployInfo);
        // inst.addTransformer(latestTransformer, true);
        // //触发transform执行
        // inst.retransformClasses(Class.forName(deployInfo.getClassFullName()));
        //
        // GlobalProxyCache.cacheClassFileTransformer.put(deployInfo.getClassFullName(), latestTransformer);

        deployContext.setDeployDefinition(definition);
        deployContext.deploy(definition);
    }


    public void sendResponseMessage(HttpExchange e, int respCode, String msg) throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", respCode);
        resp.put("msg", msg);

        //发送200的状态码
        e.sendResponseHeaders(200, 0);
        e.getResponseHeaders().set("Content-Type", "application/json");

        OutputStream os = e.getResponseBody();
        IoUtil.writeUtf8(os, true, JSON.toJSONString(resp));
    }

    public String getFileHash(String filePath) {
        String fileString = FileUtil.readString(filePath, StandardCharsets.UTF_8);
        return md5(fileString);
    }

    public String md5(String source) {
        return SecureUtil.md5(secret + source + secret);
    }

    /**
     * 向Web发送操作记录
     */
    public void sendOperaLogBatch() {

    }

    /**
     * 向缓存中插入日志
     * @param msg
     */
    public void insertOperaLog(String msg) {
        cacheOperaHistory.add(msg);
    }


    // 热部署的各种消息事件
    // ======↓↓↓======

    @Override
    public void onStartDeploy(DeployDefinition definition) {

    }

    @Override
    public void onDeployMethod(DeployDefinition definition, Method method) {

    }

    @Override
    public void onDeployMethodException(DeployDefinition definition, Method method, Exception e) {

    }

    @Override
    public void onFinishDeploy(DeployDefinition definition) {

    }

    @Override
    public void onRollbackDeploy(DeployDefinition definition) {

    }

    @Override
    public void onValidateMessage(Boolean success, Object obj, String cause) {

    }

    @Override
    public void onValidateClass(Boolean success, Object obj, String cause) {

    }

    // ======↑↑↑======
    // 热部署的各种消息事件
}
