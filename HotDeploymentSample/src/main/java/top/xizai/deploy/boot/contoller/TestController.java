package top.xizai.deploy.boot.contoller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.http.*;
import com.alibaba.fastjson.JSON;
import com.example.hotdeploymentstarter.test.SayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xizai.deployment.constants.FileConstants;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: WSC
 * @DATE: 2022/7/15
 * @DESCRIBE:
 **/
@Slf4j
@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello() {
        return "world!!!!";
    }

    @GetMapping("/say")
    public String say() {
        return new SayService().say();
    }

    @GetMapping("/deploy")
    public String deploy() {
        try {
            String testFileName = "SayService.class.latest";
            InputStream testFileInputStream = this.getClass().getClassLoader().getResourceAsStream(testFileName);
            String prefixPath = FileUtil.getAbsolutePath(new File("."));
            File targetFile = new File(prefixPath + File.separatorChar + testFileName);
            FileUtil.writeFromStream(testFileInputStream, targetFile);

            /**
             * 9821,配置文件里面开放的端口
             */
            String deployUrl = "localhost:9821/deploy";
            Map<String, String> packageNames = new HashMap<>();
            packageNames.put("SayService.class", "com.example.hotdeploymentstarter.test.SayService");

            HttpResponse response = HttpRequest.of(deployUrl)
                    .contentType(ContentType.MULTIPART.getValue())
                    .form("packageNames", JSON.toJSONString(packageNames))
                    .form("SayService.class", targetFile)
                    .setMethod(Method.PUT)
                    .execute();

            return response.body();
        }catch (Throwable e) {
            log.error(e.getMessage(), e);
            return String.format("FAILURE, error message is %s", e.getMessage());
        }
    }
}
