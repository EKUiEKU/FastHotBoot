package top.xizai.starter.test;

import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import com.example.hotdeploymentstarter.utils.DeployUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;
import top.xizai.deploy.boot.HotDeploymentBootApplication;
import top.xizai.deployment.constants.FileConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author: WSC
 * @DATE: 2022/7/14
 * @DESCRIBE:
 **/
@SpringBootTest(classes = HotDeploymentBootApplication.class)
public class HotDeploymentClassSetTest {
    @Autowired
    HotDeploymentClassSet hotDeploymentClasses;

    @Test
    public void batchInsertAndSaveTest() {
        HotDeploymentClass class1 = new HotDeploymentClass();
        class1.setClassPath("~/");
        class1.setClassName("Simple.class");
        class1.setUploader("admin");
        class1.setUploadTime(System.currentTimeMillis());
        class1.setHash("adadasfrefer");
        class1.setUploadIp("127.0.0.1");

        hotDeploymentClasses.add(class1);
    }

    @Test
    public void createAndReadFileTest() {
        Iterator<HotDeploymentClass> iterator = hotDeploymentClasses.iterator();
        while (iterator.hasNext()) {
            HotDeploymentClass next = iterator.next();
            System.out.println(next.toString());
        }
    }

    @Test
    public void startAgentTest() throws FileNotFoundException {
        File agentFile = ResourceUtils.getFile(FileConstants.AGENT_NAME);
        File injectorFile = ResourceUtils.getFile(FileConstants.INJECTOR_NAME);


        System.out.println(injectorFile.exists());
        System.out.println(agentFile.getAbsolutePath());
        System.out.println(injectorFile.getAbsolutePath());

        List<String> list = Arrays.asList("8848", agentFile.getAbsolutePath(), "9291-abcd");
        DeployUtils.executeJar(injectorFile.getAbsolutePath(), list);
    }
}
