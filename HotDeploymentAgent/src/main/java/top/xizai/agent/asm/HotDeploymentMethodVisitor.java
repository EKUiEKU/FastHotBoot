package top.xizai.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class HotDeploymentMethodVisitor extends MethodVisitor {

    private String className;
    private String classloaderName;

    private String printContent;

    public HotDeploymentMethodVisitor(MethodVisitor mv, String className, String classloaderName) {
        super(ASM8, mv);

        // this.className = className;
        // this.classloaderName = classloaderName.replace(".", "/");

        this.className = "com.example.hotdeploymentstarter.test.SayService";
        this.classloaderName = "com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader";
        this.classloaderName = classloaderName.replace(".", "/");

        this.printContent = "打印。。。。";
    }


    //此方法在目标方法调用之前调用，所以前置操作可以在这处理
    @Override
    public void visitCode() {
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        //打印内容
        mv.visitLdcInsn(printContent);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }


    @Override
    public void visitTypeInsn(int opcode, String type) {
        System.out.println("visitTypeInsn:" + opcode + " = " + type);
        if (opcode == Opcodes.NEW &&
                type.equals(className.replace(".", "/"))) {

            System.out.println("start INVOKE.... " + className);
            //
            // mv.visitMethodInsn(Opcodes.INVOKESTATIC, classloaderName,
            //         "getInstance", "()L" + classloaderName + ";");
            // mv.visitLdcInsn(type.replace("/", "."));
            // mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classloaderName,
            //         "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
            // mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
            //         "newInstance", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKESTATIC, "com/example/hotdeploymentstarter/classloader/HotDeploymentClassLoader", "getInstance", "()Lcom/example/hotdeploymentstarter/classloader/HotDeploymentClassLoader;", false);
            mv.visitLdcInsn("com.example.hotdeploymentstarter.test.SayService");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/example/hotdeploymentstarter/classloader/HotDeploymentClassLoader", "findClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("say");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }

        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitInsn(int opcode) {
        //此方法可以获取方法中每一条指令的操作类型，被访问多次
        //如应在方法结尾处添加新指令，则应判断：

        if (opcode == Opcodes.IRETURN) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("执行方法后。。。");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }
}