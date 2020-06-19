package org.airfactory.data.shelf;

import org.airfactory.data.order.BaseOrderItem;
import org.airfactory.data.order.OrderMessage;

public class OrderCompletedMessage extends OrderMessage {

    public OrderCompletedMessage(Long id, BaseOrderItem baseOrderItem) {
        super(id, baseOrderItem);
    }
}
