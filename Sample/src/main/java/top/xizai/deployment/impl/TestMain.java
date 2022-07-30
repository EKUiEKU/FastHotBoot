package top.xizai.deployment.impl;

/**
 * @author: WSC
 * @DATE: 2022/7/25
 * @DESCRIBE:
 **/
public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            String say = new SayService().say();
            System.out.println(say);
            Thread.sleep(5000);
        }
    }
}
