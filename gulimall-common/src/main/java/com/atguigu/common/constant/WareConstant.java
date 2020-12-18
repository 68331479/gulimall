package com.atguigu.common.constant;

/**
 * @Author: LDeng
 * @Date: 2020-12-04 16:35
 */
public class WareConstant {

    public enum PurchaseEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVED(2,"已领取"),
        FINISHED(3,"已完成"),
        HASERROR(4,"有异常");

        private int code;

        private String msg;

        PurchaseEnum(int code,String msg){
            this.code=code;
            this.msg=msg;
        }

        public int getCode(){
            return code;
        }

        public String getMsg(){
            return msg;
        }
    }


    public enum PurchaseDetailEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISHED(3,"已完成"),
        HASERROR(4,"采购失败");

        private int code;

        private String msg;

        PurchaseDetailEnum(int code,String msg){
            this.code=code;
            this.msg=msg;
        }

        public int getCode(){
            return code;
        }

        public String getMsg(){
            return msg;
        }
    }

}
