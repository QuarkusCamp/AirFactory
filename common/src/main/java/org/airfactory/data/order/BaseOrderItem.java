package org.airfactory.data.order;

import org.airfactory.data.TempType;

public class BaseOrderItem {

    Long id;
    String name;
    TempType temp;
    Long shelfLife;
    Long decayRate;
    Long date;

    public BaseOrderItem(){

    }

    public BaseOrderItem(String name, TempType temp, Long shelfLife, Long decayRate, Long date) {
        this.name = name;
        this.temp = temp;
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TempType getTemp() {
        return temp;
    }

    public void setTemp(TempType temp) {
        this.temp = temp;
    }

    public Long getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(Long shelfLife) {
        this.shelfLife = shelfLife;
    }

    public Long getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(Long decayRate) {
        this.decayRate = decayRate;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
