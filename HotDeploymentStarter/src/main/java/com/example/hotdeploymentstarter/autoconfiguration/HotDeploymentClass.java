package com.example.hotdeploymentstarter.autoconfiguration;

/**
 * @author: WSC
 * @DATE: 2022/7/14
 * @DESCRIBE:
 **/
public class HotDeploymentClass {
    /**
     * 文件的hash值
     */
    private String hash;
    /**
     * 文件路径
     */
    private String classPath;
    /**
     * 文件类名称
     */
    private String className;
    /**
     * 文件上传账号
     */
    private String uploader;
    /**
     * 文件上传的IP地址
     */
    private String uploadIp;
    /**
     * 文件上传到服务器的时间
     */
    private Long uploadTime;
    /**
     * 文件部署到程序的时间
     */
    private Long deployTime;
}
