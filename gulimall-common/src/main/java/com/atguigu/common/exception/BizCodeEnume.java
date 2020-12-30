package com.atguigu.common.exception;

/**
 * @Author: LDeng
 * @Date: 2020-11-24 17:12
 */

/**
 * 10: 通用
 * 11： 商品
 * 12： 订单
 * 13： 购物车
 * 14： 物流
 * 15： 用户模块
 */


public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"位置系统异常"),
    VAILD_EXCEPTIOON(10001,"参数校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    SMS_CODE_EXCEPTION(10002,"刷新短信验证码频率太高"),
    USER_EXIST_EXCEPTION(150001,"用户已经存在异常"),
    PHONE_EXIST_EXCEPTION(150002,"手机号已经存在异常");

    private int code;
    private String msg;

    BizCodeEnume(int code,String msg){
        this.code = code;
        this.msg=msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
