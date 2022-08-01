package top.xizai.agent;

import top.xizai.agent.asm.HotDeploymentAsmUtil;
import top.xizai.agent.asm.cache.GlobalProxyCache;
import top.xizai.deployment.entity.DeployInfo;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Stack;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE:
 **/
public class HotDeploymentClassFileTransformer implements ClassFileTransformer {
    private DeployInfo deployInfo;

    private String className;

    public HotDeploymentClassFileTransformer(DeployInfo deployInfo) {
        this.deployInfo = deployInfo;
        this.className = deployInfo.getClassFullName();
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