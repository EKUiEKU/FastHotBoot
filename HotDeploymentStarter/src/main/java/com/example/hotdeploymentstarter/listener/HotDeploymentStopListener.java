package com.example.hotdeploymentstarter.listener;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author wsc
 * 容器启动事件
 */
public class HotDeploymentStopListener implements ApplicationListener<ContextClosedEvent> {
    private Logger log = LoggerFactory.getLogger(HotDeploymentStopListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Hot deployment is closing!");

        ApplicationContext ctx = event.getApplicationContext();
        HttpServer httpServer = ctx.getBean(HttpServer.class);
        if (httpServer != null) {
            // waiting http close 2 sec.
            httpServer.stop(2);
        }
    }
}
