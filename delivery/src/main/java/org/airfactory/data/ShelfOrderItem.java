package org.airfactory.data;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class ShelfOrderItem extends PanacheEntity {

    public Long shelfId;
    public boolean isCompleted;
    public String name;
    public TempType temp;
    public Long shelfLife;
    public Long decayRate;
    public Long date;
    public Long orderId;
}
