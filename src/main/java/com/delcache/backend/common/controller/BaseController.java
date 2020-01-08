package com.delcache.backend.common.controller;

import com.delcache.backend.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

public class BaseController extends com.delcache.common.controller.BaseController {

    @Autowired
    MenuService menuService;

    public String render(String view, Model model) {
        model.addAttribute("activeMenu", this.menuService.getActiveMenu());
        return view;
    }
}
