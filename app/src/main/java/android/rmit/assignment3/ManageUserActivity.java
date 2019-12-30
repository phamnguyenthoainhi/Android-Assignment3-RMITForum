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
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;


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
    Uri myUri;
    View editDialog;
    private StorageReference mStorageRef;
    ImageView avatar;
    AlertDialog alertDialog;
    ImageView imageView;
    User fetchUser;
    boolean ifImageChange = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        logoutbtn = findViewById(R.id.logout);
        openeditform = findViewById(R.id.openedituser);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("ImageFolder");
        avatar = findViewById(R.id.avatarimage);
        editDialog = getLayoutInflater().inflate(R.layout.edit_user, null);
        imageView = editDialog.findViewById(R.id.imageview);

        RelativeLayout relativeLayout = findViewById(R.id.manageuserly);

        
        

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        user = new User();
        if (currentUser != null) {
            fetchCurrentUser(currentUser.getUid());
            if (currentUser.getPhotoUrl() != null) {
                imageView.setImageURI(currentUser.getPhotoUrl());
            }
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



    public void uploadData(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            ifImageChange = true;
            imageView.setImageURI(imageUri);

        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    public void uploadtostorage() {
        Log.d(TAG, "uploadtostorage: Called");

        final StorageReference imgname = mStorageRef.child("image" + this.imageUri.getLastPathSegment());
        imgname.putFile(this.imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgname.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (!usernameedit.getText().toString().isEmpty()) {
                            
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(usernameedit.getText().toString())
                                        .setPhotoUri(uri)
                                        .build();
                            user.setFullname(usernameedit.getText().toString());

                            currentUser.updateProfile(profileUpdates);
                                updateUser(uri.toString());
                            Picasso.with(ManageUserActivity.this).load(uri).fit().centerCrop()
                                    .placeholder(R.drawable.grey)
                                    .error(R.drawable.grey)
                                    .into(avatar);

                                user.setImageuri(uri.toString());

                        }

                    }


                });

            }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+ e);
                    }
                });
    }

    public void openForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageUserActivity.this);
        if (editDialog.getParent() != null) {
            ((ViewGroup)editDialog.getParent()).removeView(editDialog);
        }

        builder.setView(editDialog);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        usernameedit = editDialog.findViewById(R.id.usernameedit);
        usernameedit.setText(currentUser.getDisplayName());
        edit = editDialog.findViewById(R.id.editUserbtn);
        imageView = editDialog.findViewById(R.id.imageview);


//        imageView.setImageURI(currentUser.getPhotoUrl());
        Log.d(TAG, "openForm: current user photo url "+ currentUser.getPhotoUrl());
        editavatar = editDialog.findViewById(R.id.editavatar);


        editavatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData(view);

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

                                    if (ifImageChange) {
                                        uploadtostorage();

                                    } else {
                                        if (!usernameedit.getText().toString().isEmpty()) {

                                        updateUserProfile(usernameedit.getText().toString());
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(usernameedit.getText().toString())

                                                .build();
                                        currentUser.updateProfile(profileUpdates);

                                        user.setFullname(usernameedit.getText().toString());
                                        fetchCurrentUser(currentUser.getUid());
                                    }
                                    }
//
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
    
    
    public User fetch(String id) {
        fetchUser = new User();
        db.collection("Users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                fetchUser.setImageuri(snapshot.get("imageuri").toString());
            }
        });
        return fetchUser;
    }


    public Uri convertUri(String s) {
        Uri uri = Uri.parse(s);
        return uri;
    }
}
