package com.example.managinvoice.Helpers;

import java.io.Serializable;

public class Bill implements Serializable {

    String date;
    String noteBill;
    float amount = 0;
    String pictureBillUri;


    public Bill() {
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getPictureBillUri() {
        return pictureBillUri;
    }

    public void setPictureBillUri(String pictureBillUri) {
        this.pictureBillUri = pictureBillUri;
    }

    public String getNoteBill() {
        return noteBill;
    }

    public void setNoteBill(String noteBill) {
        this.noteBill = noteBill;
    }
}
