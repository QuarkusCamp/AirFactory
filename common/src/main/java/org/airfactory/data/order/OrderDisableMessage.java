package org.airfactory.data.order;

public class OrderDisableMessage extends OrderMessage {

    public OrderDisableMessage(){

    }

    public OrderDisableMessage(Long id, BaseOrderItem body) {
        super(id, body);
    }

    @Override
    public String toString() {
        return super.toString() + " disabled]";
    }
}
