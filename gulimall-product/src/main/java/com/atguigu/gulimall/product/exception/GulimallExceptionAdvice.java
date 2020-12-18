package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LDeng
 * @Date: 2020-11-24 16:33
 */

/*
    集中处理所有异常

 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.app")
public class GulimallExceptionAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        log.error("数据校验出错{},异常类型{}", e.getMessage(),e.getMessage(),e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String,String> map=new HashMap<>();
        result.getFieldErrors().forEach((item)->{
            String field = item.getField();
            String message = item.getDefaultMessage();
            map.put(field,message);
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTIOON.getCode(),BizCodeEnume.VAILD_EXCEPTIOON.getMsg()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        throwable.printStackTrace();
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
