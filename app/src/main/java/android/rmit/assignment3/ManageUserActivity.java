package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ViewUtils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ManageUserActivity extends AppCompatActivity {

    private static final String TAG = "ManageUserActivity";

    private static final int PICK_IMAGE_REQUEST = 1;


    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    User user;
    TextView username;
    TextView useremail;
    EditText usernameedit;
    ImageButton logoutbtn;
    Button openeditform;
    Button edit;
    ImageButton editavatar;
    private Uri imageUri;
    View editDialog;
    private StorageReference mStorageRef;
    ImageView avatar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        logoutbtn = findViewById(R.id.logout);
        openeditform = findViewById(R.id.openedituser);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        avatar = findViewById(R.id.avatarimage);


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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

                 if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                         && data != null && data.getData() != null) {
                     imageUri = data.getData();
                     ImageView imageView = editDialog.findViewById(R.id.imageview);
                     imageView.setImageURI(null);
                     imageView.setImageURI(imageUri);

                 } else {
                     Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                 }

    }

    public void openForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageUserActivity.this);

        editDialog = getLayoutInflater().inflate(R.layout.edit_user, null);
        builder.setView(editDialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        usernameedit = editDialog.findViewById(R.id.usernameedit);
        usernameedit.setText(currentUser.getDisplayName());
        edit = editDialog.findViewById(R.id.editUserbtn);



        editavatar = editDialog.findViewById(R.id.editavatar);


        editavatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

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
                                    if (!usernameedit.getText().toString().isEmpty() && imageUri!= null) {
                                        updateUserProfile(usernameedit.getText().toString());
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(usernameedit.getText().toString())
                                                .setPhotoUri(imageUri)
                                                .build();
                                        currentUser.updateProfile(profileUpdates);
                                        user.setImageuri(imageUri.toString());
                                        user.setFullname(usernameedit.getText().toString());
                                        updateUser(imageUri.toString());
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
    public void updateUser(String imageuri) {
        db.collection("Users").document(currentUser.getUid())
                .update("imageuri", imageuri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ManageUserActivity.this, "Update user photo success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManageUserActivity.this, "Update user photo fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void fetchCurrentUser(String id) {
        db.collection("Users").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "onComplete: fetch User " + task.getResult().get("imageuri"));

                if (task.getResult().get("imageuri") != null) {
                    avatar.setImageURI(convertUri(task.getResult().get("imageuri").toString()));
                } else {
                    avatar.setImageResource(R.drawable.bell);
                }

                useremail.setText(task.getResult().get("email").toString());
                username.setText(task.getResult().get("fullname").toString());
            }
        });
    }

    
    public Uri convertUri(String s) {
        Uri uri = Uri.parse(s);
        return uri;
    }
}
