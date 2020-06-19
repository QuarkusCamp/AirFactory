package org.airfactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import org.airfactory.data.ShelfItem;
import org.airfactory.data.ShelfOrderItem;
import org.airfactory.data.TempType;
import org.airfactory.data.order.BaseOrderItem;
import org.airfactory.data.order.OrderDisableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DeliveryBean {

    private static final Logger LOG = LoggerFactory.getLogger(org.airfactory.DeliveryBean.class);

    @Inject
    EntityManager entityManager;

    private MessageProducer<JsonObject> topicDisable;

    @Inject
    public void injectEventBus(EventBus eventBus) {
        topicDisable = eventBus.<JsonObject>publisher(Constants.CLOUD_KITCHEN_ORDER_DISABLE);
    }

    @Scheduled(every = "30s")
    @Transactional
    void scanShelfForExpired() {
        Query query = entityManager.createQuery("FROM ShelfItem");
        List<ShelfItem> shelfItems = query.getResultList();
        shelfItems.forEach(shelfItem -> {
            shelfItem.getShelfOrderItemList().forEach(
                    shelfOrderItem -> {
                        if (checkValue(shelfOrderItem) == 0) {
                            shelfItem.removeOrderItem(shelfOrderItem);
                            BaseOrderItem baseOrderItem = new BaseOrderItem(shelfOrderItem.name, shelfOrderItem.temp, shelfOrderItem.shelfLife, shelfOrderItem.decayRate, shelfOrderItem.date);
                            baseOrderItem.setId(shelfOrderItem.orderId);
                            OrderDisableMessage orderDisableMessage = new OrderDisableMessage(shelfOrderItem.shelfId, baseOrderItem);
                            topicDisable.write(JsonObject.mapFrom(orderDisableMessage));
                        }
                    }
            );
        });
    }

    private Long checkValue(ShelfOrderItem item) {
        Date currentDate = new Date();
        Long age = currentDate.getTime() - item.date;
        return item.shelfLife - age - item.decayRate * age;
    }

    @Scheduled(every = "35s")
    @Transactional
    void scanShelfForReLoadShelf() {
        Query query = entityManager.createQuery("FROM ShelfItem where type !=?1");
        query.setParameter(1, TempType.ETC);
        List<ShelfItem> shelfItems = query.getResultList();

        Query queryETC = entityManager.createQuery("FROM ShelfItem where type !=?1");
        queryETC.setParameter(1, TempType.ETC);
        List<ShelfItem> shelfItemsETC = queryETC.getResultList();

        shelfItems.forEach(shelfItem -> {
            if (shelfItem.getShelfOrderItemList().size() < 15) {
                Optional<ShelfItem> optional = shelfItemsETC.stream().filter(shelfItemETC -> shelfItemETC.type.equals(shelfItem.type)).findFirst();
                if (optional.isPresent()) {
                    ShelfItem shelfItemETC = optional.get();
                    if (shelfItemETC.type.equals(shelfItem.type)) {
                        shelfItemETC.getShelfOrderItemList().forEach(shelfOrderItemETC -> {
                            if (shelfItem.getShelfOrderItemList().size() < 15) {
                                shelfItemETC.removeOrderItem(shelfOrderItemETC);
                                shelfItem.addOrderItem(shelfOrderItemETC);
                            }
                        });
                    }
                }
            }
        });
    }
}
