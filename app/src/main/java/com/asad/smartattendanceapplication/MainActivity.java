package com.asad.smartattendanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        //createAccount("nasimkhankhilji39774@gmail.com", "123456789");
        SystemClock.sleep(3000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser  = mAuth.getCurrentUser();

        if(currentUser == null){
            Intent registerIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(registerIntent);
            finish();
        }else{
            Intent loginIntent = new Intent(MainActivity.this, FaceRecognitionActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    private void createAccount(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication Succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
