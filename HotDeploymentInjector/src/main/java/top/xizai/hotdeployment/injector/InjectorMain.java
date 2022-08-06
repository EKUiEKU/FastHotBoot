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
    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        String port = args[0];
        String path = args[1];
        String options = args[2];
        VirtualMachine attach = VirtualMachine.attach(port);
        attach.loadAgent(path, options);
        attach.detach();
    }
}
