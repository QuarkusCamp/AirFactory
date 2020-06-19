package org.airfactory.data.order;

public class OrderMessage {

    private Long id;

    private BaseOrderItem baseOrderItem;

    public OrderMessage(){

    }

    public OrderMessage(Long id, BaseOrderItem baseOrderItem) {
        this.id = id;
        this.baseOrderItem = baseOrderItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BaseOrderItem getBaseOrderItem() {
        return baseOrderItem;
    }

    public void setBaseOrderItem(BaseOrderItem baseOrderItem) {
        this.baseOrderItem = baseOrderItem;
    }

    @Override
    public String toString() {
        return "Order [id=" + id + "{" + baseOrderItem + "}";
    }
}
