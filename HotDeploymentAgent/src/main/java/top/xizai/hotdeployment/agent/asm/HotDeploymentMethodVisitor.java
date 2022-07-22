package top.xizai.hotdeployment.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class HotDeploymentMethodVisitor extends MethodVisitor {

    private String className;
    private String classloaderName;

    public HotDeploymentMethodVisitor(MethodVisitor mv, String className, String classloaderName) {
        super(Opcodes.ASM5, mv);

        this.className = className;
        this.classloaderName = classloaderName.replace(".", "/");
    }


    // // TODO 这段代码有误
    // @Override
    // public void visitTypeInsn(int opcode, String type) {
    //     System.out.println("visitTypeInsn:" + opcode + " = " + type);
    //     if (opcode == Opcodes.NEW &&
    //             type.equals(className.replace(".", "/"))) {
    //
    //         System.out.println("start INVOKE.... " + className);
    //         //
    //         // mv.visitMethodInsn(Opcodes.INVOKESTATIC, classloaderName,
    //         //         "getInstance", "()L" + classloaderName + ";");
    //         // mv.visitLdcInsn(type.replace("/", "."));
    //         // mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classloaderName,
    //         //         "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    //         // mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
    //         //         "newInstance", "()Ljava/lang/Object;");
    //         // mv.visitTypeInsn(Opcodes.CHECKCAST, className);
    //         mv.visitCode();
    //         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    //         mv.visitLdcInsn("执行方法后。。。");
    //         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    //
    //         mv.visitEnd();
    //     }
    //
    //     super.visitTypeInsn(opcode, type);
    // }

    //
    // @Override
    // public void visitCode() {
    //     mv.visitCode();
    //     mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    //     //打印内容
    //     mv.visitLdcInsn("前置处理代码");
    //     mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    // }
    //
    // @Override
    // public void visitMaxs(int maxStack, int maxLocals) {
    //     super.visitMaxs(maxStack + 4, maxLocals);
    // }

    @Override
    public void visitCode() {
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        //打印内容
        mv.visitLdcInsn("打印内容。。。。。。。。。。。。。");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
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