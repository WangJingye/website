package com.delcache.backend.system.controller;

import com.delcache.backend.common.controller.BaseController;
import com.delcache.backend.system.service.AdminService;
import com.delcache.common.entity.Admin;
import com.delcache.common.entity.SiteInfo;
import com.delcache.component.Db;
import com.delcache.component.Encrypt;
import com.delcache.component.Request;
import com.delcache.component.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "system/admin")
public class AdminController extends BaseController {

    private Map<String, String> statusList = new LinkedHashMap<String, String>() {
        {
            put("1", "可用");
            put("0", "禁用");
        }
    };

    @Autowired
    AdminService adminService;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) {
        Map<String, Object> params = Request.getInstance(request).getParams();
        Map<String, Object> res = this.adminService.getList(params);
        model.addAttribute("list", res.get("list"));
        model.addAttribute("statusList", this.statusList);
        model.addAttribute("pagination", res.get("pagination"));
        model.addAttribute("params", params);
        return "system/admin/index";
    }


    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit(HttpServletRequest request, Model model) throws Exception {
        String id = request.getParameter("admin_id");
        model.addAttribute("title", "创建账号");
        if (Util.parseInt(id) != 0) {
            Admin data = (Admin) Db.table(Admin.class).where("admin_id", id).find();
            if (data == null) {
                throw new Exception("数据有误");
            }
            model.addAttribute("data", data);
            model.addAttribute("title", "编辑账号 - " + data.getAdminId());
        }
        return "system/admin/edit";
    }

    @ResponseBody
    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public Object edit(HttpServletRequest request) throws Exception {
        Map<String, Object> params = Request.getInstance(request).getParams();
        Admin admin;
        if (Util.parseInt(params.get("admin_id")) != 0) {
            admin = (Admin) Db.table(Admin.class).where("admin_id", params.get("admin_id")).find();
            if (admin == null) {
                throw new Exception("参数有误");
            }
        } else {
            params.remove("admin_id");
            admin = new Admin();
        }
        String avatar = Request.getInstance(request).parseFileAndParams("avatar", "upload/admin");
        params.put("avatar", avatar);
        admin.load(params);
        Db.table(Admin.class).save(admin);
        return this.success("修改成功");
    }

    @ResponseBody
    @RequestMapping(value = "set-status", method = RequestMethod.POST)
    public Object setStatus(HttpServletRequest request) throws Exception {
        String id = request.getParameter("id");
        if (Util.parseInt(id) == 0) {
            throw new Exception("参数有误");
        }
        Db.table(Admin.class).where("id", id).update("status", request.getParameter("status"));
        return this.success("操作成功");
    }

    @ResponseBody
    @RequestMapping(value = "reset-password", method = RequestMethod.POST)
    public Object resetPassword(HttpServletRequest request) throws Exception {
        if (Util.parseInt(request.getParameter("admin_id")) == 0) {
            throw new Exception("参数有误");
        }
        Admin user = (Admin) Db.table(Admin.class).where("admin_id", request.getParameter("admin_id")).find();
        SiteInfo siteInfo = (SiteInfo) Db.table(SiteInfo.class).find();
        this.adminService.changePassword(user, siteInfo.getDefaultPassword());
        return this.success("密码已重置");
    }

    @RequestMapping(value = "profile", method = RequestMethod.GET)
    public String profile(Model model) {
        return "system/admin/profile";
    }

    @ResponseBody
    @RequestMapping(value = "change-profile", method = RequestMethod.POST)
    public Object changeProfile(HttpServletRequest request) throws Exception {
        Admin user = (Admin) request.getSession().getAttribute("user");
        Map<String, Object> params = Request.getInstance(request).getParams();
        String avatar = Request.getInstance(request).parseFileAndParams("avatar", "upload/admin");
        params.put("avatar", avatar);
        user.load(params);
        Db.table(Admin.class).save(user);
        request.getSession().setAttribute("user", user);
        return this.success("修改成功");
    }


    @ResponseBody
    @RequestMapping(value = "change-password", method = RequestMethod.POST)
    public Object changePassword(HttpServletRequest request) throws Exception {
        Admin user = (Admin) request.getSession().getAttribute("user");
        if (!user.getPassword().equals(Encrypt.encryptPassword(request.getParameter("password"), user.getSalt()))) {
            throw new Exception("当前登录密码有误～");
        }
        if (!request.getParameter("rePassword").equals(request.getParameter("newPassword"))) {
            throw new Exception("新密码与验证密码不一致～");
        }
        this.adminService.changePassword(user, request.getParameter("newPassword"));
        request.getSession().removeAttribute("user");
        return this.success("密码已重置");
    }
}
