package com.atguigu.gulimall.auth.controller;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserRegisteVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: LDeng
 * @Date: 2020-12-28 15:45
 */

@Controller
public class LoginController {

    //空方法的跳转在GulimallWebConfi里面


    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //TODO:接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis()-l<60*1000){//60秒内不能再发
                System.out.println("debug====>"+(System.currentTimeMillis()-l));
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        redisCode=code+"_"+System.currentTimeMillis();
        //验证码校验 redis key-phone value-code 前缀130000000-> code ,10分钟有效
        //防止同一个手机号在60秒内再次发送验证码，使用时间戳
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,redisCode,10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone,code);


        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegisteVo vo, BindingResult result,Model model){
        if(result.hasErrors()){
//            result.getFieldErrors().stream().collect(Collectors.toMap(
//                    fieldError->{ return fieldError.getField();},
//                    fieldError->{ return fieldError.getDefaultMessage();}
//                    )
//                );
            Map<String, String> errors = result.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField, FieldError::getDefaultMessage
                    ));
            model.addAttribute("errors",errors);
            //校验出错，带错误信息转发到注册页
            return "foward:/reg.html";
        }

        //注册成功回首页, 登录页
        return "redirect:/reg.html";
    }
}