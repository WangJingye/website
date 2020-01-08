package com.delcache.backend.system.controller;

import com.delcache.backend.common.controller.BaseController;
import com.delcache.common.entity.Admin;
import com.delcache.common.entity.SiteInfo;
import com.delcache.component.Db;
import com.delcache.component.Encrypt;
import com.delcache.component.Util;
import com.delcache.extend.captcha.Captcha;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class PublicController extends BaseController {

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, Model model) {
        SiteInfo siteInfo = (SiteInfo) Db.table(SiteInfo.class).find();
        model.addAttribute("title", siteInfo.getTitle());
        return "system/public/login";
    }

    @ResponseBody
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Object login(HttpServletRequest request) throws Exception {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new Exception("用户名密码有误");
        }
        Admin admin = (Admin) Db.table(Admin.class).where("username", username).find();
        if (admin == null || !admin.getPassword().equals(Encrypt.encryptPassword(password, admin.getSalt()))) {
            throw new Exception("用户名密码有误");
        }
        String verifyCode = (String) request.getSession().getAttribute("verifyCode");
        String captcha = request.getParameter("captcha");
        if (StringUtils.isEmpty(captcha) || !verifyCode.toLowerCase().equals(captcha.toLowerCase())) {
            throw new Exception("验证码有误");
        }
        if (admin.getStatus() == 0) {
            throw new Exception("您的账号已禁用，请联系管理员～");
        }
        admin.setLastLoginTime(Util.time());
        Db.table(Admin.class).save(admin);
        request.getSession().setAttribute("user", admin);
        return this.success("登录成功");
    }

    @RequestMapping(value = "logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Admin user = (Admin) request.getSession().getAttribute("user");
        if (user != null) {
            request.getSession().removeAttribute("user");
        }
        response.sendRedirect("/");
    }

    @RequestMapping(value = "captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Captcha.showImage(request, response);
    }
}
