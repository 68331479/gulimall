package com.atguigu.gulimall.cart.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2021-01-03 11:21
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;

    private boolean tempUser=false;
}
