package top.xizai.hotdeployment.injector;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class InjectorMain {
    /**
     * @param args pid jar-path args
     * @throws IOException
     * @throws AttachNotSupportedException
     * @throws AgentLoadException
     * @throws AgentInitializationException
     */
    // public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
    //     System.out.println(Arrays.toString(args));
    //     VirtualMachine attach = VirtualMachine.attach(args[0]);
    //     attach.loadAgent(args[1], args[2]);
    //     attach.detach();
    // }

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        System.out.println(Arrays.toString(args));
        VirtualMachine attach = VirtualMachine.attach("34152");
        attach.loadAgent("C:\\Users\\Administrator\\Documents\\MyHotDeployment\\HotDeploymentAgent\\target\\agent-1.0-jar-with-dependencies.jar",
                "com.example.hotdeploymentstarter.test.SayService-com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader");
        attach.detach();
    }
}
