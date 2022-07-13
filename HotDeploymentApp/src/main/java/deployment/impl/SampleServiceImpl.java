package deployment.impl;

import top.xizai.deployment.interfae.Constant;
import top.xizai.deployment.interfae.IService;

/**
 * @author: WSC
 * @DATE: 2022/7/11
 * @DESCRIBE:
 **/
public class SampleServiceImpl implements IService {
    @Override
    public String hello() {
        return "Hello World! NAME:" + Constant.NAME;
    }
}
