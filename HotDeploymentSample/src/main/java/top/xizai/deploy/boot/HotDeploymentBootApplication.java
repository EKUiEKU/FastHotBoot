package top.xizai.deploy.boot;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotDeploymentBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotDeploymentBootApplication.class, args);
    }

}
