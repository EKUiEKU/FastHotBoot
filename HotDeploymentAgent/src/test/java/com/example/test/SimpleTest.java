package com.example.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import top.xizai.deployment.entity.AgentParams;
import top.xizai.deployment.entity.DeployInfo;
import top.xizai.deployment.enums.DeployType;

import java.nio.charset.StandardCharsets;

/**
 * @author: WSC
 * @DATE: 2022/8/3
 * @DESCRIBE:
 **/
public class SimpleTest {
    private static String secret = "abcd";

    public static void main(String[] args) {
        System.out.println(getFileHash("C:\\DevEnv\\com\\example\\hotdeploymentstarter\\test\\SayService.class"));
    }

    public static String getFileHash(String filePath) {
        String fileString = FileUtil.readString(filePath, StandardCharsets.UTF_8);
        return md5(fileString);
    }

    public static String md5(String source) {
        return SecureUtil.md5(secret + source + secret);
    }
}
