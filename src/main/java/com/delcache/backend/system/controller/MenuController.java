package com.delcache.backend.system.controller;

import com.delcache.backend.common.controller.BaseController;
import com.delcache.backend.system.service.MenuService;
import com.delcache.common.entity.Menu;
import com.delcache.component.Db;
import com.delcache.component.Request;
import com.delcache.component.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "system/menu")
public class MenuController extends BaseController {

    private Map<String, String> statusList = new LinkedHashMap<String, String>() {
        {
            put("1", "可用");
            put("0", "禁用");
        }
    };

    @Autowired
    MenuService menuService;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) {
        Map<String, Object> params = Request.getInstance(request).getParams();
        Map<String, Object> res = this.menuService.getList(params);
        model.addAttribute("list", res.get("list"));
        model.addAttribute("pagination", res.get("pagination"));
        model.addAttribute("statusList", this.statusList);
        model.addAttribute("params", params);
        return "system/menu/index";
    }

    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit(HttpServletRequest request, Model model) throws Exception {
        String id = request.getParameter("id");
        model.addAttribute("title", "创建菜单");
        if (!StringUtils.isEmpty(id)) {
            Menu data = (Menu) Db.table(Menu.class).where("id", id).find();
            if (data == null) {
                throw new Exception("参数有误");
            }
            model.addAttribute("data", data);
            model.addAttribute("title", "编辑菜单 - " + data.getName());
        }
        List<String> urlList = this.menuService.getAllMethodList(id);
        model.addAttribute("urlList", urlList);
        Map<String, String> parentList = this.menuService.getChildMenus(0, 0);
        model.addAttribute("parentList", parentList);
        return "system/menu/edit";
    }

    @ResponseBody
    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public Object edit(HttpServletRequest request) throws Exception {
        Map<String, Object> params = Request.getInstance(request).getParams();
        Menu menu;
        if (!StringUtils.isEmpty(params.get("id"))) {
            menu = (Menu) Db.table(Menu.class).where("id", params.get("id")).find();
            if (menu == null) {
                throw new Exception("参数有误");
            }
        } else {
            params.remove("id");
            menu = new Menu();
        }
        menu.load(params);
        Db.table(Menu.class).save(menu);
        return this.success("操作成功");
    }

    @ResponseBody
    @RequestMapping(value = "set-status", method = RequestMethod.POST)
    public Object setStatus(HttpServletRequest request) throws Exception {
        Map<String, Object> params = Request.getInstance(request).getParams();
        if (Util.parseInt(params.get("id")) == 0) {
            throw new Exception("参数有误");
        }
        Db.table(Menu.class).where("id", params.get("id")).update("status", params.get("status"));
        return this.success("操作成功");
    }
}
