package com.example.managinvoice.Helpers;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Supplier implements Serializable {

    String name;
    String contactName;
    String telephone;
    String address;
    String noteSupplier;
    float debt = 0;
    ArrayList<Bill> billsList;
    ArrayList<Payment> paymentsList;


    public Supplier() {
        billsList = new ArrayList<>();
        paymentsList = new ArrayList<>();
    }


    public Supplier(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNoteSupplier() {
        return noteSupplier;
    }

    public void setNoteSupplier(String noteSupplier) {
        this.noteSupplier = noteSupplier;
    }

    public float getDebt() {
        return debt;
    }

    public void setDebt(float debt) {
        this.debt = debt;
    }

    public ArrayList<Bill> getBillsList() {

        if (billsList == null)
            billsList = new ArrayList<>();


        return billsList;
    }

    public void setBillsList(ArrayList<Bill> billsList) {
        this.billsList = billsList;
    }

    public ArrayList<Payment> getPaymentsList() {

        if (paymentsList == null)
            paymentsList = new ArrayList<>();

        return paymentsList;
    }

    public void setPaymentsList(ArrayList<Payment> paymentsList) {
        this.paymentsList = paymentsList;
    }


    @Override
    public String toString() {
        return "Supplier{" +
                "name='" + name + '\'' +
                ", contactName='" + contactName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", address='" + address + '\'' +
                ", noteSupplier='" + noteSupplier + '\'' +
                ", debt=" + debt +
                '}';
    }



}
