package com.example.hotdeploymentstarter.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.hotdeploymentstarter.classloader.HotDeploymentClassLoader;
import com.example.hotdeploymentstarter.entity.Constant;
import com.example.hotdeploymentstarter.entity.HotDeployProperties;
import com.example.hotdeploymentstarter.entity.HotDeploymentClass;
import com.example.hotdeploymentstarter.handler.ReceiveClassHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ctx.getBeanFactory();

        HotDeploymentClassLoader loader = new HotDeploymentClassLoader(getDeployClassPath());
        List<Class> leastClazzList = new ArrayList<>();

        for (HotDeploymentClass clazzInfo : deploymentClass) {
            try {
                Class<?> leastClazz = Class.forName(clazzInfo.getFullPackageName(), false, loader);
                leastClazzList.add(leastClazz);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }


        // 热部署
        for (Class clazz : leastClazzList) {
            try {
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
                }else {
                    rawBeanDefinition.setScope("singleton");
                }


                beanFactory.registerBeanDefinition(objName, rawBeanDefinition);

                registerBeanIfNecessary(clazz);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }


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
     * @param clazz
     * @return
     */
    private boolean registerBeanIfNecessary(Class clazz) {
        String clazzName = convert2camelStyle(clazz.getSimpleName());
        if (!ObjectUtils.isEmpty(clazz.getAnnotation(RestController.class))
                || !ObjectUtils.isEmpty(Controller.class)) {
            try {
                if (!ctx.containsBean(clazzName)) {
                    ctx.getBeanFactory().createBean(clazz);
                }

                doUnregisterBean(clazzName);
                return doRegisterBean(clazzName);
            }catch (Throwable e) {
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
}
