package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.icu.util.ULocale;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    EditText email;
    EditText password;
    EditText fullname;
    Button signup;
    Button show;
    Button hide;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    User currentUser;
    Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        email = findViewById(R.id.emailSignup);
        show = findViewById(R.id.showpasswordsignup);
        hide = findViewById(R.id.hidepasswordsignup);
        show.setVisibility(View.INVISIBLE);
        password = findViewById(R.id.passwordSignup);
        fullname = findViewById(R.id.fullname);
        signup = findViewById(R.id.signup);
        utilities = new Utilities();

        TextView signinfromsignup = findViewById(R.id.signinfromsignup);
        signinfromsignup.setClickable(true);
        signinfromsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide.setVisibility(View.INVISIBLE);
                show.setVisibility(View.VISIBLE);
                password.setInputType(InputType.TYPE_CLASS_TEXT);

            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                show.setVisibility(View.INVISIBLE);
                hide.setVisibility(View.VISIBLE);

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    signup(email.getText().toString(), password.getText().toString(), fullname.getText().toString());
                }
            }
        });
    }


    //    Check Input Validation
    public boolean isValid() {
        if (!email.getText().toString().contains("@rmit.edu.vn") || email.getText().toString().isEmpty()) {
            email.setError("Invalid Email");
            email.requestFocus();
            return false;
        } else if (password.getText().toString().length() < 5 || password.getText().toString().isEmpty()) {
            password.setError("Password should have more than 5 character");
            password.requestFocus();
            return false;
        } else if (fullname.getText().toString().isEmpty()) {
            fullname.setError("Invalid Full Name");
            fullname.requestFocus();
        }
        return true;
    }

    public void signup(final String inputemail, String inputpassword, final String inputfullname) {
        mAuth.createUserWithEmailAndPassword(inputemail, inputpassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
//                            SetUp user
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(inputfullname)

                                    .build();

                            user.updateProfile(profileUpdates);
                            currentUser = new User(user.getUid(), inputfullname, inputemail);

                            SumVote sumVote = new SumVote((long) 0);

                            createUser(currentUser, SignUpActivity.this);
                            utilities.createSumVote(user.getUid(),sumVote);
                            utilities.getToken();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            email.setError("The email address is already in use by another account");
                            email.requestFocus();
                            email.clearComposingText();
                        }
                    }
                });
    }
    public void createUser(final User currentUser, final Context context) {
        db.collection("Users").document(currentUser.getId()).set(currentUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(context,"Sign Up successfully !", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                        } else {
                            Toast.makeText(context, "Sign Up failed. Please try again !", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}