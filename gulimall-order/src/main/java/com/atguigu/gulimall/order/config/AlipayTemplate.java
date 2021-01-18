package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000117601549";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDWtSL9ll0jYqlV5HRSqR1xKQkTGpx81cIRBW5gx0N3z+PL2iYYV4PVixBj/I5mJzWIakBtabWelohHjAX1xSqpFBRX5RyrCgUeOaf/vk3f348lD4uc1osSYoWbDk3tdSjDIQO1EkOzFh59LwtFuvOPQX2cQOfXVft1qrrOE9IB94DPiTBQX9sHl7WOOo/FpKK3U85k9pAA2oW0nyZcK10DBUTn1O8iCb2lCfoyyBsr+aU2GD0LmM3DZrjutySChFpGdMQ6YRpavoxZ9+W5nGtxRYFJdxhQD8cvYRdrcuKANp+ePt6RxMDLbDazUZjWnuQQ+9QxS96bdFzvPRUXWplTAgMBAAECggEBAIsp0Moo4amD01n2v2ascobcICOfxfor2qOJr6dOwG4x8uY4OSTh5HNOtYr5MZsIouwOYkAImk12KFKncV2BNEtu1li6CcDat8KFgFyFDpiioHdXAhjf/jgmX47G0L+uhoMjLPH6KqMzHQPqsBW8PQ2HV1ElyJ+L/Xag+H+F3UKj5oVfpZeGYwWUXeeAlZzbeHHADr03dHYCTJfAz3K0oBVsSaF4HpoxtLXuuytBR79DQec+WsNxdet64RCNliz7LBKxtqsgbE+Q/ebe4icdkeTUDiMR4r7JCHgvN8ZKBvurJJUnnj0ZuREX17OKsCUEkYWbiPvvmm482YPd1w8ogHECgYEA/rC2evX7NlLE/3id7luy+9qdYJNyQNCA1xoZofsWlZxt7MqEdrec22Sd+IUtqSksiUO1OeA3IWtiAfD39LtjzIDX0UCzU59E5QhuS9UEGeJk1HC8QM50u05Uzm+7t7dRK46zb+cwh+YJzpV0HNevbH9XWouwmgUVNG6wCTI1yW8CgYEA18/J4Sv88hCKUoA4Zo+anoB6Td6X7x2Bpav6c3J/a/bRl2hoa3sLgwVNyESzY7+lb0rYKIkw5blR44gUHzBnk9j76KooGLgLUiaWPSemL5PU9BxosyAYhpYdUQ/blaOaRoJve/4xZXCqtavZxhnuNdRESUrRSeE3CMYHsfvIVF0CgYBD/zfo4i1cloKD1lcL4S2K8hXcR+FUDpanVw+K4HdqICZ09qig7WqSZG2MgTnslNKQHySOPB9b68hxzjzU+Qau/aMg+c4mwDnrj/Gqi0eaBYh381U/VB90NniL2ObcKLQiFeowMSAS2Ea8AcjLPoZWSDUxNmRC6qJ7qiWTfvU/PwKBgDODEoHfuQmibxHQLyYz4skwi4Dov8VAesCrsyU5UDbZ/B8yubHl9dJ5Qp6p31PQ29EbwUqzrGs499XilkL3j9GXelGHw/hafcFHGpdQTneOxsbZvRf8cj41uJuvuE8YdOudmLS08N9wrB6e/WUurVBF8WK5LAmBa1x5wD54hdG1AoGARgQIWlZQeMx6yGQa2FlBxiIaZvqjlDxr3EKgq61SS7DzycIY5NWbhEKxX5chgZgzSqCUv4kMt3e1JXTqZB6njwTtGISxU8CBPSp6Z3RSrASgkUGeEmkv23jlYbl6YQgpuK/Xglf8MhZVrkbaVvm4mY+t/+gcON5WyqNt8CGuThc=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgxMUmPdbjCMJ8LJ7k81tDLUfEcS0hHZG1Znc/+0cS0LppyzadHkoiR/s76CgG1Q9S+4TQwHKGa+4orE+E7aWeWsPU6pfolWWIBWHJ0LeY1E+Wi6vSaRk2l6QCsTLIpq9+d75gtgKkh7szKe4S2ocH1GxHCHQ1bbITNt4ewrnNt9Hj/jvl8yKqTycnj2Svu3AhH/ZX5xHa6GHd5GQ75fMwozRbd8OYOpjPn1PKZ88gt47lzDIJKW2066z2tyZ7ED22nbVcd0ldJKFU/yk6J9v2+K6xSHtUaL9uALWRiX9bOptOIg4zWrJr1wewiVJmMSbU2AunRCUfD8BZ4girVSDiwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://l056yrczwe.52http.tech/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        String timeout_express="30m";

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout_express +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
