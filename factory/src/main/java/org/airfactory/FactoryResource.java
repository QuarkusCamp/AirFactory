package org.airfactory;

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
import org.airfactory.data.order.OrderSentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/kitchen")
public class FactoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryResource.class);

    private MessageProducer<JsonObject> topic;

    @Inject
    EntityManager entityManager;

    @Inject
    public void injectEventBus(EventBus eventBus) {
        topic = eventBus.<JsonObject>publisher(Constants.CLOUD_KITCHEN_ORDER_REDAY);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ready")
    @Transactional
    public void ready(FactoryOrderItem item) {
        try {
            FactoryOrderItem factoryOrderItem = entityManager.find(FactoryOrderItem.class, item.id);
            if (factoryOrderItem != null) {
                factoryOrderItem.readyForDelivery = true;
                BaseOrderItem baseOrderItem = new BaseOrderItem(factoryOrderItem.name, factoryOrderItem.temp, factoryOrderItem.shelfLife, factoryOrderItem.decayRate, factoryOrderItem.date);
                baseOrderItem.setId(item.orderId);
                OrderCompletedMessage message = new OrderCompletedMessage(item.id, baseOrderItem);
                topic.write(JsonObject.mapFrom(message));
            } else {
                LOG.warn("KitchenOrder [{}] not exited", item);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }

    @ConsumeEvent(value = Constants.CLOUD_KITCHEN_ORDER_SENT, blocking = true)
    @Transactional
    public void getOrder(Message<JsonObject> orderObject) {
        try {
            LOG.info("KitchenOrder getOrder ", orderObject);
            OrderSentMessage orderSentMessage = orderObject.body().mapTo(OrderSentMessage.class);
            BaseOrderItem baseOrderItem = orderSentMessage.getBaseOrderItem();

            FactoryOrderItem factoryOrderItem = new FactoryOrderItem();
            factoryOrderItem.orderId = orderSentMessage.getId();
            factoryOrderItem.name = baseOrderItem.getName();
            factoryOrderItem.temp = baseOrderItem.getTemp();
            factoryOrderItem.shelfLife = baseOrderItem.getShelfLife();
            factoryOrderItem.decayRate = baseOrderItem.getDecayRate();
            factoryOrderItem.date = baseOrderItem.getDate();
            factoryOrderItem.persist();
        } catch (Throwable t) {
            LOG.warn(t.getMessage());
        }
    }
}
