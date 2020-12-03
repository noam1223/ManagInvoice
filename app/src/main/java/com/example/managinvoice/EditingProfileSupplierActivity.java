package com.example.managinvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

public class EditingProfileSupplierActivity extends AppCompatActivity {


    EditText supplierNameEditText, contactNameEditText, telephoneEditText, addressEditText, noteEditText;
    Button finishFillFormBtn;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_profile_supplier_manager);


        supplierNameEditText = findViewById(R.id.supplier_name_edit_text_editing_profile_supplier);
        contactNameEditText = findViewById(R.id.contact_name_edit_text_editing_profile_supplier);
        telephoneEditText = findViewById(R.id.telephone_edit_text_editing_profile_supplier);
        addressEditText = findViewById(R.id.address_edit_text_editing_profile_supplier);
        noteEditText = findViewById(R.id.note_edit_text_editing_profile_supplier);

        user = (User) getIntent().getSerializableExtra("user");

        finishFillFormBtn = findViewById(R.id.finish_fulfill_form_btn_editing_profile_supplier);
        finishFillFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String supplierName = supplierNameEditText.getText().toString();
                String contactName = contactNameEditText.getText().toString();
                String telephone = telephoneEditText.getText().toString();
                String address = addressEditText.getText().toString();
                String note = noteEditText.getText().toString();


                if (!supplierName.isEmpty()){

                    Supplier supplier = new Supplier(supplierName);

                    if (!contactName.isEmpty()){
                        supplier.setContactName(contactName);
                    }

                    if (!telephone.isEmpty()){
                        supplier.setTelephone(telephone);
                    }

                    if (!address.isEmpty()){
                        supplier.setAddress(address);
                    }

                    if (!note.isEmpty()){
                        supplier.setNoteSupplier(note);
                    }


                    FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").child(supplierName).
                            setValue(supplier).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(EditingProfileSupplierActivity.this, "ספק נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    });

                }

            }
        });


    }
}
