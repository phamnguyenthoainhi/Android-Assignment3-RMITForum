package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ManageUserActivity extends AppCompatActivity {

    private static final String TAG = "ManageUserActivity";
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    User user;
    TextView username;
    TextView useremail;
    EditText usernameedit;
    Button logoutbtn;
    Button openeditform;
    Button edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        logoutbtn = findViewById(R.id.logout);
        openeditform = findViewById(R.id.openedituser);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        user = new User();
        if (currentUser != null) {
            fetchCurrentUser(currentUser.getUid());
        }

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        openeditform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openForm();
            }
        });
    }

    public void openForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageUserActivity.this);

        final View editDialog = getLayoutInflater().inflate(R.layout.edit_user, null);
        builder.setView(editDialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        usernameedit = editDialog.findViewById(R.id.usernameedit);
        usernameedit.setText(currentUser.getDisplayName());
        edit = editDialog.findViewById(R.id.editUserbtn);


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (usernameedit.getText().toString().isEmpty()) {
                    usernameedit.setError("User name can not be empty");
                    usernameedit.requestFocus();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext()).setTitle("Confirmation").setMessage("Do you want to save these changes?")
                            .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!usernameedit.getText().toString().isEmpty()) {
                                        updateUserProfile(usernameedit.getText().toString());
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(usernameedit.getText().toString()).build();
                                        currentUser.updateProfile(profileUpdates);
                                        fetchCurrentUser(currentUser.getUid());

                                    }
                                }
                            })
                            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder1.create().show();
                    alertDialog.dismiss();
                }

            }
        });
        alertDialog.show();
    }


    public void updateUserProfile(String fullname) {
        db.collection("Users").document(currentUser.getUid())
                .update("fullname", fullname).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ManageUserActivity.this, "Update user name success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManageUserActivity.this, "Update user name fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void fetchCurrentUser(String id) {
        db.collection("Users").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "onComplete: fetch User " + task.getResult().get("email"));
                useremail.setText(task.getResult().get("email").toString());
                username.setText(task.getResult().get("fullname").toString());
            }
        });
    }
}
