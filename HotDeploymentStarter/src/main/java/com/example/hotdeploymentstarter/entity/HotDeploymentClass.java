package com.example.hotdeploymentstarter.entity;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HotDeploymentClass that = (HotDeploymentClass) o;
        return hash.equals(that.hash) && classPath.equals(that.classPath) && className.equals(that.className) && uploader.equals(that.uploader) && uploadIp.equals(that.uploadIp) && uploadTime.equals(that.uploadTime) && deployTime.equals(that.deployTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, classPath, className, uploader, uploadIp, uploadTime, deployTime);
    }
}
