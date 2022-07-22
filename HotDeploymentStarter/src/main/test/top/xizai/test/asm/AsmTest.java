package top.xizai.test.asm;

import cn.hutool.core.io.FileUtil;
import com.example.hotdeploymentstarter.asm.HotDeployMethodVisitor;
import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;
import org.objectweb.asm.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: WSC
 * @DATE: 2022/7/18
 * @DESCRIBE:
 **/
public class AsmTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String className = String.class.getName();

        HotDeploymentClassLoader loader = new HotDeploymentClassLoader("C:\\DevEnv");
        Class<?> clazz = Class.forName("top.xizai.test.asm.SimpleService", true, loader);
        Object newInstance = clazz.newInstance();
        Method say = clazz.getMethod("say");
        say.invoke(newInstance);

        ClassReader cr = new ClassReader("top.xizai.test.asm.SimpleService");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new LogMethodClassVisitor(cw, className.replace(".", "/"));
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
    }

    public static class LogMethodClassVisitor extends ClassVisitor {
        private String className;

        public LogMethodClassVisitor(ClassVisitor cv, String pClassName) {
            super(Opcodes.ASM5, cv);
            className = pClassName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(Opcodes.ASM5, name, desc, signature,
                    exceptions);

            System.out.println(name + " == " + className);

            return new PrintMessageMethodVisitor(mv, name, className);
        }
    }


    public static class PrintMessageMethodVisitor extends MethodVisitor {
        private String className;
        private String methodName;

        private String CLASSLOAD_TYPE;

        public PrintMessageMethodVisitor(MethodVisitor methodVisitor, String methodName, String className) {
            super(Opcodes.ASM9, methodVisitor);
            this.methodName = methodName;
            this.className = className;
            CLASSLOAD_TYPE = HotDeploymentClassLoader.class.getClassLoader().getName();
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            System.out.println("visitTypeInsn:" + opcode + " = " + type);
            if (opcode == Opcodes.NEW &&
                    type.equals(className)) {

                System.out.println("现在执行到NEW " + className);

                mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASSLOAD_TYPE,
                        "getInstance", "()L" + CLASSLOAD_TYPE + ";");
                mv.visitLdcInsn(type.replace("/", "."));
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLASSLOAD_TYPE,
                        "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                        "newInstance", "()Ljava/lang/Object;");
                // mv.visitTypeInsn(Opcodes.CHECKCAST, compileType);
            } else {
                mv.visitTypeInsn(opcode, type);
            }

            super.visitTypeInsn(opcode, type);
        }
    }
}
