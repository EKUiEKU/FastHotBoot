package top.xizai.agent;

import top.xizai.agent.asm.HotDeploymentAsmUtil;
import top.xizai.agent.asm.cache.GlobalProxyCache;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Random;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE: 热部署代理
 **/
public class HotDeploymentAgent {

    public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException, ClassNotFoundException {
        System.out.println("method agentmain invoked");
        //默认 [className-methodName-printContent] 格式
        String[] args = agentArgs.split("-");
        inst.addTransformer(new HotDeploymentClassFileTransformer(args[0], args[1]), true);
        //触发transform执行
        inst.retransformClasses(Class.forName(args[0]));
    }

    static class HotDeploymentClassFileTransformer implements ClassFileTransformer {
        private String className;
        private String classLodeName;

        public HotDeploymentClassFileTransformer(String className, String classLodeName) {
            this.className = className;
            this.classLodeName = classLodeName;
        }

        @Override
        public byte[] transform(ClassLoader loader, String clsName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

            //不处理目标类即可
            if (clsName.matches(".*" + className)) {
                /**
                 * 判断是否有缓存
                 */
                // if (GlobalProxyCache.deploymentByteMap.containsKey(className)){
                //     return (byte[]) GlobalProxyCache.deploymentByteMap.get(className);
                // }

                // try {
                //     Files.write(Path.of("C:\\DevEnv\\" + randName + "origin.class"), classfileBuffer);
                // } catch (IOException e) {
                //     throw new RuntimeException(e);
                // }

                byte[] bytes = HotDeploymentAsmUtil.changeMethodByClassBufferMethodVal(classfileBuffer, className);


                if (bytes != null) {
                    GlobalProxyCache.deploymentByteMap.put(className, bytes);
                }

                return bytes;
            }
            return new byte[0];
        }
    }
}
