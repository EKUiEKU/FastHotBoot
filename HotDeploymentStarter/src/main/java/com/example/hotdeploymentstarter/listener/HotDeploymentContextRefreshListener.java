package com.example.hotdeploymentstarter.listener;

import com.example.hotdeploymentstarter.autoconfiguration.HotDeploymentAutoConfiguration;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * @author wsc
 * 容器刷新监听事件
 */
public class HotDeploymentContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
    private Logger log = LoggerFactory.getLogger(HotDeploymentContextRefreshListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Boot is refreshed!Hot deployment is loading class...");

        // IOC容器刷新,重新热部署
        ApplicationContext ctx = contextRefreshedEvent.getApplicationContext();
        HotDeploymentClassSet hotDeploymentClassSet = ctx.getBean(HotDeploymentClassSet.class);
        if (hotDeploymentClassSet != null) {
            hotDeploymentClassSet.readClassSetInfoFromFile();
        }
    }
}
