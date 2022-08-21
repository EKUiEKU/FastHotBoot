package top.xizai.deploy.boot;

import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import com.example.hotdeploymentstarter.entity.HotDeploymentClassSet;
import com.example.hotdeploymentstarter.test.SayService;
import com.example.hotdeploymentstarter.utils.DeployUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import top.xizai.deploy.boot.HotDeploymentBootApplication;
import top.xizai.deployment.constants.FileConstants;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author: WSC
 * @DATE: 2022/7/14
 * @DESCRIBE:
 **/
public class HotDeploymentClassSetTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("pid is:" + getPid());

        while (true) {
            System.out.println(new SayService().say());
            Thread.sleep(2000);
        }
    }

    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();

        String[] names = name.split("@");


        int pid = Integer.parseInt(names[0]);
        return pid + "";
    }
}
