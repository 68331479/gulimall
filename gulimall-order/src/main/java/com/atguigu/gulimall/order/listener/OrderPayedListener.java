package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: LDeng
 * @Date: 2021-01-17 18:15
 */

@RestController//监听支付宝的消息， 并响应succes给支付宝
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;


    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo,HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            String value = request.getParameter(key);
//            System.out.println("key:"+key+"---value:"+value);
//        }
        //验签，确定是支付宝发送的回调
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
        if (signVerified) {
            //签名验证成功
            System.out.println("支付宝回调成功，验证支付宝签名成功，可以调用处理订单业务-修改订单状态支付成功");
            String result = orderService.handlePayResult(vo);
            //支付宝只接受这个success字符串回复， 再收到success之后就不再通知
            return result;
        } else {
            System.out.println("支付宝回调成功，验证支付宝签名失败");
            return "error";
        }
    }


}
