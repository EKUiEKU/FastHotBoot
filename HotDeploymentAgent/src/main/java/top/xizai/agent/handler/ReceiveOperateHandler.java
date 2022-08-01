package top.xizai.agent.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import top.xizai.agent.HotDeploymentAgent;
import top.xizai.deployment.entity.AgentParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.util.*;
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
        Headers requestHeaders = exchange.getRequestHeaders();
        InputStream requestBody = exchange.getRequestBody();



        if ("put".equalsIgnoreCase(requestMethod)) {
            try {
                String body = IoUtil.readUtf8(requestBody);
                AgentParams agentParams = JSON.parseObject(body, AgentParams.class);
                doHandler(agentParams);
            }catch (Throwable e) {
                sendResponseMessage(exchange, HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
            }
        }else {
            sendResponseMessage(exchange, HttpStatus.HTTP_UNSUPPORTED_TYPE, "unsupported this request type!");
            return;
        }
    }


    public void doHandler(AgentParams agentParams) {
        // TODO 开始验证解析
        throw new UnsupportedOperationException("Undeveloped..");
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
