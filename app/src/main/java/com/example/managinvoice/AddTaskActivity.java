package com.example.managinvoice;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managinvoice.Helpers.Bill;
import com.example.managinvoice.Helpers.DecimalDigitsInputFilter;
import com.example.managinvoice.Helpers.Payment;
import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "WRITE_EXTERNAL";
    private static final String CAMERA = "OPEN_CAMERA";
    TextView headLineTextView, dateTextView, scanPicTextView;
    LinearLayout takePicFieldLinearLayout;
    RadioGroup optionsOfPaymentRadioGroup;
    ProgressBar savingImageProgressBar;
    ImageButton button;
    RelativeLayout relativeLayout;
    AlertDialog dialog;
    ViewGroup.LayoutParams layoutParams;


    //Radio Button

    EditText amountOfMoneyEditText, noteFieldEditText;

    Button saveBtn;

    ImageButton scanPaymentBillBtn;
    ImageView paymentOrBillImageView;


    Supplier supplier;
    boolean billOrPayment, cashOrCheck = true;
    Uri imageUri;

    Intent intent;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        intent = getIntent();
        billOrPayment = intent.getBooleanExtra("bill_payment", false);
        user = (User) intent.getSerializableExtra("user");

        storageReference = firebaseStorage.getReference(user.getUsername());

        supplier = (Supplier) intent.getSerializableExtra("supplier");

        headLineTextView = findViewById(R.id.head_line_add_task_activity_text_view);
        dateTextView = findViewById(R.id.date_of_bill_payment_text_view_add_task_activity);
        scanPicTextView = findViewById(R.id.scan_payment_bill_text_view_add_task_acitivity);
        savingImageProgressBar = findViewById(R.id.progress_bar_save_image_add_task_activity);

        paymentOrBillImageView = findViewById(R.id.image_view_payment_or_bill_add_task_activity);
        layoutParams = paymentOrBillImageView.getLayoutParams();
        paymentOrBillImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                if(isImageFitToScreen) {
//                    isImageFitToScreen=false;
//                    paymentOrBillImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//                    paymentOrBillImageView.setAdjustViewBounds(true);
//                }else{
//                    isImageFitToScreen=true;
//                    paymentOrBillImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 400));
//                    paymentOrBillImageView.setScaleType(ImageView.ScaleType.FIT_XY);
//                }
            }
        });

        amountOfMoneyEditText = findViewById(R.id.amount_of_money_edit_text_add_task_activity);
        amountOfMoneyEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(10, 2)});
        noteFieldEditText = findViewById(R.id.note_bill_edit_text_add_task_activity);

        saveBtn = findViewById(R.id.save_payment_bill_btn_add_task_activity);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (billOrPayment) {

                    //payment

                    final Payment payment = new Payment();


                    payment.setDate(dateTextView.getText().toString());

                    if (!amountOfMoneyEditText.getText().toString().isEmpty()) {
                        payment.setAmount(Float.valueOf(amountOfMoneyEditText.getText().toString()));
                    }else {
                        Toast.makeText(AddTaskActivity.this, "אנא מלא את שדה ׳סכום׳", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!noteFieldEditText.getText().toString().isEmpty()) {
                        payment.setNotePayment(noteFieldEditText.getText().toString());
                    }

                    payment.setCashOrCheck(cashOrCheck);

                    if (imageUri != null) {
                        savingImageProgressBar.setVisibility(View.VISIBLE);

                        storageReference.child(supplier.getName() + "/MediaPayment/" + System.currentTimeMillis() + "/").putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    payment.setImagePaymentUri(uri.toString());

                                                    addPaymentToDatabase(payment);
                                                }
                                            });

                                        } else if (task.isCanceled()) {
                                            Log.i("TASK CANCELED", task.getException().getMessage());
                                        }

                                    }
                                });


                    } else if (!cashOrCheck){

                        Toast.makeText(AddTaskActivity.this, "חובה לעלות תמונה", Toast.LENGTH_SHORT).show();

                    } else {

                        addPaymentToDatabase(payment);
                    }

                } else {

                    //bill

                    final Bill bill = new Bill();


                    bill.setDate(dateTextView.getText().toString());

                    if (!amountOfMoneyEditText.getText().toString().isEmpty()) {
                        bill.setAmount(Float.valueOf(amountOfMoneyEditText.getText().toString()));
                    }else {
                        Toast.makeText(AddTaskActivity.this, "אנא מלא את שדה ׳סכום׳", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!noteFieldEditText.getText().toString().isEmpty()) {
                        bill.setNoteBill(noteFieldEditText.getText().toString());
                    }

                    if (imageUri != null) {

                        savingImageProgressBar.setVisibility(View.VISIBLE);
                        storageReference.child(supplier.getName() + "/MediaBills/" + System.currentTimeMillis() + ".jpg").putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    bill.setPictureBillUri(uri.toString());

//                                                    FirebaseDatabase.getInstance().getReference("Suppliers").child(supplier.getName()).child("billsList").child()

                                                    addBillToDatabase(bill);
                                                }
                                            });

                                        } else if (task.isCanceled()) {
                                            Log.i("TASK CANCELED", task.getException().getMessage());
                                        }

                                    }
                                });

                    } else {

                        Toast.makeText(AddTaskActivity.this, "חובה לעלות תמונה", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        scanPaymentBillBtn = findViewById(R.id.take_pic_bill_image_btn_add_task_activity);
        scanPaymentBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (isStoragePermissionGranted() && isCameraPermissionGranted()) {

                    selectImage();

                }

            }
        });


        takePicFieldLinearLayout = findViewById(R.id.linear_layout_scan_payment_bill_edit_text_add_task_acitivity);


        optionsOfPaymentRadioGroup = findViewById(R.id.radio_group_options_payment_add_task_activity);


        optionsOfPaymentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amountOfMoneyEditText.getLayoutParams();


                if (rb != null) {

                    switch (rb.getId()) {

                        case R.id.radio_btn_check_add_task_activity:

                            takePicFieldLinearLayout.setVisibility(View.VISIBLE);

                            params.addRule(RelativeLayout.BELOW, takePicFieldLinearLayout.getId());

                            cashOrCheck = false;


                            break;


                        case R.id.radio_btn_cash_add_task_activity:

                            takePicFieldLinearLayout.setVisibility(View.INVISIBLE);

                            params.addRule(RelativeLayout.BELOW, optionsOfPaymentRadioGroup.getId());

                            cashOrCheck = true;

                            break;


                    }

                    amountOfMoneyEditText.setLayoutParams(params);


                }
            }
        });


        if (billOrPayment) {

            //payment

            takePicFieldLinearLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amountOfMoneyEditText.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, optionsOfPaymentRadioGroup.getId());
            scanPicTextView.setText("סרוק צ׳ק");

        } else {

            //bill

            optionsOfPaymentRadioGroup.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) takePicFieldLinearLayout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, headLineTextView.getId());


        }


        headLineTextView.setText(intent.getStringExtra("head_line"));

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        dateTextView.setText(currentDate);


    }

    private void addPaymentToDatabase(Payment payment) {
        savingImageProgressBar.setVisibility(View.GONE);
        supplier.getPaymentsList().add(payment);
//        Map map = new HashMap();
//        map.put("paymentsList", supplier.getPaymentsList());
        FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").child(supplier.getName()).child("paymentList").push().setValue(payment);
        finish();
    }

    private void addBillToDatabase(Bill bill) {
        savingImageProgressBar.setVisibility(View.GONE);
        supplier.getBillsList().add(bill);
//        Map map = new HashMap();
//        map.put("billsList", supplier.getBillsList());
        FirebaseDatabase.getInstance().getReference(user.getUsername()).child("Suppliers").child(supplier.getName()).child("billsList").push().setValue(bill);
        finish();
    }


    private void selectImage() {
        try {

            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"מצלמה", "גלרייה", "ביטול"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בחר אפשרות");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("מצלמה")) {
                            dialog.dismiss();

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "תמונה חדשה");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "תמונה התחלה");
                            imageUri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                            mStartForResult.launch(intent);

                        } else if (options[item].equals("גלרייה")) {

                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            mStartForResult.launch(pickPhoto);

                        } else if (options[item].equals("ביטול")) {
                            dialog.dismiss();
                        }
                    }
                });

                builder.show();

            } else
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    public boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.v(CAMERA, "Permission is granted");
                return true;
            } else {
                Log.v(CAMERA, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
                return false;
            }
        } else {
            Log.v(CAMERA, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);

            if (imageUri == null && isCameraPermissionGranted())
                selectImage();
        }

        if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);

            if (imageUri == null)
                selectImage();
        }
    }


    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {


                        if (imageUri == null) {
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(paymentOrBillImageView);
                        } else Picasso.get().load(imageUri).into(paymentOrBillImageView);


                        paymentOrBillImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                AlertDialog.Builder builder = new AlertDialog.Builder(AddTaskActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                                LayoutInflater inflater = AddTaskActivity.this.getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.custom_fullimage_dialog, null);
                                builder.setView(dialogView);
                                dialog = builder.create();




                                ImageView imageView = dialogView.findViewById(R.id.full_image_view_alert_dialog);
                                TextView textView = dialogView.findViewById(R.id.custom_full_image_place_name_alert_dialog);
                                relativeLayout = dialogView.findViewById(R.id.area_back_arrow_alert_dialog);
                                button = dialogView.findViewById(R.id.back_btn_alert_dialog);
                                setListenerCarefully();
                                relativeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        setListenerCarefully();
                                    }
                                });

                                Picasso.get().load(imageUri).into(imageView);


                                if (!noteFieldEditText.getText().toString().isEmpty()) {

                                    textView.setText(noteFieldEditText.getText().toString());

                                }


                                dialogView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        setListenerCarefully();
                                    }
                                });



                                dialog.show();

                            }
                        });
                    }
                }
            });



    private void setListenerCarefully() {
        if (relativeLayout.getAlpha() == 1.0f) {

            relativeLayout.animate().alpha(0.0f);
            button.setOnClickListener(null);

        } else {

            relativeLayout.animate().alpha(1.0f);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.dismiss();
                    paymentOrBillImageView.setLayoutParams(layoutParams);

                }
            });
        }
    }
}