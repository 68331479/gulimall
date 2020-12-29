package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author: LDeng
 * @Date: 2020-12-29 18:45
 */
@Data
public class UserRegisteVo {

    @NotEmpty(message = "必须提交用户名")
    @Length(min=6,max=18,message = "用户名必须是6-18位")
    private String username;

    @NotEmpty(message = "密码必须填写")
    @Length(min=6,max=18,message = "用户名必须是6-18位")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1][0-9]{11}$/",message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
