package com.example.hotdeploymentstarter.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.hotdeploymentstarter.entity.Constant;
import com.example.hotdeploymentstarter.entity.HotDeployProperties;
import com.example.hotdeploymentstarter.entity.HotDeploymentClass;


import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
public class DeployUtils {
    private HotDeployProperties hotDeployProperties;

    public DeployUtils(HotDeployProperties properties) {
        this.hotDeployProperties = properties;
    }

    /**
     * 简单的校验Class字节码是否合法
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
     * @param source
     * @return
     */
    public String md5(String source) {
        return SecureUtil.md5(hotDeployProperties.getSalt() + source + hotDeployProperties.getSalt());
    }

    /**
     * AES加密
     * @param source
     * @return
     */
    public String aseEncode(String source) {
        return SecureUtil.aes(hotDeployProperties.getSalt().getBytes())
                .encryptBase64(source, StandardCharsets.UTF_8);
    }

    /**
     * AES解密
     * @param source
     * @return
     */
    public String aseDecode(String source) {
        return SecureUtil.aes(hotDeployProperties.getSalt().getBytes())
                .decryptStr(source);
    }

    /**
     * 获取本地数据的根目录
     * @return
     */
    public String getRootPath() {
        return hotDeployProperties.getClassFilePath() + File.separatorChar + Constant.ROOT_NAME;
    }

    /**
     * 获取保存HotDeploymentClassSet的本地文件路径
     * @return
     */
    public String getHotDeploymentClassSetFilePath() {
        String selfJsonEncodeFileName = md5(Constant.CLASSES_INFO);
        return getRootPath() + File.separatorChar + selfJsonEncodeFileName;
    }

    /**
     * 获取保存HotDeploymentClassSet校验文件的本地文件路径
     * @return
     */
    public String getHotDeploymentClassSetMD5FilePath() {
        String selfJsonEncodeMD5FileName = md5(Constant.MD5_VERIFICATION);
        return getRootPath() + File.separatorChar + selfJsonEncodeMD5FileName;
    }

    /**
     * 获取热部署文件保存的目录
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
     * @param filePath
     * @return
     */
    public String getFileHash(String filePath) {
        String fileString = FileUtil.readString(filePath, StandardCharsets.UTF_8);
        return md5(fileString);
    }
}
