package com.delcache.backend.common.service;

import com.delcache.common.dao.BaseDao;
import com.delcache.component.Db;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Transactional
public class BaseService extends com.delcache.common.service.BaseService {

    @Autowired
    BaseDao baseDao;

    public Map<String, Object> pagination(Db selector, Map<String, Object> params) {
        if (!StringUtils.isEmpty(params.get("page"))) {
            selector.page = Integer.parseInt(params.get("page").toString());
        }
        if (!StringUtils.isEmpty(params.get("pageSize"))) {
            selector.pageSize = Integer.parseInt(params.get("pageSize").toString());
        }
        selector.limit(String.valueOf((selector.page - 1) * selector.pageSize) + "," + String.valueOf(selector.pageSize));
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> pagination = new HashMap<>();
        int total = selector.count();
        pagination.put("total", total);
        pagination.put("pageSize", selector.pageSize);
        pagination.put("totalPage", (int) Math.ceil((double) total / selector.pageSize));
        pagination.put("current", selector.page);
        result.put("list", selector.findAll());
        result.put("pagination", pagination);
        return result;
    }
}
