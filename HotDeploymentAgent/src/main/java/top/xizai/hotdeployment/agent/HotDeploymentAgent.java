package top.xizai.hotdeployment.agent;

import cn.hutool.core.io.FileUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import top.xizai.hotdeployment.agent.asm.HotDeploymentMethodClassVisitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
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
        inst.retransformClasses(Class.forName("top.xizai.test.asm.HotAsmDeploymentTest"));
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

            System.out.println("className is " + clsName);
            //不处理目标类即可
            if (clsName.trim().contains("HotAsmDeploymentTest")) {
                System.out.println("werite " +"C:\\DevEnv\\" + className + ".class");

                ClassReader classReader = new ClassReader(classfileBuffer);
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new HotDeploymentMethodClassVisitor(classWriter, className, classLodeName);
                classReader.accept(cv, 0);

                FileUtil.writeBytes(classWriter.toByteArray(), "C:\\DevEnv\\" + new Random().nextInt() + ".class");
                return classWriter.toByteArray();
            }
            return new byte[0];
        }
    }
}
