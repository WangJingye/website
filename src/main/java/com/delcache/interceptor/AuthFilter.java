package com.delcache.interceptor;

import com.delcache.backend.common.config.Config;
import com.delcache.backend.system.service.MenuService;
import com.delcache.common.entity.Admin;
import com.delcache.common.entity.Menu;
import com.delcache.common.entity.OperationLog;
import com.delcache.component.Db;
import com.delcache.component.Request;
import com.delcache.component.UrlManager;
import com.delcache.component.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        return checkAuth(request, response, o);
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView mv) throws Exception {
        if (this.checkLogin(request) && !UrlManager.isAjax(request)) {
            MenuService ms = new MenuService();
            List<Menu> activeMenu = ms.getActiveMenu();
            if (activeMenu != null) {
                mv.addObject("activeMenu", activeMenu);
                mv.addObject("user", request.getSession().getAttribute("user"));
            }
        }
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, Exception e) throws Exception {

    }

    /**
     * 判断是否需要登录
     *
     * @param request
     * @return
     */
    private boolean checkLogin(HttpServletRequest request) {
        for (Map.Entry<String, String[]> entry : Config.actionNoLoginList.entrySet()) {
            for (String action : entry.getValue()) {
                String uri = "/" + Util.stringTrim(entry.getKey() + "/" + action, "/");
                if (UrlManager.parseRequestUrl(request.getRequestURI()).equals(uri)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean checkAuth(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        Admin user = (Admin) request.getSession().getAttribute("user");
        //不需要检测登录，那么同时就不需要验证权限
        if (!this.checkLogin(request)) {
            return true;
        }
        if (null == user) {
            return this.redirectLogin(request, response);
        }
        boolean checkAuth = true;
        for (Map.Entry<String, String[]> entry : Config.actionWhiteList.entrySet()) {
            for (String action : entry.getValue()) {
                String uri = "/" + Util.stringTrim(entry.getKey() + "/" + action, "/");
                if (UrlManager.parseRequestUrl(request.getRequestURI()).equals(uri)) {
                    checkAuth = false;
                    break;
                }
            }
        }
        //不需要检测权限
        if (!checkAuth) {
            return true;
        }
        //用户被禁用
        if (user.getStatus() == 0) {
            return this.redirectNoAuth(request, response);
        }
        String uri = UrlManager.parseRequestUrl(request.getRequestURI());
        Menu currentMenu = (Menu) Db.table(Menu.class).where("url", uri).find();
        if (currentMenu == null) {
            return this.redirectNotFound(request, response);
        }
        //验证用户权限,identity = 1 为超级管理员
        if (user.getIdentity() == 0 && !this.checkUserAuth(request, currentMenu)) {
            return this.redirectNoAuth(request, response);
        }
        if (UrlManager.isAjax(request)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String params = objectMapper.writeValueAsString(Request.getInstance(request).getParams());
            OperationLog opLog = new OperationLog();
            opLog.setCreateTime(Util.time());
            opLog.setOperatorId(user.getAdminId());
            opLog.setUrl(request.getRequestURI());
            opLog.setPost(params);
            opLog.setReferUrl(request.getHeader("Referer"));
            opLog.setIp(Util.getRemoteIp(request));
            Db.table(OperationLog.class).save(opLog);
        }
        return true;
    }

    private Boolean redirectLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        if (UrlManager.isAjax(request)) {
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            out.print(Util.objectToString(Util.error("未登录", 999)));
            out.close();
        } else {
            response.sendRedirect("/login.html");//todo 跳转到报错界面，然后再跳到登录界面
        }
        return false;
    }

    private Boolean redirectNoAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        if (UrlManager.isAjax(request)) {
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            out.print(Util.objectToString(Util.error("您暂无该权限")));
            out.close();
        } else {
            throw new Exception("您暂无该权限");
        }
        return false;
    }

    private Boolean redirectNotFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        if (UrlManager.isAjax(request)) {
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            out.print(Util.objectToString(Util.error("您访问的页面不在权限列表中")));
            out.close();
        } else {
            throw new Exception("您访问的页面不在权限列表中");
        }
        return false;
    }

    private Boolean checkUserAuth(HttpServletRequest request, Menu currentMenu) {
        Admin user = (Admin) request.getSession().getAttribute("user");
        String sql = "select a.* from tbl_role_menu as a left join tbl_role_admin as b on b.role_id = a.role_id where b.admin_id=\"" + user.getAdminId() + "\" and a.menu_id=\"" + currentMenu.getId() + "\"";
        Map<String, Object> roleMenu = (Map<String, Object>) Db.table(Map.class).find(sql);
        if (roleMenu == null) {
            return false;
        }
        return true;
    }

    /**
     * @param request
     * @return Create Date:2013-6-5
     * @author Shine
     * Description:获取IP
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
