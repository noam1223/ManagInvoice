package com.example.managinvoice.Helpers;

import java.io.Serializable;

public class Payment implements Serializable {

    String date;
    String notePayment;
    float amount = 0;
    boolean cashOrCheck;



    String imagePaymentUri;

    public Payment() {
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

    public boolean isCashOrCheck() {
        return cashOrCheck;
    }

    public void setCashOrCheck(boolean cashOrCheck) {
        this.cashOrCheck = cashOrCheck;
    }

    public String getImagePaymentUri() {
        return imagePaymentUri;
    }

    public void setImagePaymentUri(String imagePaymentUri) {
        this.imagePaymentUri = imagePaymentUri;
    }


    public String getNotePayment() {
        return notePayment;
    }

    public void setNotePayment(String notePayment) {
        this.notePayment = notePayment;
    }
}
