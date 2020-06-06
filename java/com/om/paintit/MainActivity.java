package com.om.paintit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    int RC_SIGN_IN = 234;
    String TAG = "Google sign In";
    GoogleSignInClient rGoogleSignInClient;
    SignInButton linkInButton;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference ref;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        button = findViewById(R.id.saveData);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            Log.i("Request","For permission Gps");
        } else {
            Activate();
        }


        // Save Data button doesn't do anything for now
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("clicked","Data Save Clicked");
                Toast.makeText(MainActivity.this, "Doesn't do anything", Toast.LENGTH_SHORT).show();

            }
        });



        //Changing the Ggoogle SignIn Button Text
        linkInButton = findViewById(R.id.linkIn);
        TextView textView = (TextView) linkInButton.getChildAt(0);
        textView.setText("Link Google Sign In");


        //Making Google singIn Option
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        rGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Linking button with Google SignIn (Log In)
        linkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Google","Sign in linking...");
                LinkIn();
            }
        });
    }


    //Log Out
    public void logOut(View view) {
        Log.i("Logged","out");
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
        finish();
    }


    // linking the account with Google Sign In
    public void LinkIn() {
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
                if (account.getEmail().equals(firebaseAuth.getCurrentUser().getEmail())) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Log.i(account.getEmail(), "Did not match with" + firebaseAuth.getCurrentUser().getEmail());
                    Toast.makeText(this, "Accounts did not match", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                // Google Link account failed, update UI appropriately
                Toast.makeText(this, "Linking Failed", Toast.LENGTH_LONG).show();
                Log.i("Error", "Linking failed");
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(MainActivity.this, "Linked the account", Toast.LENGTH_SHORT).show();
                            //  updateUI(user);
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "The account is already Linked", Toast.LENGTH_SHORT).show();
                            //  updateUI(null);
                        }
                        // ...
                    }
                });
    }

    //Making the broadcast receiver, which will resume with resuming the MainActivity
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Activate();
        }
    }

    // Activate Button by this finction
    public void Activate() {
        Button button = findViewById(R.id.activate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Starting the Gps_service in background
                Log.i("Start","service Gps in Background");
                Intent intent = new Intent(getApplicationContext(),Gps_service.class);
                startService(intent);
            }
        });
    }


    // Overriding the onDestroy method So it will not kill the Gps_service
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}