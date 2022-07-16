package com.example.hotdeploymentstarter.listener;

import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author wsc
 * 容器启动事件
 */
public class HotDeploymentStartListener implements ApplicationListener<ApplicationStartedEvent> {
    private Logger log = LoggerFactory.getLogger(HotDeploymentStartListener.class);

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        log.info("Hot deployment is ready!");
    }
}
