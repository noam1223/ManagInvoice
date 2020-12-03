package com.example.managinvoice.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managinvoice.Helpers.Supplier;
import com.example.managinvoice.Helpers.User;
import com.example.managinvoice.R;
import com.example.managinvoice.SupplierProfileActivity;

import java.util.ArrayList;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>{


    Context context;
    ArrayList<Supplier> suppliers = new ArrayList<>();
    User user;


    public SupplierAdapter(Context context, ArrayList<Supplier> suppliers, User user) {
        this.context = context;
        this.suppliers = suppliers;
        this.user = user;
    }



    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.supplier_name_item, null, false);

        return new SupplierViewHolder(layoutView);
    }




    @Override
    public void onBindViewHolder(@NonNull final SupplierViewHolder holder, int position) {

        holder.supplierTextView.setText(suppliers.get(holder.getAdapterPosition()).getName());

        holder.supplierLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, SupplierProfileActivity.class);
                intent.putExtra("supplier", suppliers.get(holder.getAdapterPosition()));
                intent.putExtra("user", user);

                context.startActivity(intent);



            }
        });

    }





    @Override
    public int getItemCount() {
        return suppliers.size();
    }





    class SupplierViewHolder extends RecyclerView.ViewHolder{

        LinearLayout supplierLinearLayout;
        TextView supplierTextView;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);

            supplierLinearLayout = itemView.findViewById(R.id.linear_layout_supplier_item);
            supplierTextView = itemView.findViewById(R.id.supplier_name_text_view_item);

        }


    }
}
