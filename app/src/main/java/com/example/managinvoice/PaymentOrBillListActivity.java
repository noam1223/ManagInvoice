package com.example.managinvoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managinvoice.Adapters.PaymentOrBillAdapter;
import com.example.managinvoice.Adapters.SupplierAdapter;
import com.example.managinvoice.Helpers.ShareFileListener;
import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PaymentOrBillListActivity extends AppCompatActivity implements ShareFileListener {

    TextView headLineTextView, totalAmountOfMoneyTextView;
    RecyclerView billPaymentRecyclerView;
    RecyclerView.Adapter billOrPaymentAdapter;

    Supplier supplier;
    boolean billOrPayment;

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback;
    DatabaseReference supplierDatabase;
    User user;
    String finishPathLine;
    int numInt = 0;

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    ArrayList<String> paymentKeys = new ArrayList<>();
    ArrayList<String> billKeys = new ArrayList<>();
    ArrayList<String> chosenKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_or_bill_list);


        headLineTextView = findViewById(R.id.head_line_text_view_payment_or_bill_list);
        totalAmountOfMoneyTextView = findViewById(R.id.total_sum_payment_or_bill_list);
        billPaymentRecyclerView = findViewById(R.id.payment_or_bill_recycler_view_list);

        headLineTextView.setText(getIntent().getStringExtra("head_line"));
        supplier = (Supplier) getIntent().getSerializableExtra("supplier");
        paymentKeys = getIntent().getStringArrayListExtra("paymentKeys");
        billKeys = getIntent().getStringArrayListExtra("billKeys");
        user = (User) getIntent().getSerializableExtra("user");

        supplierDatabase = FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers");

        billOrPayment = getIntent().getBooleanExtra("bill_payment", false); //bill

        updateTotalAmountTextView();

        if (isStoragePermissionGranted()) {
            initializePaymentOrBillRecyclerView(true);
        } else initializePaymentOrBillRecyclerView(false);


        simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                final int position = viewHolder.getAdapterPosition();
                String finishLine = "";
                finishPathLine = "";
                final String stringDelete;
                chosenKeys = null;


                if (billOrPayment) {
                    numInt = supplier.getPaymentsList().size();
                    finishLine = "צ׳ק";
                    finishPathLine = "paymentList";
                    chosenKeys = paymentKeys;

                    if (!supplier.getPaymentsList().get(position).isCashOrCheck())
                        stringDelete = supplier.getPaymentsList().get(position).getImagePaymentUri();
                    else stringDelete = null;

                } else {
                    numInt = supplier.getBillsList().size();
                    finishLine = "חשבונית";
                    finishPathLine = "billsList";
                    chosenKeys = billKeys;
                    stringDelete = supplier.getBillsList().get(position).getPictureBillUri();


                }


                new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setTitle("האם אתה בטוח שאתה רוצה למחוק " + finishLine + "?")
                        .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                                billOrPaymentAdapter.notifyDataSetChanged();


                            }
                        })

                        .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Log.i("NUMBER OF SWAP", chosenKeys.get(position));
                                Log.i("NUMBER OF SWAP", paymentKeys.get(position));
                                Log.i("NUMBER OF SWAP", position + "");

                                DatabaseReference deleteReference = supplierDatabase.child(supplier.getName()).child(finishPathLine).child(chosenKeys.get(position));
                                deleteReference.removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {


                                        if (error != null) {
                                            Log.i("ERROR DELETE", error.getMessage());
                                        }


                                        if (billOrPayment) {
                                            supplier.getPaymentsList().remove(position);
                                            paymentKeys.remove(position);
                                        } else {
                                            supplier.getBillsList().remove(position);
                                            billKeys.remove(position);
                                        }

                                        billOrPaymentAdapter.notifyDataSetChanged();
                                        updateTotalAmountTextView();

                                        if (stringDelete != null) {

                                            Log.i("STRING DELETE", stringDelete);

                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(stringDelete);
                                            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (!task.isSuccessful()) {
                                                        Log.i("TASK DELETE FILE", task.getException().getMessage());
                                                    }

                                                    Toast.makeText(PaymentOrBillListActivity.this, "DELETED", Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                        }

                                    }
                                });

                            }
                        }).show();

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(billPaymentRecyclerView);

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                initializePaymentOrBillRecyclerView(true);
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            initializePaymentOrBillRecyclerView(true);

        }
    }


    private void updateTotalAmountTextView() {
        if (billOrPayment) {
            totalAmountOfMoneyTextView.setText(String.valueOf(totalPaymentAmountOfMoney()));
        } else
            totalAmountOfMoneyTextView.setText(String.valueOf(totalBillAmountOfMoney()));
    }


    public float totalPaymentAmountOfMoney() {

        float totalSum = 0;

        for (int i = 0; i < supplier.getPaymentsList().size(); i++) {
            totalSum += supplier.getPaymentsList().get(i).getAmount();

        }

        return totalSum;

    }


    public float totalBillAmountOfMoney() {

        float totalSum = 0;

        for (int i = 0; i < supplier.getBillsList().size(); i++) {
            totalSum += supplier.getBillsList().get(i).getAmount();
        }

        return totalSum;

    }

    private void initializePaymentOrBillRecyclerView(boolean isGranted) {

        billPaymentRecyclerView.setHasFixedSize(false);
        billPaymentRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        billPaymentRecyclerView.setLayoutManager(layoutManager);
        billOrPaymentAdapter = new PaymentOrBillAdapter(getApplicationContext(), supplier, billOrPayment, this, isGranted, this);
        billPaymentRecyclerView.setAdapter(billOrPaymentAdapter);

    }

    @Override
    public void shareFileWhatsApp(String imgUri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imgUri);
        try {


            final File localFile = File.createTempFile("images", ".jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Intent shareIntent = new Intent();
                    Log.i("FILE PATH", localFile.getAbsolutePath());
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "TITLE");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", localFile));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "שיתוף תמונה"));

                }
            });
        } catch (IOException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.i("CREATE FILE EXCEPTION", e.getMessage());
        }


    }
}
