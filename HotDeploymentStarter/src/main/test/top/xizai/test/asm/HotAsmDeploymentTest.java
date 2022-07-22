package top.xizai.test.asm;

import cn.hutool.core.lang.Console;
import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class HotAsmDeploymentTest extends Thread{
    public static void main(String[] args) throws InterruptedException, AgentLoadException, IOException, AttachNotSupportedException, AgentInitializationException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // new HotAsmDeploymentTest().start();

        HotDeploymentClassLoader loader = new HotDeploymentClassLoader("C:\\DevEnv");
        Class<?> clazz = Class.forName("top.xizai.test.asm.SimpleService", true, loader);
        Object newInstance = clazz.newInstance();
        Method say = clazz.getMethod("say");
        say.invoke(newInstance);

        while (true) {
            SimpleService simpleService = new SimpleService();
            simpleService.say();
            injectMyself2HotDeploy();
            Thread.sleep(5000);
        }
    }


    @Override
    public void run() {
        try {
            Thread.sleep(3000);
            injectMyself2HotDeploy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AttachNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (AgentLoadException e) {
            throw new RuntimeException(e);
        } catch (AgentInitializationException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void injectMyself2HotDeploy() throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] names = name.split("@");
        String pid = names[0];

        System.out.println(pid);
        String targetClass = "top.xizai.test.asm.SimpleService";

        // String args = targetClass + "-" + HotDeploymentClassLoader.class.getName();
        // String agentPath = ResourceUtils.getFile("HotDeploymentAgent-1.0.jar").getAbsolutePath();
        // String injectorPath = ResourceUtils.getFile("HotDeploymentInjector-1.0.jar").getAbsolutePath();


        // System.out.println(agentPath);
        // System.out.println(injectorPath);
        // System.out.println(args);
    }
}
