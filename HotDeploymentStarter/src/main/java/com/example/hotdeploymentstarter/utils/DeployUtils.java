package com.example.hotdeploymentstarter.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SignUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;
import com.example.hotdeploymentstarter.entity.Constant;
import com.example.hotdeploymentstarter.entity.HotDeployProperties;
import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.xizai.deployment.entity.AgentParams;
import top.xizai.deployment.entity.DeployInfo;
import top.xizai.deployment.enums.DeployType;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
public class DeployUtils {
    private Logger log = LoggerFactory.getLogger(DeployUtils.class);
    private HotDeployProperties hotDeployProperties;

    private ConfigurableApplicationContext ctx;

    public DeployUtils(HotDeployProperties properties) {
        this.hotDeployProperties = properties;
    }

    public DeployUtils(HotDeployProperties properties, ConfigurableApplicationContext ctx) {
        this.hotDeployProperties = properties;
        this.ctx = ctx;
    }

    /**
     * 简单的校验Class字节码是否合法
     *
     * @return
     */
    public boolean verifyClassBytesLegal(String filePath) {
        /**
         * ca fe ba be 00 00 00
         */
        byte[] classStart = {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, 0x00, 0x00};
        byte[] bytes = FileUtil.readBytes(filePath);

        int i;
        for (i = 0; i < bytes.length && i < classStart.length; i++) {
            byte b = bytes[i];
            if (b != classStart[i]) {
                return false;
            }
        }

        if (i != classStart.length) {
            return false;
        }

        return true;
    }

    /**
     * MD5加密
     *
     * @param source
     * @return
     */
    public String md5(String source) {
        return SecureUtil.md5(hotDeployProperties.getSalt() + source + hotDeployProperties.getSalt());
    }

    /**
     * AES加密
     *
     * @param source
     * @return
     */
    public String aseEncode(String source) {
        return SecureUtil.aes(hotDeployProperties.getSalt().getBytes())
                .encryptBase64(source, StandardCharsets.UTF_8);
    }

    /**
     * AES解密
     *
     * @param source
     * @return
     */
    public String aseDecode(String source) {
        return SecureUtil.aes(hotDeployProperties.getSalt().getBytes())
                .decryptStr(source);
    }

    /**
     * 获取本地数据的根目录
     *
     * @return
     */
    public String getRootPath() {
        return hotDeployProperties.getClassFilePath() + File.separatorChar + Constant.ROOT_NAME;
    }

    /**
     * 获取保存HotDeploymentClassSet的本地文件路径
     *
     * @return
     */
    public String getHotDeploymentClassSetFilePath() {
        String selfJsonEncodeFileName = md5(Constant.CLASSES_INFO);
        return getRootPath() + File.separatorChar + selfJsonEncodeFileName;
    }

    /**
     * 获取保存HotDeploymentClassSet校验文件的本地文件路径
     *
     * @return
     */
    public String getHotDeploymentClassSetMD5FilePath() {
        String selfJsonEncodeMD5FileName = md5(Constant.MD5_VERIFICATION);
        return getRootPath() + File.separatorChar + selfJsonEncodeMD5FileName;
    }

    /**
     * 获取热部署文件保存的目录
     *
     * @return
     */
    public String getDeployClassPath() {
        return getRootPath() + File.separatorChar + Constant.CLASSES_DIR;
    }


    public Boolean verifyClassFile(HotDeploymentClass deploymentClass) {
        String filePath = deploymentClass.getClassPath();
        String fileString = FileUtil.readString(filePath, StandardCharsets.UTF_8);
        String hash = md5(fileString);
        if (hash.equals(deploymentClass.getHash())) {
            return true;
        }

        return false;
    }

    /**
     * 获取文件的HASH值
     *
     * @param filePath
     * @return
     */
    public String getFileHash(String filePath) {
        String fileString = FileUtil.readString(filePath, StandardCharsets.UTF_8);
        return md5(fileString);
    }

    /**
     * 1.如果Class在IOC容器中,则将原来的IOC容器对象删除,替换最新的
     * 2.如果Class不是在IOC容器里,
     *
     * @param deploymentClass
     */
    public Boolean hotDeployment(List<HotDeploymentClass> deploymentClass) {
        HotDeploymentClassLoader loader = new HotDeploymentClassLoader();
        List<Class> leastClazzList = new ArrayList<>();
        Map<String, HotDeploymentClass> hotDeploymentClassMap = new HashMap<>();

        for (HotDeploymentClass clazzInfo : deploymentClass) {
            try {
                Class<?> leastClazz = Class.forName(clazzInfo.getFullPackageName(), true, loader);
                leastClazzList.add(leastClazz);
                hotDeploymentClassMap.put(clazzInfo.getFullPackageName(), clazzInfo);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }


        // 热部署
        for (Class clazz : leastClazzList) {
            try {
                HotDeploymentClass deploymentInfo = hotDeploymentClassMap.get(clazz.getName());
                /**
                 * 用ASM改变class文件字节码来实现热部署
                 */
                registerObject(clazz, deploymentInfo);

                /**
                 * IOC类仅需将我们最新的class对象放入IOC容器即可
                 */
                registerIOCIfNecessary(clazz);
                registerControllerIfNecessary(clazz);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }


        return true;
    }

    /**
     * 通过ASM技术将所有字节码中拥有本来的new关键字全部通过反射实例化最新的对象
     *
     * @param clazz
     * @param deploymentClassInfo
     */
    private boolean registerObject(Class clazz, HotDeploymentClass deploymentClassInfo) {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setDeployType(DeployType.REPLACE_CLASS);
        deployInfo.setVersion(System.currentTimeMillis());
        deployInfo.setHashCode(deploymentClassInfo.getHash());
        deployInfo.setClassFullName(deploymentClassInfo.getFullPackageName());

        AgentParams params = new AgentParams();
        params.setClassLoaderFullName(HotDeploymentClassLoader.class.getName());
        params.setClassLoaderPath(this.getDeployClassPath());
        params.setDeployments(Arrays.asList(deployInfo));

        Map deployInfoMap = JSON.parseObject(JSON.toJSONString(params), Map.class);
        String sign = SignUtil.signParamsMd5(deployInfoMap);

        params.setSign(sign);

        String url = "127.0.0.1:" + hotDeployProperties.getAgentPort();

        String ret = HttpUtil.post(url, JSON.toJSONString(params));
        log.info("remote agent response info:{}", ret);

        return true;
    }

    /**
     * 转换成峰驼模式
     *
     * @return
     */
    private String convert2camelStyle(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }

        String camelCase = StrUtil.toCamelCase(name, '_');
        String c = camelCase.charAt(0) + "";
        return camelCase.replaceFirst(c, c.toLowerCase());
    }

    public static String convertPackageName2Path(String packageName) {
        return packageName.replace('.', File.separatorChar);
    }

    /**
     * 如果是Controller,则将其注册到URL映射中
     *
     * @param clazz
     * @return
     */
    private boolean registerControllerIfNecessary(Class clazz) {
        String clazzName = convert2camelStyle(clazz.getSimpleName());
        if (!ObjectUtils.isEmpty(clazz.getAnnotation(RestController.class))
                || !ObjectUtils.isEmpty(clazz.getAnnotation(Controller.class))) {
            try {
                if (!ctx.containsBean(clazzName)) {
                    ctx.getBeanFactory().createBean(clazz);
                }

                doUnregisterBean(clazzName);
                return doRegisterBean(clazzName);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

        return true;
    }

    private boolean doRegisterBean(String clazzName) {
        try {
            final RequestMappingHandlerMapping requestMappingHandlerMapping =
                    ctx.getBean(RequestMappingHandlerMapping.class);
            if (requestMappingHandlerMapping != null) {
                String handler = clazzName;
                Object controller = ctx.getBean(handler);
                if (controller == null) {
                    return false;
                }
                //注册Controller
                Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().
                        getDeclaredMethod("detectHandlerMethods", Object.class);
                //将private改为可使用
                method.setAccessible(true);
                method.invoke(requestMappingHandlerMapping, handler);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean doUnregisterBean(String clazzName) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                ctx.getBean("requestMappingHandlerMapping");
        if (requestMappingHandlerMapping != null) {
            String handler = clazzName;
            Object controller = ctx.getBean(handler);
            if (controller == null) {
                return false;
            }
            final Class<?> targetClass = controller.getClass();
            ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    try {
                        Method createMappingMethod = RequestMappingHandlerMapping.class.
                                getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                        createMappingMethod.setAccessible(true);
                        RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                                createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                        if (requestMappingInfo != null) {
                            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }

        return true;
    }

    /**
     * 如果Class上面有注入的注解,则将其注入到IOC容器中
     *
     * @param clazz
     * @return
     */
    public boolean registerIOCIfNecessary(Class clazz) {
        if (!ObjectUtils.isEmpty(clazz.getAnnotation(Component.class))
                || !ObjectUtils.isEmpty(clazz.getAnnotation(Service.class))
                || !ObjectUtils.isEmpty(clazz.getAnnotation(Controller.class))
                || !ObjectUtils.isEmpty(clazz.getAnnotation(RestController.class))
                || !ObjectUtils.isEmpty(clazz.getAnnotation(Configuration.class))) {

            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ctx.getBeanFactory();
            String objName = convert2camelStyle(clazz.getSimpleName());

            boolean isContain = beanFactory.containsBeanDefinition(objName);
            if (isContain) {
                beanFactory.removeBeanDefinition(objName);
            }

            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            AbstractBeanDefinition rawBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

            // 默认单例模式
            Annotation scope = clazz.getAnnotation(Scope.class);
            if (scope != null) {
                rawBeanDefinition.setScope(scope.toString());
            } else {
                rawBeanDefinition.setScope("singleton");
            }

            beanFactory.registerBeanDefinition(objName, rawBeanDefinition);
        }
        return false;
    }


    public static void executeJar(String jarFilePath, List args) {
        BufferedReader error;
        BufferedReader op;
        int exitVal;

        final List actualArgs = new ArrayList();

        actualArgs.add(0, "java");

        actualArgs.add(1, "-jar");

        actualArgs.add(2, jarFilePath);

        actualArgs.addAll(args);

        try {

            final Runtime re = Runtime.getRuntime();


            final Process command = re.exec((String[]) actualArgs.toArray(new String[0]));

            error = new BufferedReader(new InputStreamReader(command.getErrorStream()));

            op = new BufferedReader(new InputStreamReader(command.getInputStream()));


            command.waitFor();

            exitVal = command.exitValue();

            if (exitVal != 0) {

                throw new IOException("Failed to execure jar, " + getExecutionLog(error, op));

            }

        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static String getExecutionLog(BufferedReader error, BufferedReader op) {

        String errorResult = "";

        String line;

        try {

            while ((line = error.readLine()) != null) {

                errorResult = errorResult + "\n" + line;

            }

        } catch (final IOException e) {

        }

        String output = "";

        try {

            while ((line = op.readLine()) != null) {

                output = output + "\n" + line;

            }

        } catch (final IOException e) {

        }

        try {

            error.close();

            op.close();

        } catch (final IOException e) {

        }

        return errorResult;
    }
}
