package top.xizai.deployment.factory;

/**
 * @author: WSC
 * @DATE: 2022/8/4
 * @DESCRIBE: 类对象,携带版本号
 **/
public class ClassObject {
    /**
     * 储存加载好的Class对象
     */
    private Class clazz;
    /**
     * 版本号
     */
    private Long version;
    /**
     * 类全限定名称
     */
    private String className;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ClassObject(Class clazz, Long version, String className) {
        this.clazz = clazz;
        this.version = version;
        this.className = className;
    }
}
