package org.airfactory.data.order;

public class OrderSentMessage extends OrderMessage {

    public OrderSentMessage(){

    }

    public OrderSentMessage(Long id, BaseOrderItem body) {
        super(id, body);
    }

    @Override
    public String toString() {
        return super.toString() + " sent]";
    }
}
