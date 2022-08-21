package top.xizai.deployment.enums;

/**
 * @author: WSC
 * @DATE: 2022/8/1
 * @DESCRIBE: 部署方法
 **/
public enum DeployType {
    REPLACE_CLASS(1, "将整个对象进行替换"),
    REPLACE_METHOD(2, "只对指定的方法进行替换"),
    ROLLBACK(3, "将替换掉的字节码进行回滚");

    private int code;
    private String name;

    DeployType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
