package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testMD5(){
        String s =
                DigestUtils.md5Hex("123456");
        //加盐
        String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$aaaaaaaa");
        System.out.println(s);
        System.out.println(s1);


        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);

        boolean matches = passwordEncoder.matches("123456", encode);
        System.out.println(matches);
    }
}
