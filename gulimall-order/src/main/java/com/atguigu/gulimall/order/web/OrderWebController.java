package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 15:03
 */

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        //展示订单确认的数据

        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        System.out.println("订单提交的数据"+vo);
        //下单成功来到支付选择页
        //下单失败，回到订单确认页，重新确认信息
        if(responseVo.getCode()==0){
            model.addAttribute("submitOrderResponseVo",responseVo);
            return "pay";
        }else {
            String msg="下单失败";
            switch (responseVo.getCode()){
                case 1: msg = msg+"-1，令牌校验失败,请刷新再次提交";break;
                case 2: msg = msg+"-2，金额对比失败,订单商品价格发生变化";break;
                case 3: msg=msg+"-3，库存锁定失败,商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
