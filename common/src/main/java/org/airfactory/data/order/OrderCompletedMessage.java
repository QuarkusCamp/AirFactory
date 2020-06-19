package org.airfactory.data.order;

public class OrderCompletedMessage extends OrderMessage {

    public OrderCompletedMessage(){

    }

    public OrderCompletedMessage(Long id, BaseOrderItem body) {
        super(id, body);
    }

    @Override
    public String toString() {
        return super.toString() + " completed]";
    }
}
