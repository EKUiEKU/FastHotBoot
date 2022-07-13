package com.example.hotdeploymentstarter.entity;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/**
 * @author: WSC
 * @DATE: 2022/7/14
 * @DESCRIBE: 用来存放热更新的Class文件的信息
 **/
@Component
public class HotDeploymentClassSet extends HashSet<HotDeploymentClass> {
    private Logger log = LoggerFactory.getLogger(HotDeploymentClassSet.class);
    @Autowired
    HotDeployProperties hotDeployProperties;

    private String getSelfJsonFilePath() {
        String selfJsonEncodeFileName = SecureUtil.md5(Constant.CLASSES_INFO);
        return hotDeployProperties.getClassFilePath() + File.separatorChar + selfJsonEncodeFileName;
    }

    private String getJsonMD5FilePath() {
        String selfJsonMD5EncodeFileName = SecureUtil.md5(Constant.MD5_VERIFICATION);
        return hotDeployProperties.getClassFilePath() + File.separatorChar + selfJsonMD5EncodeFileName;
    }

    /**
     * 保存所有类的信息
     */
    public void saveClassSetInfo() {
        String selfJson = JSON.toJSONString(this);
        String selfJsonMd5 = SecureUtil.md5(selfJson);

        String selfJsonFilePath = getSelfJsonFilePath();
        String md5FilePath = getJsonMD5FilePath();

        try {
            FileUtil.writeString(selfJson, selfJsonFilePath, StandardCharsets.UTF_8);
            FileUtil.writeString(selfJsonMd5, md5FilePath, StandardCharsets.UTF_8);
        }catch (Exception e) {
            log.error("save classes info failure!");
            log.error(e.getMessage(), e);
            if (FileUtil.exist(selfJsonFilePath)) {
                FileUtil.del(selfJsonFilePath);
            }

            if (FileUtil.exist(md5FilePath)) {
                FileUtil.del(md5FilePath);
            }
        }
    }
}
