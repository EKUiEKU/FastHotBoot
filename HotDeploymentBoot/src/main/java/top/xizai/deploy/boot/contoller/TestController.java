package top.xizai.deploy.boot.contoller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello() {
        return "world!!!!";
    }
}
