package top.xizai.deployment.factory;

import top.xizai.deployment.entity.ByteObject;
import top.xizai.deployment.entity.DeployInfo;
import top.xizai.deployment.enums.DeployType;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE: ASM编码的可缓存的热部署上下文
 **/
public class AsmCacheableDeployContext extends CacheableDeployContext implements ClassFileTransformer {
    private static Logger log = Logger.getLogger(AsmCacheableDeployContext.class.getSimpleName());

    /**
     * 缓存编辑后的字节码
     */
    public static Map<String, ByteObject> deploymentByteMap = new ConcurrentHashMap<>(16);
    /**
     * 缓存原始的字节码
     */
    public static Map<String, Stack<ByteObject>> originByteMap = new ConcurrentHashMap<>(16);

    /**
     * 监听事件
     */
    private List<DeployEventListener> deployEventListenerList = new ArrayList<>();

    private Instrumentation instrumentation;

    /**
     * 类加载器加载部署类的地址
     */
    private String classFileLoaderPath;


    public AsmCacheableDeployContext(Instrumentation instrumentation, String classFileLoaderPath) {
        this.instrumentation = instrumentation;
        this.classFileLoaderPath = classFileLoaderPath;

        DeployClassLoader.resignDeployClassPath(classFileLoaderPath);

        this.instrumentation.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String clsName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (clsName == null || classfileBuffer == null) {
            return new byte[0];
        }

        try {
            DeployDefinition definition = getDeployDefinition(clsName.replace("/", "."));

            if (definition == null) {
                return new byte[0];
            }

            DeployType deployType = definition.getDeployType();
            String className = definition.getFullPackageName();
            Long version = definition.getVersion();

            //不处理目标类即可
            if (clsName.matches(".*" + className)) {
                for (DeployEventListener listener : deployEventListenerList) {
                    listener.onStartDeploy(definition);
                }

                if (deployType.equals(DeployType.REPLACE_CLASS) || deployType.equals(DeployType.REPLACE_METHOD)) {
                    /**
                     * 判断是否有缓存
                     */
                    if (deploymentByteMap.containsKey(className)) {
                        ByteObject byteObject = deploymentByteMap.get(className);
                        if (Objects.equals(byteObject.getVersion(), definition.getVersion())) {
                            return byteObject.getBytes();
                        }
                    }

                    byte[] bytes = null;

                    try {
                        AsmClassFileTransformerAdapter adapter = new AsmClassFileTransformerAdapter(className, this);
                        bytes = adapter.changeMethodByClassBufferMethodVal(classfileBuffer);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }


                    if (bytes != null) {
                        /**
                         * 缓存原始的字节码
                         * 将原始对象压入栈中,方便后期回溯原始对象
                         */
                        Stack<ByteObject> originByteObjectStack = originByteMap.get(clsName);
                        if (originByteObjectStack == null) {
                            originByteObjectStack = new Stack<>();
                            originByteMap.put(className, originByteObjectStack);
                        }
                        originByteObjectStack.push(new ByteObject(classfileBuffer, version));

                        /**
                         * 缓存编辑后的字节码
                         */
                        deploymentByteMap.put(className, new ByteObject(bytes, version));

                        log.log(Level.INFO, "class {0} with version {1} is be deployed!", new Object[]{className, version});

                        for (DeployEventListener listener : deployEventListenerList) {
                            listener.onFinishDeploy(definition);
                        }
                    }

                    return bytes;

                } else if (deployType.equals(DeployType.ROLLBACK)) {
                    Stack<ByteObject> originByteObjectStack = originByteMap.get(className);
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
                    for (DeployEventListener listener : deployEventListenerList) {
                        listener.onFinishDeploy(definition);
                    }

                    return recoverBytes;
                }
            }
        }catch (Throwable e) {
            e.printStackTrace();
        }


        return new byte[0];
    }


    @Override
    public void flushCacheInDisk() {

    }

    /**
     * 添加监听事件
     *
     * @param listener
     */
    public void addDeployEventListener(DeployEventListener listener) {
        this.deployEventListenerList.add(listener);
    }

    public List<DeployEventListener> getDeployEventListenerList() {
        return deployEventListenerList;
    }

    /**
     * 开始热部署类
     *
     * @param definition
     */
    public void deploy(DeployDefinition definition) {
        try {
            Class<?> clazz = Class.forName(definition.getFullPackageName());
            this.instrumentation.retransformClasses(clazz);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSalt() {
        return null;
    }

    @Override
    public boolean doValidate(String username, String passwd, Object... args) {
        return false;
    }

    @Override
    public boolean validateMessage(Object body, Object... args) {
        return false;
    }

    @Override
    public boolean validateClassFile(File file, Object... args) {
        return false;
    }
}
