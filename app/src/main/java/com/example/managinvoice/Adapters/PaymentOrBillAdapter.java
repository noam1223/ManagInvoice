package com.example.managinvoice.Adapters;


import android.app.AlertDialog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managinvoice.BuildConfig;
import com.example.managinvoice.Helpers.ShareFileListener;
import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.PaymentOrBillListActivity;
import com.example.managinvoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


public class PaymentOrBillAdapter extends RecyclerView.Adapter<PaymentOrBillAdapter.PaymentOrBillAdapterViewHolder> {


    Context context;
    Supplier supplier;
    boolean billOrPayment, isGranted = true;
    PaymentOrBillListActivity activity;
    AlertDialog dialog;
    RelativeLayout relativeLayout;
    ImageButton button;
    ShareFileListener shareFileListener;


    public PaymentOrBillAdapter(Context context, Supplier supplier, boolean billOrPayment, PaymentOrBillListActivity activity, boolean isGranted, ShareFileListener shareFileListener) {
        this.context = context;
        this.supplier = supplier;
        this.billOrPayment = billOrPayment;
        this.activity = activity;
        this.isGranted = isGranted;
        this.shareFileListener = shareFileListener;
    }


    @NonNull
    @Override
    public PaymentOrBillAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        return new PaymentOrBillAdapterViewHolder(layoutView);
    }


    @Override
    public void onBindViewHolder(@NonNull final PaymentOrBillAdapterViewHolder holder, int position) {

        if (billOrPayment) {

            holder.dateTextView.setText(supplier.getPaymentsList().get(holder.getAdapterPosition()).getDate());
            holder.amountOfMoneyTextView.setText(String.valueOf(supplier.getPaymentsList().get(holder.getAdapterPosition()).getAmount()));

            if (supplier.getPaymentsList().get(holder.getAdapterPosition()).getImagePaymentUri() != null) {
                holder.imageViewBill.setVisibility(View.VISIBLE);
                Picasso.get().load(Uri.parse(supplier.getPaymentsList().get(holder.getAdapterPosition()).getImagePaymentUri())).into(holder.imageViewBill);
            }

            if (supplier.getPaymentsList().get(holder.getAdapterPosition()).getNotePayment() != null) {
                holder.noteTextView.setVisibility(View.VISIBLE);
                holder.noteTextView.setText(supplier.getPaymentsList().get(holder.getAdapterPosition()).getNotePayment());
            }

            holder.cashOrCheckTextView.setVisibility(View.VISIBLE);
            if (supplier.getPaymentsList().get(holder.getAdapterPosition()).isCashOrCheck()) {
                holder.cashOrCheckTextView.setText("מזומן");
            } else holder.cashOrCheckTextView.setText("צ׳ק");


            if (isGranted) {
                holder.shareImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        shareFileListener.shareFileWhatsApp(supplier.getPaymentsList().get(holder.getAdapterPosition()).getImagePaymentUri());

                    }
                });
            }

        } else {

            holder.dateTextView.setText(supplier.getBillsList().get(holder.getAdapterPosition()).getDate());
            holder.amountOfMoneyTextView.setText(String.valueOf(supplier.getBillsList().get(holder.getAdapterPosition()).getAmount()));

            if (supplier.getBillsList().get(holder.getAdapterPosition()).getPictureBillUri() != null) {
                holder.imageViewBill.setVisibility(View.VISIBLE);
                Picasso.get().load(Uri.parse(supplier.getBillsList().get(holder.getAdapterPosition()).getPictureBillUri())).into(holder.imageViewBill);

            }

            if (supplier.getBillsList().get(holder.getAdapterPosition()).getNoteBill() != null) {
                holder.noteTextView.setVisibility(View.VISIBLE);
                holder.noteTextView.setText(supplier.getBillsList().get(holder.getAdapterPosition()).getNoteBill());
            }

            holder.cashOrCheckTextView.setVisibility(View.GONE);

            if (isGranted) {
                holder.shareImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        shareFileListener.shareFileWhatsApp(supplier.getBillsList().get(holder.getAdapterPosition()).getPictureBillUri());
                    }
                });
            }
        }


        holder.imageViewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);


                LayoutInflater inflater = activity.getLayoutInflater();
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

                imageView.setImageDrawable(holder.imageViewBill.getDrawable());


                if (!holder.noteTextView.getText().toString().isEmpty()) {

                    textView.setText(holder.noteTextView.getText().toString());

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

                }
            });
        }
    }


    @Override
    public int getItemCount() {

        if (billOrPayment) {
            return supplier.getPaymentsList().size();
        }

        return supplier.getBillsList().size();
    }


    class PaymentOrBillAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView, amountOfMoneyTextView, cashOrCheckTextView, noteTextView;
        ImageView imageViewBill;
        ImageButton shareImageBtn;

        public PaymentOrBillAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.payment_date_text_view_payment_item);
            amountOfMoneyTextView = itemView.findViewById(R.id.payment_amount_text_view_payment_item);
            cashOrCheckTextView = itemView.findViewById(R.id.check_or_cash_text_view_payment_item);
            noteTextView = itemView.findViewById(R.id.note_text_view_payment_item);

            imageViewBill = itemView.findViewById(R.id.payment_image_view_payment_item);
            shareImageBtn = itemView.findViewById(R.id.share_btn_payment_item);

        }


    }
}
