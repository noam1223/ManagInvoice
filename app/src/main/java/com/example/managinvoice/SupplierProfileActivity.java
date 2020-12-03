package com.example.managinvoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managinvoice.Helpers.Bill;
import com.example.managinvoice.Helpers.Payment;
import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierProfileActivity extends AppCompatActivity implements View.OnClickListener {


    TextView supplierNameTextView, supplierDetailsTextView, debtAmountTextView;
    Button addBillBtn, addPaymentBtn, billListBtn, paymentListBtn;

    Supplier supplier;

    RelativeLayout totalRelativeLayout;
    ProgressBar progressBarProfile;
    User user;


    ArrayList<String> paymentKeys = new ArrayList<>();
    ArrayList<String> billKeys = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_profile);


        supplierNameTextView = findViewById(R.id.supplier_name_text_view_supplier_profile);
        supplierDetailsTextView = findViewById(R.id.note_supplier_text_view_supplier_profile);
        debtAmountTextView = findViewById(R.id.debt_amount_text_view_supplier_profile);

        addBillBtn = findViewById(R.id.add_bill_task_btn_supplier_profile);
        addPaymentBtn = findViewById(R.id.add_payment_task_btn_supplier_profile);
        billListBtn = findViewById(R.id.go_to_bill_list_activity_btn_supplier_profile);
        paymentListBtn = findViewById(R.id.go_to_payment_list_activity_btn_supplier_profile);

        totalRelativeLayout = findViewById(R.id.total_layout_supplier_profile_activity);
        progressBarProfile = findViewById(R.id.progress_bar_supplier_profile_activity);

        user = (User) getIntent().getSerializableExtra("user");

        addBillBtn.setOnClickListener(this);
        addPaymentBtn.setOnClickListener(this);
        billListBtn.setOnClickListener(this);
        paymentListBtn.setOnClickListener(this);


        supplier = (Supplier) getIntent().getSerializableExtra("supplier");

        if (supplier != null) {

            FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").child(supplier.getName()).addValueEventListener(new ValueEventListener() {



                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        Log.i("SNAPSHOT PROFILE", supplier.toString());
                        Supplier supplierClone = new Supplier();
                        String name = snapshot.child("name").getValue().toString();
                        String contactName = "";
                        String telephone = "";
                        String address = "";
                        String noteSupplier = "";


                        if (snapshot.child("contactName").getValue() != null){
                            contactName = snapshot.child("contactName").getValue().toString();
                        }

                        if (snapshot.child("telephone").getValue() != null) {
                            telephone = snapshot.child("telephone").getValue().toString();
                        }

                        if (snapshot.child("address").getValue() != null) {
                            address = snapshot.child("address").getValue().toString();
                        }

                        if (snapshot.child("noteSupplier").getValue() != null) {
                            noteSupplier = snapshot.child("noteSupplier").getValue().toString();
                        }


                        float debt = Float.valueOf(snapshot.child("debt").getValue().toString());

                        supplierClone.setName(name);

                        if (!contactName.isEmpty()) {
                            supplierClone.setContactName(contactName);
                        }


                        if (!telephone.isEmpty()) {
                            supplierClone.setTelephone(telephone);
                        }


                        if (!address.isEmpty()) {
                            supplierClone.setAddress(address);
                        }


                        if (!noteSupplier.isEmpty()) {
                            supplierClone.setNoteSupplier(noteSupplier);
                        }

                        supplierClone.setDebt(debt);

                        supplier = supplierClone;

                        billKeys = new ArrayList<>();
                        paymentKeys = new ArrayList<>();

                        for (DataSnapshot data :
                                snapshot.child("paymentList").getChildren()) {

                            paymentKeys.add(data.getKey());

                            supplier.getPaymentsList().add(data.getValue(Payment.class));
                        }


                        for (DataSnapshot data :
                                snapshot.child("billsList").getChildren()) {

                            billKeys.add(data.getKey());

                            supplier.getBillsList().add(data.getValue(Bill.class));
                        }

                        Log.i("SNAPSHOT PROFILE", paymentKeys.size() + "");
                        Log.i("SNAPSHOT PROFILE", billKeys.size() + "");

                        if (supplier != null)
                            updateUi(supplier);

                    }



                    FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").removeEventListener(this);

                }



                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Log.i("SUPPLIER CANCELED", error.getMessage());
                    Toast.makeText(SupplierProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                }

            });


        }
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }




    private void updateUi(Supplier supplier) {


        String contactName = supplier.getContactName();
        String address = supplier.getAddress();
        String telephone = supplier.getTelephone();
        String noteSupplier = supplier.getNoteSupplier();
        float debt = supplier.getDebt();

        progressBarProfile.setVisibility(View.GONE);
        totalRelativeLayout.setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder();

        debtAmountTextView.setText(String.valueOf(debt));
        supplierNameTextView.setText(supplier.getName());

        float sum = totalPaymentAmountOfMoney() - totalBillAmountOfMoney();
        debtAmountTextView.setText(String.format("%.2f" , sum));


        if (contactName != null && !contactName.isEmpty()){

            stringBuilder.append("איש קשר: " + contactName);

        }


        if (address != null && !address.isEmpty()){

            stringBuilder.append("\n" + "כתובת: " + address);

        }

        if (telephone != null && !telephone.isEmpty()){

            stringBuilder.append("\n" + "טלפון: " + telephone);

        }

        if (noteSupplier != null && !noteSupplier.isEmpty()){

            stringBuilder.append("\n" + "מידע נוסף: " + noteSupplier);

        }


        supplierDetailsTextView.setText(stringBuilder);
        Map map = new HashMap();
        map.put("debt", sum);
        FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").child(supplier.getName()).updateChildren(map);

    }




    public float totalPaymentAmountOfMoney(){

        float totalSum = 0;

        for (int i = 0; i < supplier.getPaymentsList().size(); i++) {
            totalSum += supplier.getPaymentsList().get(i).getAmount();

        }

        return totalSum;

    }



    public float totalBillAmountOfMoney(){

        float totalSum = 0;

        for (int i = 0; i < supplier.getBillsList().size(); i++) {
            totalSum += supplier.getBillsList().get(i).getAmount();
        }

        return totalSum;

    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent();
        String headLine = "";

        //false = bill
        //true = payment

        switch (view.getId()){


            case R.id.add_bill_task_btn_supplier_profile:

                intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                intent.putExtra("bill_payment", false);
                headLine = "הוספת חשבונית";

                break;


            case R.id.add_payment_task_btn_supplier_profile:

                intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                intent.putExtra("bill_payment", true);
                headLine = "הוספת תשלום";

                break;



            case R.id.go_to_bill_list_activity_btn_supplier_profile:

                intent = new Intent(getApplicationContext(), PaymentOrBillListActivity.class);
                intent.putExtra("bill_payment", false);
                headLine = "כל החשבוניות";

                break;


            case R.id.go_to_payment_list_activity_btn_supplier_profile:

                intent = new Intent(getApplicationContext(), PaymentOrBillListActivity.class);
                intent.putExtra("bill_payment", true);
                headLine = "כל התשלומים";

                break;



        }

        intent.putExtra("supplier", supplier);
        intent.putExtra("user", user);
        intent.putExtra("head_line", headLine);
        intent.putStringArrayListExtra("billKeys", billKeys);
        intent.putStringArrayListExtra("paymentKeys", paymentKeys);
        startActivity(intent);

    }






}
