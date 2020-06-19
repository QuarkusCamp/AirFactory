package org.airfactory;

import java.util.Date;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.airfactory.data.TempType;

@Entity
public class OrderItem extends PanacheEntity {

    public String name;
    public TempType temp;
    public Long shelfLife;
    public Long decayRate;
    public Long date;

    public boolean isDisable;
    public boolean isCompleted;
}
