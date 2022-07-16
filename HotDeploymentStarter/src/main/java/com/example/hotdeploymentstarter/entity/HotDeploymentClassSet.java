package com.example.hotdeploymentstarter.entity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.example.hotdeploymentstarter.utils.DeployUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: WSC
 * @DATE: 2022/7/14
 * @DESCRIBE: 用来存放热更新的Class文件的信息
 **/
public class HotDeploymentClassSet extends HashSet<HotDeploymentClass> {
    private Logger log = LoggerFactory.getLogger(HotDeploymentClassSet.class);

    @Autowired
    DeployUtils deployUtils;

    /**
     * 在初始化对象的时候把数据读到内存中
     */
    // @PostConstruct
    // public void init() {
    //     readClassSetInfoFromFile();
    // }

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
        String selfJson = deployUtils.aseEncode(JSON.toJSONString(this.toArray()));
        String selfJsonMd5 = deployUtils.md5(selfJson);

        String selfJsonFilePath = deployUtils.getHotDeploymentClassSetFilePath();
        String md5FilePath = deployUtils.getHotDeploymentClassSetMD5FilePath();

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
        String selfJsonFilePath = deployUtils.getHotDeploymentClassSetFilePath();
        String md5FilePath = deployUtils.getHotDeploymentClassSetMD5FilePath();

        if (!FileUtil.exist(selfJsonFilePath) || !FileUtil.exist(md5FilePath)) {
            return;
        }

        this.clear();

        log.info("loading data from disk!");

        String selfJson = FileUtil.readString(selfJsonFilePath, StandardCharsets.UTF_8);
        String selfJsonMd5 = FileUtil.readString(md5FilePath, StandardCharsets.UTF_8);

        // 验签
        String md5Verification = deployUtils.md5(selfJson);
        if (!md5Verification.equals(selfJsonMd5)) {
            log.error("{} is be modified", deployUtils.getHotDeploymentClassSetFilePath());
            throw new UnsupportedOperationException("read local classes set info failure.File is be modified");
        }

        // 解密
        String selfJsonDecode = deployUtils.aseDecode(selfJson);

        // 添加到容器里面
        List<HotDeploymentClass> localClassInfo = JSON.parseArray(selfJsonDecode, HotDeploymentClass.class);
        for (HotDeploymentClass deploymentClass : localClassInfo) {
            this.add(deploymentClass);
            Date uploadDate = new Date(deploymentClass.getUploadTime());
            log.info("load <{}> info from disk,upload time is {}", deploymentClass.getClassPath()
                    , DateUtil.format(uploadDate, "yyyy-MM-dd HH:mm:ss"));
        }

        if (localClassInfo.size() != 0) {
            log.info("load {} class info from disk successfully.", localClassInfo.size());
        }

        // 重新热部署一下
        deployUtils.hotDeployment(this.stream().collect(Collectors.toList()));
    }
}
