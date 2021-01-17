package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: LDeng
 * @Date: 2021-01-17 18:15
 */

@RestController//监听支付宝的消息， 并响应succes给支付宝
public class OrderPayedListener {

    @Autowired
    OrderService orderService;


    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo,HttpServletRequest request){
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            String value = request.getParameter(key);
//            System.out.println("key:"+key+"---value:"+value);
//        }
        String result = orderService.handlePayResult(vo);

        //支付宝只接受这个success字符串回复， 再收到success之后就不再通知
        return result;
    }

}
