package top.xizai.deployment.entity;

/**
 * @author: WSC
 * @DATE: 2022/8/4
 * @DESCRIBE: 用来封装Class字节码
 **/
public class ByteObject {
    /**
     * 用来储存Class字节码
     */
    private byte[] bytes;
    /**
     * Class当前的版本
     */
    private Long version;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ByteObject(byte[] bytes, Long version) {
        this.bytes = bytes;
        this.version = version;
    }
}
