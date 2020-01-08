package com.delcache.common.controller;

import com.delcache.component.Util;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class ErrorController extends BasicErrorController {

    public ErrorController(ServerProperties serverProperties) {
        super(new DefaultErrorAttributes(), serverProperties.getError());
    }

    //通过浏览器访问的url 如果发生异常全部会被浏览并跳转到list.html页面
    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        //请求的状态

        HttpStatus status = this.getStatus(request);
        response.setStatus(status.value());
        Map<String, Object> model = this.getErrorAttributes(request,
                this.isIncludeStackTrace(request, MediaType.TEXT_HTML));
        ModelAndView modelAndView = this.resolveErrorView(request, response, status, model);

        if (modelAndView != null) {
            return modelAndView;
        }
        String message = model.get("message").toString();
        if (status.value() == 404) {
            message = "您所请求的地址不存在";
        }
        String url = (String) model.get("redirect");
        if (StringUtils.isEmpty(url)) {
            url = "/";
        }
        response.setContentType("text/html");
        ModelAndView mv = new ModelAndView();
        mv.setViewName("system/error/index");
        mv.addObject("message", message);
        mv.addObject("url", url);
        return mv;
    }

    //通过ajax请求访问的接口如果发生异常会调用这个方法
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = this.getErrorAttributes(request,
                this.isIncludeStackTrace(request, MediaType.ALL));
        HttpStatus status = HttpStatus.valueOf(200);
        //输出自定义的Json格式
        Map<String, Object> map = Util.error(body.get("message").toString(), 400);
        return new ResponseEntity<>(map, status);
    }

}
