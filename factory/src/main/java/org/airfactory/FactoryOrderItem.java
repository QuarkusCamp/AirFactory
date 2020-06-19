package org.airfactory;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.airfactory.data.TempType;
import org.airfactory.data.order.BaseOrderItem;

@Entity
public class FactoryOrderItem extends PanacheEntity {

    boolean readyForDelivery;

    String name;
    TempType temp;
    Long shelfLife;
    Long decayRate;
    Long date;
    Long orderId;

}
