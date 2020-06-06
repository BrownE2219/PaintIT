package com.om.paintit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    EditText rFullName,rPhone,rPassword,rEmail;
    Button rRegister;
    TextView rLogIn;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        rFullName = findViewById(R.id.FullName);
        rEmail = findViewById(R.id.Email);
        rPhone = findViewById(R.id.Phone);
        rPassword = findViewById(R.id.Password);
        rRegister = findViewById(R.id.RegisterButton);
        rLogIn = findViewById(R.id.Register);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.ProgressBar);
        if(firebaseAuth.getCurrentUser() != null) {
            //Checking if the user already logedIn
            Log.i("User","already loged In");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        rRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = rEmail.getText().toString().trim();
                String password = rPassword.getText().toString().trim();
                if(TextUtils.isEmpty(email)) {
                    rEmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    rPassword.setError("Password is Required");
                    return;
                }
                if(password.length()<8) {
                    rPassword.setError("Password length must be >= 8");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                //register user to firebase
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Signed successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Error!"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        rLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LogInActivity.class));
                finish();
            }
        });
    }
}