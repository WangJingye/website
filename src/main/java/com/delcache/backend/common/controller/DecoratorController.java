package com.delcache.backend.common.controller;

import com.delcache.backend.system.service.MenuService;
import com.delcache.common.entity.SiteInfo;
import com.delcache.component.Db;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/layouts")
public class DecoratorController {
    @Autowired
    MenuService menuService;

    @RequestMapping(value = "default")
    public String defaultDecorator(HttpServletRequest request,Model model) {
        model.addAttribute("menus", this.menuService.getLayoutMenus());
        model.addAttribute("siteInfo", Db.table(SiteInfo.class).find());
        model.addAttribute("user", request.getSession().getAttribute("user"));
        return "layouts/main";
    }

    @RequestMapping(value = "login")
    public String loginDecorator() {
        return "layouts/login";
    }

    @RequestMapping(value = "empty")
    public String EmptyDecorator() {
        return "layouts/empty";
    }
}
