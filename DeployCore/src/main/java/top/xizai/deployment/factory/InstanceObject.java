package top.xizai.deployment.factory;

/**
 * @author: WSC
 * @DATE: 2022/8/4
 * @DESCRIBE: 类的实例对象,携带版本号
 **/
public class InstanceObject {
    /**
     * 储存加载好的Class对象
     */
    private Object inst;
    /**
     * 版本号
     */
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Object getInst() {
        return inst;
    }

    public void setInst(Object inst) {
        this.inst = inst;
    }

    public InstanceObject(Object inst, Long version) {
        this.inst = inst;
        this.version = version;
    }
}
