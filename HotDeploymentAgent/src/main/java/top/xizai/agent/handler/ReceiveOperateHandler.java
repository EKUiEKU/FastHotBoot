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
import top.xizai.agent.HotDeploymentClassFileTransformer;
import top.xizai.agent.asm.cache.GlobalProxyCache;
import top.xizai.deployment.entity.AgentParams;
import top.xizai.deployment.entity.DeployInfo;
import top.xizai.deployment.enums.DeployType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
public class ReceiveOperateHandler implements HttpHandler {
    private static Logger log = Logger.getLogger(HotDeploymentAgent.class.getSimpleName());
    private final String secret;
    private final Instrumentation inst;

    public ReceiveOperateHandler(Instrumentation inst, String secret) {
        this.inst = inst;
        this.secret = secret;
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
        String classLoaderPath = agentParams.getClassLoaderPath();
        List<DeployInfo> deployments = agentParams.getDeployments();
        // 初始化全局数据
        GlobalProxyCache.classLoaderFullName = agentParams.getClassLoaderFullName();
        GlobalProxyCache.classLoaderPath = agentParams.getClassLoaderPath();

        for (DeployInfo deployment : deployments) {
            String deployClassFilePath = classLoaderPath
                    + File.separatorChar
                    + deployment.getClassFullName().replace('.', File.separatorChar)
                    + ".class";

            doDeployment(deployment, deployClassFilePath);
        }
    }


    /**
     * 对待部署的类进行验证,确保本地的类没有被人动过
     * @param deployInfo    部署文件的信息
     * @param classPath     部署文件的地址
     */
    public void doDeployment(DeployInfo deployInfo, String classPath) {
        // 校验预加载的Class的Hash是否一致
        String classFileHashCode = this.getFileHash(classPath);
        if (classFileHashCode.equals(deployInfo.getHashCode())) {
            try {
                DeployType deployType = deployInfo.getDeployType();
                switch (deployType) {
                    case REPLACE_METHOD:
                        // 检查是否有指定的方法名称,或者是指定的忽略的名称
                        // 没有的话直接部署整个Class文件,走下一步。
                    case REPLACE_CLASS:
                        break;
                    case ROLLBACK:
                        // 根据版本号回滚历史Class对象
                }

                this.doRealDeployment(deployInfo);
            } catch (Exception e) {
                log.log(Level.WARNING, "deploying class file {0} occur some error, error message is: {1}",
                        new Object[]{deployInfo.getClassFullName(), e.getMessage()});
            }
        } else {
            log.log(Level.WARNING, "class file {0} is be modified!", deployInfo.getClassFullName());
        }
    }

    /**
     * 执行热部署
     *
     * @param deployInfo
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     */
    public void doRealDeployment(DeployInfo deployInfo) throws ClassNotFoundException, UnmodifiableClassException {
        if (GlobalProxyCache.cacheClassFileTransformer.containsKey(deployInfo.getClassFullName())) {
            HotDeploymentClassFileTransformer transformer = GlobalProxyCache.cacheClassFileTransformer.get(deployInfo.getClassFullName());
            inst.removeTransformer(transformer);
        }
        HotDeploymentClassFileTransformer latestTransformer = new HotDeploymentClassFileTransformer(deployInfo);
        inst.addTransformer(latestTransformer, true);
        //触发transform执行
        inst.retransformClasses(Class.forName(deployInfo.getClassFullName()));

        GlobalProxyCache.cacheClassFileTransformer.put(deployInfo.getClassFullName(), latestTransformer);
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

}
