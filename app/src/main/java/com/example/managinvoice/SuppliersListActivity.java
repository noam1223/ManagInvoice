package com.example.managinvoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managinvoice.Adapters.SupplierAdapter;
import com.example.managinvoice.Helpers.Bill;
import com.example.managinvoice.Helpers.Payment;
import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SuppliersListActivity extends AppCompatActivity {

    RecyclerView suppliersRecyclerView;
    RecyclerView.Adapter supplierAdapter;
    TextView amountOfMoneyTextView;
    Button addSupplierBtn;

    ProgressBar progressBarLoadSuppliers;
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback;

    ArrayList<Supplier> suppliers = new ArrayList<>();

    DatabaseReference supplierDatabase;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers_list);

        progressBarLoadSuppliers = findViewById(R.id.progress_bar_suppliers_list_activity);
        progressBarLoadSuppliers.setVisibility(View.VISIBLE);
        suppliersRecyclerView = findViewById(R.id.supplier_recycler_view_suppliers_list_activity);
        amountOfMoneyTextView = findViewById(R.id.amount_sum_text_view_activity_suppliers_list);
        addSupplierBtn = findViewById(R.id.add_supplier_btn_suppliers_list_activity);

        user = (User) getIntent().getSerializableExtra("user");

        supplierDatabase = FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers");
        simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                final int position = viewHolder.getAdapterPosition();

                final Dialog dialog = new Dialog(viewHolder.itemView.getContext());
                View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_dialog, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                TextView headLine = dialog.findViewById(R.id.head_line_text_view_custom_dialog);
                TextView subLine = dialog.findViewById(R.id.sub_line_text_view_custom_dialog);

                final EditText passwordEditText = dialog.findViewById(R.id.password_edit_text_custom_dialog);

                Button yesBtn = dialog.findViewById(R.id.yes_delete_custom_dialog);
                Button noBtn = dialog.findViewById(R.id.no_delete_custom_dialog);


                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (user.getPassword().equals(passwordEditText.getText().toString())){

                            supplierDatabase.child(suppliers.get(position)
                                        .getName()).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                    if (error != null){
                                        Log.i("ERROR DELETE", error.getMessage());
                                    }

                                    supplierAdapter.notifyDataSetChanged();

                                }
                            });

                        } else {
                            Toast.makeText(SuppliersListActivity.this, "סיסמא שגויה", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialog.dismiss();
                        supplierAdapter.notifyDataSetChanged();

                    }
                });


                dialog.show();

            }
        };

        addSupplierBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), EditingProfileSupplierActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);

            }
        });


        supplierDatabase.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                suppliers.clear();

                for (DataSnapshot singleData :
                        snapshot.getChildren()) {


                    Supplier supplier = new Supplier();
                    String name = singleData.child("name").getValue().toString();
                    String contactName = "";
                    String telephone = "";
                    String address = "";
                    String noteSupplier = "";


                    if (singleData.child("contactName").getValue() != null){
                        contactName = singleData.child("contactName").getValue().toString();
                    }

                    if (singleData.child("telephone").getValue() != null) {
                        telephone = singleData.child("telephone").getValue().toString();
                    }

                    if (singleData.child("address").getValue() != null) {
                        address = singleData.child("address").getValue().toString();
                    }

                    if (singleData.child("noteSupplier").getValue() != null) {
                        noteSupplier = singleData.child("noteSupplier").getValue().toString();
                    }


                    float debt = Float.valueOf(singleData.child("debt").getValue().toString());

                    supplier.setName(name);

                    if (!contactName.isEmpty()) {
                        supplier.setContactName(contactName);
                    }


                    if (!telephone.isEmpty()) {
                        supplier.setTelephone(telephone);
                    }


                    if (!address.isEmpty()) {
                        supplier.setAddress(address);
                    }


                    if (!noteSupplier.isEmpty()) {
                        supplier.setNoteSupplier(noteSupplier);
                    }

                    supplier.setDebt(debt);


                    for (DataSnapshot data :
                            snapshot.child("paymentList").getChildren()) {

                        supplier.getPaymentsList().add(data.getValue(Payment.class));
                    }


                    for (DataSnapshot data :
                            snapshot.child("billsList").getChildren()) {

                        supplier.getBillsList().add(data.getValue(Bill.class));
                    }

                    suppliers.add(supplier);


                }

                initializeSuppliersRecyclerView();
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                itemTouchHelper.attachToRecyclerView(suppliersRecyclerView);
                progressBarLoadSuppliers.setVisibility(View.GONE);

                float amountOfDebt = 0;

                for (int i = 0; i < suppliers.size(); i++) {
                    amountOfDebt += suppliers.get(i).getDebt();
                }

                amountOfMoneyTextView.setText(String.format("%.2f", amountOfDebt));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.i("SUPPLIER CANCELED", error.getMessage());
                Toast.makeText(SuppliersListActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                suppliers.clear();
                initializeSuppliersRecyclerView();

            }

        });

    }


    private void initializeSuppliersRecyclerView() {

        suppliersRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        suppliersRecyclerView.setLayoutManager(layoutManager);
        supplierAdapter = new SupplierAdapter(this, suppliers, user);
        suppliersRecyclerView.setAdapter(supplierAdapter);

    }
}
