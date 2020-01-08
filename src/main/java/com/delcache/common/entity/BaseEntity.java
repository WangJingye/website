package com.delcache.common.entity;

import com.delcache.component.DbIgnore;
import com.delcache.component.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BaseEntity {

    @DbIgnore
    public Object extra;

    public void load(Map<String, Object> map) {
        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getDeclaredMethods();
        Map<String, Method> methodMap = new HashMap<>();
        for (Method method : methods) {
            methodMap.put(method.getName(), method);
        }
        try {
            for (Field field : fields) {
                String f = field.getName();
                String column = Util.toUnderlineString(f);
                if (!map.containsKey(column)) {
                    continue;
                }
                String fieldType = field.getType().getSimpleName();
                String setMethodName = "set" + Util.toCamelName(f);
                if (methodMap.containsKey(setMethodName)) {
                    Object value = map.get(column);
                    Method setMethod = methodMap.get(setMethodName);
                    if ("String".equals(fieldType)) {
                        String temp = value.toString();
                        setMethod.invoke(this, temp);
                    } else if ("Integer".equals(fieldType) || "int".equals(fieldType)) {
                        Integer temp = Integer.parseInt(value.toString());
                        setMethod.invoke(this, temp);
                    } else if ("Long".equalsIgnoreCase(fieldType)) {
                        Long temp = Long.parseLong(value.toString());
                        setMethod.invoke(this, temp);
                    } else if ("Double".equalsIgnoreCase(fieldType)) {
                        Double temp = Double.parseDouble(value.toString());
                        setMethod.invoke(this, temp);
                    } else if ("Float".equalsIgnoreCase(fieldType)) {
                        Float temp = Float.parseFloat(value.toString());
                        setMethod.invoke(this, temp);
                    } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                        Boolean temp = Boolean.parseBoolean(value.toString());
                        setMethod.invoke(this, temp);
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
