package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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


public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    EditText email;
    EditText password;
    Button signin;
    Button show;
    Button hide;

    SignInButton signinGoogle;
    private FirebaseAuth mAuth;
    User currentUser;
    Utilities utilities = new Utilities();
    //Google Login Request Code
    private int RC_SIGN_IN = 123;
    //Google Sign In Client
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailSignin);
        password = findViewById(R.id.passwordSignin);
        signin = findViewById(R.id.signin);
        signinGoogle = findViewById(R.id.signingg);
        show = findViewById(R.id.showpasswordsignin);
        hide = findViewById(R.id.hidepasswordsignin);
        show.setVisibility(View.INVISIBLE);

        TextView signupfromsignin = findViewById(R.id.signupfromsignin);
        signupfromsignin.setClickable(true);
        signupfromsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
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




        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("205169153058-csuqplq7pnscdf21eg0hsabo6i8oqni9.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    signin(email.getText().toString(), password.getText().toString());
                }
            }
        });

        signinGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mGoogleSignInClient = new GoogleSignInClient();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//         Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            currentUser = new User(user.getUid(), user.getDisplayName(), user.getEmail());
                            utilities.createUser(currentUser, SignInActivity.this);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

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
        mAuth.signInWithEmailAndPassword(inputemail, inputpassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            utilities.getToken();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (task.getException().toString().contains("There is no user record corresponding to this identifier. The user may have been deleted")) {
                                email.setError("Email is not register");
                                email.clearComposingText();
                                email.requestFocus();
                            } else if (task.getException().toString().contains(" The password is invalid or the user does not have a password")) {
                                password.setError("Password is invalid");
                                password.clearComposingText();
                                password.requestFocus();
                            }
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    //    Hide the keyboard
    public void hideKeyBoard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);
    }


}
