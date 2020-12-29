package com.atguigu.gulimall.thirdparty;

import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    SmsComponent smsComponent;
    @Test
    public void sendSMS() {

        smsComponent.sendSmsCode("13641505900","99999");
    }


}
