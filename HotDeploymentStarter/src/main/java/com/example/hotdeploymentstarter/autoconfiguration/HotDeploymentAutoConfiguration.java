package com.example.hotdeploymentstarter.autoconfiguration;

import cn.hutool.core.io.FileUtil;
import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;
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
import org.springframework.util.ResourceUtils;
import top.xizai.deployment.constants.FileConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

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
        //注册部署类的根目录
        HotDeploymentClassLoader.resignDeployClassPath(deployUtils.getDeployClassPath());

        this.injectorDeployAgent(properties.getAgentPort(), properties.getSalt());
        return httpServer;
    }

    /**
     * 启动热部署代理
     *
     * @throws FileNotFoundException
     */
    private void injectorDeployAgent(Integer agentPort, String salt) throws FileNotFoundException {
        /**
         * 将本地的jar文件放置系统目录
         */
        InputStream agentInputStream = this.getClass().getClassLoader().getResourceAsStream(FileConstants.AGENT_NAME);
        InputStream injectorInputStream = this.getClass().getClassLoader().getResourceAsStream(FileConstants.INJECTOR_NAME);

        String agentHomePath = FileUtil.getAbsolutePath(new File(FileConstants.AGENT_HOME));

        File agentFile = new File(agentHomePath + File.separatorChar + FileConstants.AGENT_NAME);
        File injectorFile = new File(agentHomePath + File.separatorChar + FileConstants.INJECTOR_NAME);

        FileUtil.writeFromStream(agentInputStream, agentFile);
        FileUtil.writeFromStream(injectorInputStream, injectorFile);

        // 获取当前进程的PID
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] names = name.split("@");
        String pid = names[0];

        // Agent的参数
        String options = agentPort + "-" + salt;

        List<String> list = Arrays.asList(pid, agentFile.getAbsolutePath(), options);
        DeployUtils.executeJar(injectorFile.getAbsolutePath(), list);
    }


    @Bean
    @ConditionalOnClass(HttpServer.class)
    public HotDeploymentClassSet hotDeploymentClasses() {
        return new HotDeploymentClassSet();
    }
}
