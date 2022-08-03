package top.xizai.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import top.xizai.agent.asm.cache.GlobalProxyCache;
import top.xizai.agent.asm.classloader.TempClassLoader;
import top.xizai.deployment.entity.DeployInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * @author: WSC
 * @DATE: 2022/7/31
 * @DESCRIBE: 热部署加载代理
 **/
public class HotDeploymentProxy extends ClassVisitor {
    /**
     * 热部署类的全限定名
     */
    private final String deployClassFullName;
    /**
     * 热部署的字节码对象
     */
    private Class clazz;
    /**
     * 热部署信息
     */
    private DeployInfo deployInfo;

    /**
     * @param deployInfo 热部署信息
     */
    public HotDeploymentProxy(ClassVisitor cv, DeployInfo deployInfo) {
        super(ASM8, cv);
        this.deployClassFullName = deployInfo.getClassFullName();
        List<String> specifyMethods = deployInfo.getSpecifyMethods();
        List<String> ignoreMethods = deployInfo.getIgnoreMethods();

        try {
            TempClassLoader classLoader = new TempClassLoader(GlobalProxyCache.classLoaderPath);
            clazz = classLoader.findClass(deployClassFullName);

            GlobalProxyCache.deployClassCache.put(deployClassFullName, clazz);

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                /**
                 * 指定方法更新和忽略方法更新
                 */
                if (specifyMethods != null && specifyMethods.contains(methodName)
                        || ignoreMethods != null && !ignoreMethods.contains(methodName)) {
                    String methodParams = Arrays.stream(method.getParameterTypes())
                            .map(m -> m.getName())
                            .collect(Collectors.joining("$"));
                    StringBuilder sb = new StringBuilder();
                    sb.append(deployClassFullName)
                            .append("$")
                            .append(methodName)
                            .append("$")
                            .append(methodParams);

                    method.setAccessible(true);
                    GlobalProxyCache.methodsCache.put(sb.toString(), method);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        try {
            GlobalProxyCache.instanceCache.put(deployClassFullName, clazz.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
                if (GlobalProxyCache.methodsCache.containsKey(methodKey)) {
                    mv = new HotDeploymentMethodProxy(api, mv, name, descriptor);
                }
            }
        }
        return mv;
    }


    public class HotDeploymentMethodProxy extends MethodVisitor {
        private String methodName;

        public HotDeploymentMethodProxy(int api, MethodVisitor methodVisitor, String methodName, String descriptor) {
            super(api, methodVisitor);
            this.methodName = methodName;
        }

        @Override
        public void visitCode() {
            String classLoaderClassPath = GlobalProxyCache.classLoaderFullName.replace('.', '/');

            /**
             * 获取类加载器的实例对象
             */
            mv.visitMethodInsn(INVOKESTATIC, classLoaderClassPath, "getInstance", "()L" + classLoaderClassPath + ";", false);
            /**
             * 根据类加载器,获取目标对象
             */
            mv.visitLdcInsn(deployClassFullName);
            mv.visitMethodInsn(INVOKEVIRTUAL, classLoaderClassPath, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);

            mv.visitVarInsn(ASTORE, 1);
            /**
             * 实例化目标对象
             */
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
            mv.visitVarInsn(ASTORE, 2);
            /**
             * 获取目标执行的方法
             */
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(methodName);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitVarInsn(ASTORE, 3);
            /**
             * 通过反射调用目标方法
             */
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 4);
            /**
             * 直接返回,不调用原Class的方法
             */
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();

            super.visitCode();
        }
    }

    public String getMethodKey(String methodName, String descriptor) {
        Type[] types = Type.getArgumentTypes(descriptor);
        String params = Arrays.stream(types)
                .map(type -> type.getClassName())
                .collect(Collectors.joining("$"));
        return deployClassFullName + "$" + methodName + "$" + params;
    }
}
