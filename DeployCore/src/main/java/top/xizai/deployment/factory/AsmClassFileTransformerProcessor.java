package top.xizai.deployment.factory;

import org.objectweb.asm.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE:
 **/
public class AsmClassFileTransformerProcessor {
    private Instrumentation inst;
    private AsmCacheableDeployContext ctx;

    private DeployDefinition definition;

    public AsmClassFileTransformerProcessor(String fullClazzName, AsmCacheableDeployContext ctx) {
        this.ctx = ctx;
        definition = ctx.getDeployDefinition(fullClazzName);
    }

    private class DeployClassProxy extends ClassVisitor {
        public DeployClassProxy(ClassVisitor cv) {
            super(ASM8, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (mv != null && !"<init>".equals(name) && !"<clinit>".equals(name)) {
                boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
                boolean isNativeMethod = (access & ACC_NATIVE) != 0;
                if (!isAbstractMethod && !isNativeMethod) {
                    /**
                     * 判断最新的Class对象是否有包含调用函数
                     */
                    String methodKey = getMethodKey(name, descriptor);
                    Method method = ctx.getDeployMethod(methodKey);
                    if (method != null) {
                        for (DeployEventListener listener : ctx.getDeployEventListenerList()) {
                            listener.onDeployMethod(definition, method);
                        }
                        try {
                            mv = new DeployMethodProxy(api, mv, name, descriptor);
                        }catch (Exception e) {
                            for (DeployEventListener listener : ctx.getDeployEventListenerList()) {
                                listener.onDeployMethodException(definition, method, e);
                            }
                            e.printStackTrace();
                        }
                    }
                }
            }
            return mv;
        }
    }


    private class DeployMethodProxy extends MethodVisitor {
        private String methodName;
        private String methodDescriptor;

        public DeployMethodProxy(int api, MethodVisitor methodVisitor, String methodName, String descriptor) {
            super(api, methodVisitor);
            this.methodName = methodName;
            this.methodDescriptor = descriptor;
        }

        @Override
        public void visitCode() {
            String classLoaderFullName = DeployClassLoader.class.getName();
            String classLoaderClassPath = classLoaderFullName.replace('.', '/');
            String returnClassName = methodDescriptor.split("\\)L")[1].replace(";", "");
            /**
             * 获取类加载器的实例对象
             */
            mv.visitMethodInsn(INVOKESTATIC, classLoaderClassPath, "getInstance", "()L" + classLoaderClassPath + ";", false);
            /**
             * 根据类加载器,获取目标对象
             */
            mv.visitLdcInsn(definition.getFullPackageName());
            mv.visitMethodInsn(INVOKEVIRTUAL, classLoaderClassPath, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);

            // 复制一份 实例化和根据方法名获取方法实例都要用到
            mv.visitInsn(DUP);

            /**
             * 实例化目标对象
             */
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
            // 将实例存到成员变量中 invoke的时候使用
            mv.visitVarInsn(ASTORE, 1);

            /**
             * 获取目标执行的方法 入参是方法名和空数组
             */
            mv.visitLdcInsn(methodName);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            /**
             * 通过反射调用目标方法 入参的实例对象和空数组
             */
            // 将成员变量的ins方到栈顶
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, returnClassName);
            /**
             * 直接返回,不调用原Class的方法
             */
            mv.visitInsn(ARETURN);
        }
    }

    public String getMethodKey(String methodName, String descriptor) {
        Type[] types = Type.getArgumentTypes(descriptor);
        String params = Arrays.stream(types)
                .map(type -> type.getClassName())
                .collect(Collectors.joining("$"));
        return definition.getFullPackageName() + "$" + methodName + "$" + params;
    }

    public byte[] changeMethodByClassBufferMethodVal(byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        DeployClassProxy proxy = new DeployClassProxy(classWriter);

        classReader.accept(proxy, 0);

        byte[] bytes = classWriter.toByteArray();

        return bytes;
    }
}
