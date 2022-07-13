package deployment;

import deployment.classloader.HotDeploymentClassLoader;
import deployment.impl.SampleServiceImpl;
import top.xizai.deployment.interfae.IService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * @author: WSC
 * @DATE: 2022/7/11
 * @DESCRIBE:
 **/
public class DeploymentApplication {

    private static String path = "C:\\Users\\Administrator\\Documents\\MyHotDeployment\\Sample\\target\\classes";
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        IService localService = new SampleServiceImpl();

        while (!reader.ready()) {
            String line = reader.readLine();

            if ("read".equalsIgnoreCase(line.trim())) {
                System.out.println(localService.hello());
            }

            if ("deploy".equalsIgnoreCase(line.trim())) {
                HotDeploymentClassLoader loader = new HotDeploymentClassLoader(path);
                Class<?> deploymentClass = Class.forName("top.xizai.deployment.impl.SampleServiceImpl", false, loader);
                localService = (IService)deploymentClass.newInstance();
                System.out.println(localService.hello());
            }
        }
    }
}
