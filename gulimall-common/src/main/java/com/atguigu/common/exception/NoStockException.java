package com.atguigu.common.exception;

/**
 * @Author: LDeng
 * @Date: 2021-01-12 11:43
 */
public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(){
        super("商品库存不足");
    }

    public NoStockException(Long skuId){
        super("商品"+skuId+",没有足够的库存了");
    }

    public NoStockException(String msg){
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
