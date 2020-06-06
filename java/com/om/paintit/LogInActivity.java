package com.om.paintit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogInActivity extends AppCompatActivity {
    EditText rPassword,rEmail;
    Button rLogIn;
    TextView rRegister;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    int RC_SIGN_IN = 234;
    String TAG = "Google sign In";
    GoogleSignInClient rGoogleSignInClient;
    SignInButton logInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        rEmail = findViewById(R.id.Email);
        rPassword = findViewById(R.id.Password);
        rRegister = findViewById(R.id.Register);
        rLogIn = findViewById(R.id.LogInButton);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.ProgressBar);
        logInButton = findViewById(R.id.signIn);

        //Setting the Google Button Text to (LogIn)
        Log.i("Google button","Text from Sign In to Log In");
        TextView textView = (TextView) logInButton.getChildAt(0);
        textView.setText("Log In");

        //GoogleSignIn option for Google LogIn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        rGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                progressBar.setVisibility(View.VISIBLE);
            }
        });



        // LogIn Listener
        rLogIn.setOnClickListener(new View.OnClickListener() {
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


                //LogIn user to firebaseAccount with Email and Pasword
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // User successfully loged In
                            Toast.makeText(LogInActivity.this, "Successfully Loged In", Toast.LENGTH_LONG).show();
                            Log.i("LogedInwith",firebaseAuth.getCurrentUser().getEmail());
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                        else {
                            //Getting error while loged IN
                            Toast.makeText(LogInActivity.this, "Error!"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.i("Error","Password or Email");
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });


        //Register button setting up
        rRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Register button","set up successfully");
                //Getting back to Register Activity
                Log.i("Back","Getting back to Register Activity");
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });
    }


    //Google Sign In Functions for G button
    public void signIn() {
        Intent signInIntent = rGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show();
                Log.i("Google Sign In","Sign in failed");
                // ...
            }
        }
    }

    //Sign With Google Sign In credentials
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //    updateUI(user);
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LogInActivity.this, "Some thing wrong with Credentials", Toast.LENGTH_LONG).show();
                            Log.i("Wrong with credential","Can not logIn");
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
}