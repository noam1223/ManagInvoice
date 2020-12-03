package com.example.managinvoice.LoginScreens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managinvoice.FirstNavigationActivity;
import com.example.managinvoice.Helpers.User;
import com.example.managinvoice.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText userNameEditText, passwordEditText;
    TextView navigateToRegisterTextView;
    Button loginBtn;

    DatabaseReference ref;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEditText = findViewById(R.id.user_name_edit_text_login_activity);
        passwordEditText = findViewById(R.id.password_edit_text_login_activity);
        ref = FirebaseDatabase.getInstance().getReference().child("Users");

        navigateToRegisterTextView = findViewById(R.id.navigate_to_register_screen_text_view_login_activity);
        navigateToRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();


                if (userName.length() < 3 || password.length() < 3){

                    Toast.makeText(LoginActivity.this, "יש להזין לפחות 4 תווים בכל אחד מהתיבות", Toast.LENGTH_SHORT).show();
                    return;
                }


                user = new User(userName, password);

                ref.orderByChild("username").equalTo(user.getUsername()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){

                            Log.i("SNAP", snapshot.getKey());

                            for (DataSnapshot data :
                                    snapshot.getChildren()) {

                                User currentUser = data.getValue(User.class);

                                if (currentUser.getUsername().equals(user.getUsername())){


                                    Toast.makeText(LoginActivity.this, "קיים משתמש בשם זה", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            }


                        } else {

                            ref.push().setValue(user);
                            ref.removeEventListener(this);
                            goToFirstNavigation();
                        }

                        ref.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.i("DATABASE USER LOGIN", error.getMessage());

                    }
                });

            }
        });









        loginBtn = findViewById(R.id.login_btn_login_activity);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();


                if (userName.length() < 3 || password.length() < 3){

                    Toast.makeText(LoginActivity.this, "יש להזין לפחות 4 תווים בכל אחד מהתיבות", Toast.LENGTH_SHORT).show();
                    return;
                }

                user = new User(userName, password);

                ref.orderByChild("username").equalTo(user.getUsername()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){


                            for (DataSnapshot data :
                                    snapshot.getChildren()) {

                                User currentUser = data.getValue(User.class);

                                if (currentUser.getUsername().equals(user.getUsername())){

                                    if (user.getPassword().equals(currentUser.getPassword())){

                                        goToFirstNavigation();
                                        return;

                                    } else {

                                        Toast.makeText(LoginActivity.this, "סיסמא לא נכונה נסה שוב", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                Toast.makeText(LoginActivity.this, "לא קיים משתמש בשם זה", Toast.LENGTH_SHORT).show();
                                return;

                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "לא קיים משתמש בשם זה", Toast.LENGTH_SHORT).show();
                        }

                        ref.removeEventListener(this);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.i("DATABASE USER LOGIN", error.getMessage());

                    }
                });


            }
        });






    }

    private void goToFirstNavigation() {
        Intent intent = new Intent(LoginActivity.this, FirstNavigationActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }
}
