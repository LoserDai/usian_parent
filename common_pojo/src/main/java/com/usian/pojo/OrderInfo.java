package com.usian.pojo;

/**
 * @author Loser
 * @date 2021年12月03日 15:04
 */
public class OrderInfo {
    private String orderItem;
    private TbOrder tbOrder;
    private TbOrderShipping tbOrderShipping;

    public OrderInfo() {
    }

    public String getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(String orderItem) {
        this.orderItem = orderItem;
    }

    public TbOrder getTbOrder() {
        return tbOrder;
    }

    public void setTbOrder(TbOrder tbOrder) {
        this.tbOrder = tbOrder;
    }

    public TbOrderShipping getTbOrderShipping() {
        return tbOrderShipping;
    }

    public void setTbOrderShipping(TbOrderShipping tbOrderShipping) {
        this.tbOrderShipping = tbOrderShipping;
    }
}
