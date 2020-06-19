package org.airfactory;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
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
import org.airfactory.data.ShelfItem;
import org.airfactory.data.ShelfOrderItem;
import org.airfactory.data.TempType;
import org.airfactory.data.order.BaseOrderItem;
import org.airfactory.data.order.OrderCompletedMessage;
import org.airfactory.data.order.OrderDisableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/shelf")
public class DeliveryResource {

    private static final Logger LOG = LoggerFactory.getLogger(org.airfactory.DeliveryResource.class);

    private MessageProducer<JsonObject> topicCompleted;
    private MessageProducer<JsonObject> topicDisable;

    @Inject
    EntityManager entityManager;

    @Inject
    public void injectEventBus(EventBus eventBus) {
        topicCompleted = eventBus.<JsonObject>publisher(Constants.CLOUD_KITCHEN_ORDER_DELIVERY);
        topicDisable = eventBus.<JsonObject>publisher(Constants.CLOUD_KITCHEN_ORDER_DISABLE);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/new")
    @Transactional
    public void newShelf(ShelfItem item) {
        item.persist();
        LOG.info("Shelf [{}] created", item);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/completed")
    @Transactional
    public void orderCompleted(ShelfOrderItem item) {
        try {
            LOG.info("ShelfOrderItem [{}] completed", item);
            ShelfItem shelfItem = entityManager.find(ShelfItem.class, item.shelfId);
            shelfItem.removeOrderItem(item);
            item.isCompleted = true;
            BaseOrderItem baseOrderItem = new BaseOrderItem(item.name, item.temp, item.shelfLife, item.decayRate, item.date);
            baseOrderItem.setId(item.id);
            OrderCompletedMessage orderCompletedMessage = new OrderCompletedMessage(item.shelfId, baseOrderItem);
            topicCompleted.write(JsonObject.mapFrom(orderCompletedMessage));
        } catch (Exception e) {
            LOG.warn(e.getMessage());
        }
    }

    @ConsumeEvent(value = Constants.CLOUD_KITCHEN_ORDER_REDAY, local = true, blocking = true)
    @Transactional
    public void onShelf(Message<JsonObject> orderObject) {
        OrderCompletedMessage orderCompletedMessage = orderObject.body().mapTo(OrderCompletedMessage.class);
        BaseOrderItem baseOrderItem = orderCompletedMessage.getBaseOrderItem();
        Query query = entityManager.createQuery("FROM ShelfItem WHERE type =?1");
        query.setParameter(1, baseOrderItem.getTemp());
        List<ShelfItem> shelfItems = query.getResultList();
        Optional<ShelfItem> shelfItemOptional = shelfItems.stream().filter(shelfItem -> shelfItem.getShelfOrderItemList().size() < 15).findFirst();
        if (shelfItemOptional.isPresent()) {
            ShelfItem shelfItem = shelfItemOptional.get();
            ShelfOrderItem shelfOrderItem = new ShelfOrderItem();
            shelfOrderItem.shelfId = shelfItem.id;
            shelfOrderItem.date = baseOrderItem.getDate();
            shelfOrderItem.decayRate = baseOrderItem.getDecayRate();
            shelfOrderItem.name = baseOrderItem.getName();
            shelfOrderItem.temp = baseOrderItem.getTemp();
            shelfOrderItem.orderId = baseOrderItem.getId();

            shelfItem.addOrderItem(shelfOrderItem);
            shelfOrderItem.persist();
            shelfItem.persist();
        } else {
            Query queryETC = entityManager.createQuery("FROM ShelfItem WHERE type =?1");
            queryETC.setParameter(1, TempType.ETC);
            List<ShelfItem> shelfItemsETC = query.getResultList();
            Optional<ShelfItem> shelfItemOptionalETC = shelfItemsETC.stream().filter(shelfItem -> shelfItem.getShelfOrderItemList().size() < 15).findFirst();
            if (shelfItemOptionalETC.isPresent()) {
                ShelfItem shelfItem = shelfItemOptional.get();
                ShelfOrderItem shelfOrderItem = new ShelfOrderItem();
                shelfOrderItem.shelfId = shelfItem.id;
                shelfOrderItem.date = baseOrderItem.getDate();
                shelfOrderItem.decayRate = baseOrderItem.getDecayRate();
                shelfOrderItem.name = baseOrderItem.getName();
                shelfOrderItem.temp = baseOrderItem.getTemp();
                shelfOrderItem.orderId = baseOrderItem.getId();
                shelfItem.addOrderItem(shelfOrderItem);
                shelfOrderItem.persist();
                shelfItem.persist();
            } else {
                OrderDisableMessage orderDisableMessage = new OrderDisableMessage(orderCompletedMessage.getId(), baseOrderItem);
                topicDisable.write(JsonObject.mapFrom(orderDisableMessage));
            }
        }
    }
}
