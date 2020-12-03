package com.example.managinvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstNavigationActivity extends AppCompatActivity {

    Button suppliersNavigationBtn, costumersNavigationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_navigation);


        suppliersNavigationBtn = findViewById(R.id.suppliers_btn_navigation_activity);
        costumersNavigationBtn = findViewById(R.id.costumers_btn_navigation_activity);

        suppliersNavigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SuppliersListActivity.class);
                intent.putExtra("user", getIntent().getSerializableExtra("user"));
                startActivity(intent);

            }
        });


        costumersNavigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }


}
