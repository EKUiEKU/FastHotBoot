package top.xizai.test.asm;

import java.lang.management.ManagementFactory;

/**
 * @author: WSC
 * @DATE: 2022/7/19
 * @DESCRIBE:
 **/
public class GetSelfPIDTest {
    public static void main(String[] args) {
        String name = ManagementFactory.getRuntimeMXBean().getName();

        System.out.println(name);

        String[] names = name.split("@");

// pid

        int pid = Integer.parseInt(names[0]);

// host name

        String systemName = names[1];

        System.out.println("pid: " + pid);

        System.out.println("hostname: " + systemName);


    }
}
