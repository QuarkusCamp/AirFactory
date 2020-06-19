package org.airfactory.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class ShelfItem extends PanacheEntity {

    public TempType type;

    @OneToMany
    public List<ShelfOrderItem> shelfOrderItemList = new ArrayList<>();

    public List<ShelfOrderItem> getShelfOrderItemList() {
        return shelfOrderItemList;
    }

    public void setShelfOrderItemList(List<ShelfOrderItem> shelfOrderItemList) {
        this.shelfOrderItemList = shelfOrderItemList;
    }

    public void addOrderItem(ShelfOrderItem item) {
        synchronized (shelfOrderItemList) {
            if (shelfOrderItemList.size() < 15) {
                shelfOrderItemList.add(item);
            } else {
                throw new org.airfactory.DeliveryFullException("The Shelf " + id + " is full");
            }
        }
    }

    public void removeOrderItem(ShelfOrderItem item) {
        synchronized (shelfOrderItemList) {
            shelfOrderItemList.removeIf(i -> i.equals(item));
        }
    }
}
