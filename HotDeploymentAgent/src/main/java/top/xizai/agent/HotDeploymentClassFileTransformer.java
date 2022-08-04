package top.xizai.agent;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import top.xizai.agent.asm.HotDeploymentAsmUtil;
import top.xizai.agent.asm.cache.GlobalProxyCache;
import top.xizai.deployment.entity.ByteObject;
import top.xizai.deployment.entity.DeployInfo;
import top.xizai.deployment.enums.DeployType;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE:
 **/
public class HotDeploymentClassFileTransformer implements ClassFileTransformer {

    private static Logger log = Logger.getLogger(HotDeploymentClassFileTransformer.class.getSimpleName());
    private DeployInfo deployInfo;

    public HotDeploymentClassFileTransformer(DeployInfo deployInfo) {
        this.deployInfo = deployInfo;
    }

    @Override
    public byte[] transform(ClassLoader loader, String clsName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        DeployType deployType = deployInfo.getDeployType();
        String className = deployInfo.getClassFullName();
        Long version = deployInfo.getVersion();

        //不处理目标类即可
        if (clsName.matches(".*" + className)) {
            if (deployType.equals(DeployType.REPLACE_CLASS) || deployType.equals(DeployType.REPLACE_METHOD)) {
                /**
                 * 判断是否有缓存
                 */
                if (GlobalProxyCache.deploymentByteMap.containsKey(className)) {
                    ByteObject byteObject = GlobalProxyCache.deploymentByteMap.get(className);
                    if (byteObject.getVersion() == deployInfo.getVersion()) {
                        return byteObject.getBytes();
                    }
                }

                byte[] bytes = null;

                try {
                    bytes = HotDeploymentAsmUtil.changeMethodByClassBufferMethodVal(classfileBuffer, deployInfo);
                } catch (Throwable e) {
                    e.printStackTrace();
                }


                if (bytes != null) {
                    /**
                     * 缓存原始的字节码
                     * 将原始对象压入栈中,方便后期回溯原始对象
                     */
                    Stack<ByteObject> originByteObjectStack = GlobalProxyCache.originByteMap.get(clsName);
                    if (originByteObjectStack == null) {
                        originByteObjectStack = new Stack<>();
                        GlobalProxyCache.originByteMap.put(className, originByteObjectStack);
                    }
                    originByteObjectStack.push(new ByteObject(classfileBuffer, version));

                    /**
                     * 缓存编辑后的字节码
                     */
                    GlobalProxyCache.deploymentByteMap.put(className, new ByteObject(bytes, version));

                    log.log(Level.INFO, "class {0} with version {1} is be deployed!", new Object[]{className, version});
                }

                return bytes;

            } else if (deployType.equals(DeployType.ROLLBACK)) {
                Stack<ByteObject> originByteObjectStack = GlobalProxyCache.originByteMap.get(className);
                // 判断栈中是否有对应的版本号

                Boolean hasFound = false;
                if (originByteObjectStack != null && originByteObjectStack.size() != 0) {
                    for (int i = originByteObjectStack.size() - 1; i >= 0; i--) {
                        ByteObject byteObject = originByteObjectStack.get(i);
                        if (byteObject.getVersion().equals(version)) {
                            hasFound = true;
                            break;
                        }
                    }
                }

                if (!hasFound) {
                    log.log(Level.WARNING, "class {0} with version {1} is not found in memory!", new Object[]{className, version});
                    return new byte[0];
                }

                ByteObject originByteObject = null;
                byte[] recoverBytes = null;
                while ((originByteObject = originByteObjectStack.pop()) != null) {
                    if (originByteObject.getVersion().equals(version)) {
                        recoverBytes = originByteObject.getBytes();
                        break;
                    }
                }

                log.log(Level.INFO, "roll back to class {0} with version {1}!", new Object[]{className, version});
                return recoverBytes;
            }
        }


        return new byte[0];
    }
}