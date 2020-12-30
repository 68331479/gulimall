package com.atguigu.gulimall.auth.controller;


import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserRegisteVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
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

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //TODO:接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis()-l<60*1000){//60秒内不能再发
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


    //RedirectAttributes redirectAttributes 模拟重定向携带数据， 本质利用session
    //只要跳到下一个页面，取出数据以后，session里面的数据就会删掉
    //TODO 分布式下session 携带数据的问题，
    @PostMapping("/regist")
    public String regist(@Valid UserRegisteVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField, FieldError::getDefaultMessage
                    ));
            redirectAttributes.addFlashAttribute("errors",errors);
            //校验出错，带错误信息转发到注册页
            //return "foward:/reg.html";// 这里不能用转发， 这个请求是POST， reg只接收GET请求
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //校验验证码
        String code = vo.getCode();//能来到这里， 说明校验已经通过
        String phone = vo.getPhone();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(s)){//如果redis里面没有取到说明已经过期了
            if(code.equals(s.split("_")[0])){//验证码对比通过
                //删除验证码,令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
                //调用<<远程服务 guli-member>>进行注册
                R regist = memberFeignService.regist(vo);
                if(regist.getCode()==0){
                    //远程调用成功
                    //注册成功回首页。
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    Map<String,String> errors=new HashMap<>();
                    errors.put("msg",regist.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    //远程调用失败
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else{
                Map<String, String> errors= new HashMap<>();
                errors.put("code","验证码对比不通过");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{//验证码过期
            Map<String, String> errors= new HashMap<>();
            errors.put("code","验证码过期");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }



    }
}
