package com.example.hotdeploymentstarter.entity;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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

    /**
     * 在初始化对象的时候把数据读到内存中
     */
    @PostConstruct
    public void init() {
        readClassSetInfoFromFile();
    }

    private String getSelfJsonFilePath() {
        String selfJsonEncodeFileName = md5(Constant.CLASSES_INFO);
        return hotDeployProperties.getClassFilePath() + File.separatorChar + selfJsonEncodeFileName;
    }

    private String getJsonMD5FilePath() {
        String selfJsonMD5EncodeFileName = md5(Constant.MD5_VERIFICATION);
        return hotDeployProperties.getClassFilePath() + File.separatorChar + selfJsonMD5EncodeFileName;
    }

    @Override
    public boolean add(HotDeploymentClass hotDeploymentClass) {
        boolean ret = super.add(hotDeploymentClass);
        if (ret) {
            saveClassSetInfo();
        }
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        if (ret) {
            saveClassSetInfo();
        }

        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends HotDeploymentClass> c) {
        boolean ret = super.addAll(c);
        if (ret) {
            saveClassSetInfo();
        }

        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = super.removeAll(c);
        if (ret) {
            saveClassSetInfo();
        }

        return ret;
    }


    /**
     * 保存所有类的信息
     */
    public void saveClassSetInfo() {
        String selfJson = JSON.toJSONString(this.toArray());
        String selfJsonMd5 = md5(selfJson);

        String selfJsonFilePath = getSelfJsonFilePath();
        String md5FilePath = getJsonMD5FilePath();

        try {
            if (FileUtil.exist(selfJsonFilePath)) {
                FileUtil.del(selfJsonFilePath);
            }
            if (FileUtil.exist(md5FilePath)) {
                FileUtil.del(md5FilePath);
            }

            FileUtil.writeString(selfJson, selfJsonFilePath, StandardCharsets.UTF_8);
            FileUtil.writeString(selfJsonMd5, md5FilePath, StandardCharsets.UTF_8);

            log.info("classes info is be stored in disk.");
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

    /**
     * 读取本地的数据
     */
    public void readClassSetInfoFromFile() {
        String selfJsonFilePath = getSelfJsonFilePath();
        String md5FilePath = getJsonMD5FilePath();

        String selfJson = FileUtil.readString(selfJsonFilePath, StandardCharsets.UTF_8);
        String selfJsonMd5 = FileUtil.readString(md5FilePath, StandardCharsets.UTF_8);

        // 验签
        String md5Verification = md5(selfJson);
        if (md5Verification.equals(selfJsonMd5)) {
            log.error("{} is be modified", getJsonMD5FilePath());
            throw new UnsupportedOperationException("read local classes set info failure.File is be modified");
        }

        // 添加到容器里面
        List<HotDeploymentClass> localClassInfo = JSON.parseArray(selfJson, HotDeploymentClass.class);
        for (HotDeploymentClass deploymentClass : localClassInfo) {
            this.add(deploymentClass);
        }

        if (localClassInfo.size() != 0) {
            log.info("loading {} class info from disk.", localClassInfo.size());
        }
    }

    private String md5(String source) {
        return SecureUtil.md5(hotDeployProperties.getSalt() + source + hotDeployProperties.getSalt());
    }
}
