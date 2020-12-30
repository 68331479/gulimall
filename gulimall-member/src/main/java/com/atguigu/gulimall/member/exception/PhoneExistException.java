package com.atguigu.gulimall.member.exception;

/**
 * @Author: LDeng
 * @Date: 2020-12-30 11:40
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已经注册过");
    }
}
