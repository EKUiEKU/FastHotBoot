package top.xizai.agent.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import top.xizai.deployment.entity.DeployInfo;

/**
 * @author: WSC
 * @DATE: 2022/7/30
 * @DESCRIBE:
 **/
public class HotDeploymentAsmUtil {
    public static byte[] changeMethodByClassBufferMethodVal(byte[] classfileBuffer, DeployInfo deployInfo) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        // ClassVisitor cv = new HotDeploymentMethodClassVisitor(classWriter, className, classLodeName);
        ClassVisitor cv = new HotDeploymentProxy(classWriter, deployInfo);
        classReader.accept(cv, 0);

        byte[] bytes = classWriter.toByteArray();

        return bytes;
    }
}
