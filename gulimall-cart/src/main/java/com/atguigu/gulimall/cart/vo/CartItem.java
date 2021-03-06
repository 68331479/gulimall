package com.atguigu.gulimall.cart.vo;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:08
 */

import java.math.BigDecimal;
import java.util.List;

/**
 * 每一个购物项
 */

public class CartItem {

    private Long skuId;
    private Boolean check=true;//是否被选中
    private String title;
    private String image;
    private List<String> skuAttr;//属性组合
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;// 计算得来， 所以不用@Data 注解

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        //计算当前项目小计
        return this.price.multiply(new BigDecimal("" + this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "skuId=" + skuId +
                ", check=" + check +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", skuAttr=" + skuAttr +
                ", price=" + price +
                ", count=" + count +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
