package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2020-12-31 15:46
 */
@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
