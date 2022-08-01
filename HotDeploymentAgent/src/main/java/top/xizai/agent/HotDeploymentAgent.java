package top.xizai.agent;

import cn.hutool.log.level.Level;
import com.sun.net.httpserver.HttpServer;
import top.xizai.agent.asm.HotDeploymentAsmUtil;
import top.xizai.agent.asm.cache.GlobalProxyCache;
import top.xizai.agent.handler.ReceiveOperateHandler;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE: 热部署代理
 **/
public class HotDeploymentAgent {
    private static Logger log = Logger.getLogger(HotDeploymentAgent.class.getSimpleName());

    public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException, ClassNotFoundException {
        log.info("Remote deployment agent initializing...");
        //默认 [PORT-Secret] 格式
        String[] args = agentArgs.split("-");
        Integer port = Integer.valueOf(args[0]);
        String secret = args[1];

        // 开启远程调度线程,接收HotDeploymentStart的远程调用

        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.start();
            httpServer.createContext("/", new ReceiveOperateHandler(inst, secret));

            log.info("Remote deployment agent initialized.");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // inst.addTransformer(new HotDeploymentClassFileTransformer(args[0], args[1]), true);
        // //触发transform执行
        // inst.retransformClasses(Class.forName(args[0]));
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
                if (GlobalProxyCache.deploymentByteMap.containsKey(className)){
                    return (byte[]) GlobalProxyCache.deploymentByteMap.get(className);
                }

                byte[] bytes = HotDeploymentAsmUtil.changeMethodByClassBufferMethodVal(classfileBuffer, className);


                if (bytes != null) {
                    /**
                     * 缓存原始的字节码
                     * 将原始对象压入栈中,方便后期回溯原始对象
                     */
                    Stack<Object> byteStack = GlobalProxyCache.originByteMap.get(clsName);
                    if (bytes == null) {
                        byteStack = new Stack<>();
                    }
                    byteStack.push(classfileBuffer);

                    /**
                     * 缓存编辑后的字节码
                     */
                    GlobalProxyCache.deploymentByteMap.put(className, bytes);
                }

                return bytes;
            }
            return new byte[0];
        }
    }
}
