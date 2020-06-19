package org.airfactory;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import org.airfactory.data.order.BaseOrderItem;
import org.airfactory.data.order.OrderCompletedMessage;
import org.airfactory.data.order.OrderDisableMessage;
import org.airfactory.data.order.OrderSentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/order")
public class OrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderResource.class);

    private MessageProducer<JsonObject> topic;

    @Inject
    EntityManager entityManager;

    @Inject
    public void injectEventBus(EventBus eventBus) {
        topic = eventBus.<JsonObject>publisher(Constants.CLOUD_KITCHEN_ORDER_SENT);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/new")
    @Transactional
    public void newOrder(OrderItem item) {
        try {
            item.date = new Date().getTime();
            item.persist();
            LOG.info("Order [{}] created", item);
            BaseOrderItem baseOrderItem = new BaseOrderItem(item.name, item.temp, item.shelfLife, item.decayRate, item.date);
            baseOrderItem.setId(item.id);
            OrderSentMessage message = new OrderSentMessage(item.id, baseOrderItem);
            topic.write(JsonObject.mapFrom(message));
        } catch (Exception e) {
            LOG.warn(e.getMessage());
        }
    }

    @ConsumeEvent(value = Constants.CLOUD_KITCHEN_ORDER_DISABLE, blocking = true)
    @Transactional
    public void disableOrder(Message<JsonObject> orderDisableObject) {
        OrderDisableMessage orderCompletedMessage = orderDisableObject.body().mapTo(OrderDisableMessage.class);
        OrderItem item = entityManager.find(OrderItem.class, orderCompletedMessage.getId());
        item.isDisable = true;
        item.persist();
        LOG.info("Order [{}] disabled", item);
    }

    @ConsumeEvent(value = Constants.CLOUD_KITCHEN_ORDER_COMPLETED, blocking = true)
    @Transactional
    public void completeOrder(Message<JsonObject> orderCompletedObject) {
        OrderCompletedMessage orderCompletedMessage = orderCompletedObject.body().mapTo(OrderCompletedMessage.class);
        OrderItem item = entityManager.find(OrderItem.class, orderCompletedMessage.getBaseOrderItem().getId());
        item.isCompleted = true;
        item.persist();
        LOG.info("Order [{}] completed", item);
    }
}
