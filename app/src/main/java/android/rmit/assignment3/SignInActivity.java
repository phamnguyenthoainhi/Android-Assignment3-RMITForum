package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    EditText email;
    EditText password;
    Button signin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailSignin);
        password = findViewById(R.id.passwordSignin);
        signin = findViewById(R.id.signin);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    signin(email.getText().toString(), password.getText().toString());
                }
            }
        });
    }

//    Check Input Validation
    public boolean isValid() {
        if (!email.getText().toString().contains("@rmit.edu.vn") || email.getText().toString().isEmpty()) {
            email.setError("Invalid Email");
            return false;
        } else if (password.getText().toString().length() < 5 || password.getText().toString().isEmpty()) {
            password.setError("Password should have more than 5 character");
            return false;
        }
        return true;
    }


    public void signin(final String inputemail, String inputpassword) {
        Log.d(TAG, "signin: hello");
        mAuth.signInWithEmailAndPassword(inputemail, inputpassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (task.getException().toString().contains("There is no user record corresponding to this identifier. The user may have been deleted")) {
                                email.setError("Email is not register");
                                email.clearComposingText();
                                email.requestFocus();
                            }
                            else if (task.getException().toString().contains(" The password is invalid or the user does not have a password")) {
                                password.setError("Password is invalid");
                                password.clearComposingText();
                                password.requestFocus();
                            }
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }


}
