package com.atguigu.gulimall.order.web;


import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: LDeng
 * @Date: 2021-01-16 16:27
 */

@Controller
public class payWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;


    //1， 将支付页让浏览器展示
    //2， 支付成功后， 跳转到订单列表页
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo  payVo=orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        //支付宝返回的 直接是一个页面， 将此页面直接交个浏览器就行
        System.out.println(pay);
        return pay;
    };


}
