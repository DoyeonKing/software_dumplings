package com.example.springboot.common;

public class Result {
    private String code;
    private String msg;
    private Object data;


    // 【新增常量定义】
    public static final String CODE_SUCCESS = "200"; // 成功
    public static final String CODE_PARAM_ERROR = "400"; // 参数错误
    public static final String CODE_AUTH_ERROR = "401"; // 认证失败
    public static final String CODE_NOT_FOUND = "404"; // 资源未找到
    public static final String CODE_SYS_ERROR = "500"; // 系统错误 (默认)
    public static final String CODE_BIZ_ERROR = "600"; // 业务逻辑错误


    public static Result success(){
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }
    public static Result success(Object data){
        Result result = success();
        result.setData(data);
        return result;
    }
    public static Result error(){
        Result result = new Result();
        result.setCode("500");
        result.setMsg("加载失败");
        return result;
    }
    public static Result error(String code, String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 【新增方法】: 允许返回自定义成功消息，没有数据
    public static Result success(String msg){
        Result result = new Result();
        result.setCode("200");
        result.setMsg(msg);
        return result;
    }
    // 【新增方法】：同时允许返回自定义成功消息和数据
    public static Result success(String msg, Object data){
        Result result = new Result();
        result.setCode("200");
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
