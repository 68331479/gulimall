package com.atguigu.gulimall.member.exception;

/**
 * @Author: LDeng
 * @Date: 2020-12-30 11:40
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已经存在");
    }
}
