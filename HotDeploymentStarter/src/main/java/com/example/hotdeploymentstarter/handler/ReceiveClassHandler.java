package com.example.hotdeploymentstarter.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.multipart.MultipartFormData;
import cn.hutool.core.net.multipart.UploadFile;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import com.example.hotdeploymentstarter.utils.DeployUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
public class ReceiveClassHandler implements HttpHandler {
    private Logger log = LoggerFactory.getLogger(ReceiveClassHandler.class);

    private DeployUtils deployUtils;

    private HotDeploymentClassSet deploymentClassSet;

    public ReceiveClassHandler(DeployUtils deployUtils, HotDeploymentClassSet deploymentClassSet) {
        this.deployUtils = deployUtils;
        this.deploymentClassSet = deploymentClassSet;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        InputStream requestBody = exchange.getRequestBody();
        InetAddress remoteIp = exchange.getRemoteAddress().getAddress();

        List<HotDeploymentClass> tmpDeploymentClass = new ArrayList<>();


        if ("put".equalsIgnoreCase(requestMethod)) {
            MultipartFormData formData = new MultipartFormData();
            formData.parseRequestStream(requestBody, StandardCharsets.UTF_8);
            requestBody.close();

            String packageNames = formData.getParam("packageNames");
            if (ObjectUtils.isEmpty(packageNames)) {
                String msg = "request params is not contain <packageNames>!";
                sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, msg);
                return;
            }
            Map packageMap = JSON.parseObject(packageNames, Map.class);

               Set<String> fileParamNames = formData.getFileParamNames();
            for (String fileName : fileParamNames) {
                if (ObjectUtils.isEmpty(fileName)) {
                    sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, "file name is empty!");
                    return;
                }

                if (!fileName.endsWith(".class")) {
                    String msg = "file name <%s> is not legal,please ends with <.class>!";
                    sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, String.format(msg, fileName));
                    return;
                }

                if (!packageMap.containsKey(fileName)) {
                    String msg = "file name <%s> is not contain package info,please add info to packageNames!";
                    sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, String.format(msg, fileName));
                    return;
                }

                // 将数据保存到本地
                String classPath = deployUtils.getDeployClassPath();
                String fullPackage = packageMap.get(fileName) + "";
                String filePath = classPath + File.separatorChar + DeployUtils.convertPackageName2Path(fullPackage) + ".class";
                UploadFile file = formData.getFile(fileName);
                FileUtil.writeBytes(file.getFileContent(), filePath);

                if (!deployUtils.verifyClassBytesLegal(filePath)) {
                    String msg = "class name: <%s> is not a legal class file!";
                    sendResponseMessage(exchange, HttpStatus.HTTP_BAD_REQUEST, String.format(msg, fileName));
                    return;
                }

                // 写入HotDeploymentClassSet中
                HotDeploymentClass deployClassInfo = new HotDeploymentClass();
                deployClassInfo.setHash(deployUtils.getFileHash(filePath));
                deployClassInfo.setClassPath(filePath);
                deployClassInfo.setClassName(fileName);
                deployClassInfo.setUploadIp(remoteIp.getHostAddress());
                deployClassInfo.setUploadTime(System.currentTimeMillis());
                deployClassInfo.setFullPackageName(fullPackage);
                //TODO 权限访问,最后再写
                deployClassInfo.setUploader("admin");

                tmpDeploymentClass.add(deployClassInfo);
            }


            for (HotDeploymentClass deploymentClass : tmpDeploymentClass) {
                log.info("receive <{}> file from remote host <{}>.", deploymentClass.getClassName(), remoteIp.getHostAddress());
            }
            deploymentClassSet.addAll(tmpDeploymentClass);
            // 热部署
            if (deployUtils.hotDeployment(tmpDeploymentClass)) {
                sendResponseMessage(exchange, HttpStatus.HTTP_OK, "success");
            }else {
                sendResponseMessage(exchange, HttpStatus.HTTP_INTERNAL_ERROR, "fail to find class.");
            }
        }else {
            sendResponseMessage(exchange, HttpStatus.HTTP_UNSUPPORTED_TYPE, "unsupported this request type!");
            return;
        }
    }


    public void sendResponseMessage(HttpExchange e, int respCode, String msg) throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", respCode);
        resp.put("msg", msg);

        //发送200状态码
        e.sendResponseHeaders(200, 0);
        e.getResponseHeaders().set("Content-Type", "application/json");

        OutputStream os = e.getResponseBody();
        IoUtil.writeUtf8(os, true, JSON.toJSONString(resp));
    }

}
