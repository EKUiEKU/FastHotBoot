package com.example.hotdeploymentstarter.autoconfiguration;

import com.example.hotdeploymentstarter.entity.HotDeployProperties;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import com.example.hotdeploymentstarter.handler.ReceiveClassHandler;
import com.example.hotdeploymentstarter.utils.DeployUtils;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author wsc
 * 自动配置
 */
@Configuration
@EnableConfigurationProperties(HotDeployProperties.class)
public class HotDeploymentAutoConfiguration {
    private Logger log = LoggerFactory.getLogger(HotDeploymentAutoConfiguration.class);

    @Bean
    @ConditionalOnClass(HttpServer.class)
    public DeployUtils deployUtils(HotDeployProperties properties, ConfigurableApplicationContext ctx) throws IOException {
        return new DeployUtils(properties, ctx);
    }

    @Bean
    @ConditionalOnClass(HttpServer.class)
    public HttpServer httpServer(HotDeployProperties properties, DeployUtils deployUtils, HotDeploymentClassSet deploymentClassSet) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(properties.getPort()), 0);
        httpServer.start();
        log.info("Hot department is listen port on {}.", properties.getPort());

        //监听事件
        httpServer.createContext("/deploy", new ReceiveClassHandler(deployUtils, deploymentClassSet));

        return httpServer;
    }



    @Bean
    @ConditionalOnClass(HttpServer.class)
    public HotDeploymentClassSet hotDeploymentClasses() {
        return new HotDeploymentClassSet();
    }
}
