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

            log.info("Remote deployment agent is listen port on " + port + ".");
            log.info("Remote deployment agent initialized.");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
