package com.delcache.common.controller;

import com.delcache.component.Util;

import java.util.Map;

public class BaseController {

    public Map<String, Object> success(String message, Object data) {
        return Util.success(message, data);
    }

    public Map<String, Object> success(String message) {
        return Util.success(message);
    }

    public Map<String, Object> error(String message) {
        return Util.error(message);
    }

    public Map<String, Object> error(String message, Object data) {
        return Util.error(message, data);
    }

}
