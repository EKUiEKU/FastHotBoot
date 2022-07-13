package top.xizai.deployment.impl;

import top.xizai.deployment.interfae.Constant;
import top.xizai.deployment.interfae.IService;

import java.util.Random;

/**
 * @author: WSC
 * @DATE: 2022/7/11
 * @DESCRIBE:
 **/
public class SampleServiceImpl implements IService {
    private static int RAND = new Random().nextInt();

    @Override
    public String hello() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return "Hello Hot Deployment!NAME:" + Constant.NAME + " Build By 22:43 RAND IS " + RAND;
    }
}
