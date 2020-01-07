package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Validate extends AppCompatActivity {
    Button verify;
    Button skip;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate);
        verify = findViewById(R.id.validateemail);
        skip = findViewById(R.id.skipvalidate);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        verify.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          currentUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void aVoid) {
                                                  Toast.makeText(Validate.this, "Please check your email", Toast.LENGTH_SHORT).show();
                                                  startActivity(new Intent(Validate.this, CourseActivity.class));
                                              }
                                          });
                                      }
                                  });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( Validate.this,CourseActivity.class));
            }
        });
    }
}
