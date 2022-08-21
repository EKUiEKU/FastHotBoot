package top.xizai.deployment.factory;

import java.io.File;

/**
 * @author: WSC
 * @DATE: 2022/8/21
 * @DESCRIBE: 做安全校验的接口
 **/
public interface SecurityFactory {
    /**
     * 获取加密的秘钥
     * @return
     */
    String getSalt();

    /**
     * 做校验的动作
     * @param username
     * @param passwd
     * @param args
     * @return
     */
    boolean doValidate(String username, String passwd, Object... args);

    /**
     * 对请求过来的数据做校验
     * @param body
     * @param args
     * @return
     */
    boolean validateMessage(Object body, Object... args);

    /**
     * 对将要部署的文件做校验
     * 判断文件是否被恶意更改过
     * @param file
     * @param args
     * @return
     */
    boolean validateClassFile(File file, Object... args);
}
