package top.xizai.starter.test;

import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xizai.deploy.boot.HotDeploymentBootApplication;

import java.util.Iterator;

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
}
