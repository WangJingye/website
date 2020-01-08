package com.delcache.website;

import com.delcache.component.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = "com.delcache")
public class WebsiteApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(WebsiteApplication.class, args);
        //将run方法的返回值赋值给工具类中的静态变量
        SpringUtil.setApplicationContext(applicationContext);
    }

}
