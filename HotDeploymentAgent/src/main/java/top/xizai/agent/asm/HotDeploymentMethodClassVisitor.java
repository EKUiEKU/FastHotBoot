package top.xizai.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class HotDeploymentMethodClassVisitor extends ClassVisitor {
    /**
     * 要热部署的类名称
     */
    private String className;
    /**
     * 类加载器的名称
     */
    private String classloaderName;

    public HotDeploymentMethodClassVisitor(ClassVisitor cv, String className, String classloaderName) {
        super(Opcodes.ASM8, cv);

        this.className = className;
        this.classloaderName = classloaderName;
    }

    private String defineInterface = "com/example/hotdeploymentstarter/test/ISayService";

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, new String[]{defineInterface});
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        System.out.println("访问方法:" + name);

        if (name.equals("say")) {
            return new HotDeploymentMethodVisitor(mv, className, classloaderName);
        }else {
            return mv;
        }
    }
}
