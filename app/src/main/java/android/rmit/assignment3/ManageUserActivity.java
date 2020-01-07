package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.material.tabs.TabLayout;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ManageUserActivity extends AppCompatActivity {

    private static final String TAG = "ManageUserActivity";

    private static final int PICK_IMAGE_REQUEST = 1;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    FirebaseFirestore db= FirebaseFirestore.getInstance();;
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
    ArrayList<Course> subscribedCourses;
//    ArrayList<String> coursesid;
    Course fetchedCourse;
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    SubscribedCourseAdapter subscribedCourseAdapter;
    ArrayList<SumVote> sumVotes;
    ImageView trophy;
    RelativeLayout ranklayout;
    ViewPager viewPager;
    TabLayout tabLayout;
    PagerController pagerController;
    String userId;
    TextView ranking;
    ArrayList<UserSumVote> userSumVotes;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);
        ranklayout = findViewById(R.id.ranklayout);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        user = new User();
        editDialog = getLayoutInflater().inflate(R.layout.edit_user, null);

        imageView = editDialog.findViewById(R.id.imageview);
        trophy = findViewById(R.id.trophy);
        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        logoutbtn = findViewById(R.id.logout);
        openeditform = findViewById(R.id.openedituser);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("ImageFolder");
        avatar = findViewById(R.id.avatarimage);
        Button back = findViewById(R.id.fromUser);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tablayout);

        ranking = findViewById(R.id.ranking);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(ManageUserActivity.this, CourseActivity.class));
                finish();
            }
        });

        user = new User();

        onNewIntent(getIntent());

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ManageUserActivity.this, MainActivity.class));
            }
        });

        openeditform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openForm();
            }
        });

        if (currentUser.getUid().equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")){
            recyclerView = findViewById(R.id.rankrecyclerview);
            recyclerView.setVisibility(View.VISIBLE);
            TextView rankingtext = findViewById(R.id.ranktextview);
            rankingtext.setVisibility(View.VISIBLE);
            userSumVotes = new ArrayList<>();
            fetchSumVote();
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();

        if(bundle!=null) {
            if(bundle.get("id")!=null) {
                fetchCurrentUser((String) bundle.get("id"));
                userId = (String) bundle.get("id");

                fetchRank(userId);

                if (!mAuth.getUid().equals(userId)) {
                    openeditform.setVisibility(View.GONE);
                }

                pagerController = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());
                viewPager.setAdapter(pagerController);
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        }

    }

    public void uploadData(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    public void fetchRank(final String id) {
        db.collection("SumVotes").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        sumVotes = new ArrayList<>();
                        for (DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            SumVote sumVote = new SumVote();
                            sumVote.setSum((Long) doc.get("sum"));
                            sumVote.setId(doc.getId());
                            sumVotes.add(sumVote);

                        }

                        sumVotes = sort(sumVotes);

                        if (!id.equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")){
                            if (sumVotes.size() > 5 ) {
                                for (int i = 0; i < 5 ; i++) {
                                    if ((id.equals(sumVotes.get(i).getId())) && (sumVotes.get(i).getSum() >= 5) ) {
                                        ranklayout.setVisibility(View.VISIBLE);
                                        trophy.setImageResource(R.drawable.award);
                                        break;
                                    } else {
                                        ranklayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                            } if (sumVotes.size() == 2) {
                                for (int i = 0; i < 2 ; i++) {
                                    if (id.equals(sumVotes.get(i).getId()) && sumVotes.get(i).getSum() >= 5) {
                                        Log.d(TAG, "onSuccess: checking "+currentUser.getUid().equals(sumVotes.get(i).getId()) );
                                        ranklayout.setVisibility(View.VISIBLE);
                                        trophy.setImageResource(R.drawable.award);
                                        break;
                                    } else {
                                        Log.d(TAG, "onSuccess: 124");
                                        ranklayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                            } if (sumVotes.size() == 3) {
                                for (int i = 0; i < 3 ; i++) {
                                    if (id.equals(sumVotes.get(i).getId()) && sumVotes.get(i).getSum() >= 5) {
                                        ranklayout.setVisibility(View.VISIBLE);
                                        trophy.setImageResource(R.drawable.award);
                                        break;
                                    } else {
                                        ranklayout.setVisibility(View.INVISIBLE);
                                    }
                                }

                            } if (sumVotes.size() == 4) {
                                for (int i = 0; i < 4 ; i++) {
                                    if (id.equals(sumVotes.get(i).getId()) && sumVotes.get(i).getSum() >= 5) {
                                        ranklayout.setVisibility(View.VISIBLE);
                                        trophy.setImageResource(R.drawable.award);
                                        break;
                                    } else {
                                        ranklayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                            if (sumVotes.size() == 1) {
                                if (id.equals(sumVotes.get(0).getId()) && sumVotes.get(0).getSum() >= 5) {
                                    ranklayout.setVisibility(View.VISIBLE);
                                    trophy.setImageResource(R.drawable.award);

                                } else {
                                    ranklayout.setVisibility(View.INVISIBLE);
                                }

                            }
                        } else {
                            tabLayout.setVisibility(View.INVISIBLE);
                            ranklayout.setVisibility(View.VISIBLE);
                            trophy.setImageResource(R.drawable.icons8settings40);
                            ranking.setText("  Admin");
                        }
                    }
                });
    }
    public ArrayList<SumVote> sort(ArrayList<SumVote> sumVotes) {
        Collections.sort(sumVotes, new Comparator<SumVote>() {
            @Override
            public int compare(SumVote t1, SumVote t2) {
                return (int) (t2.getSum() - t1.getSum());
            }


        } );
        return sumVotes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            ifImageChange = true;
            Picasso.with(ManageUserActivity.this).load(imageUri).fit().centerCrop()
                    .placeholder(R.drawable.grey)
                    .error(R.drawable.grey)
                    .into(imageView);
//            imageView.setImageURI(imageUri);

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
                    Picasso.with(ManageUserActivity.this).load(convertUri(task.getResult().get("imageuri").toString()).toString()).fit().centerCrop()
                            .placeholder(R.drawable.grey)
                            .error(R.drawable.grey)
                            .into(avatar);
                } else {
                avatar.setImageResource(R.drawable.grey);
            }

                useremail.setText(task.getResult().get("email").toString());
                username.setText(task.getResult().get("fullname").toString());
            }
        });
    }

    public void fetchSumVote() {
        db.collection("SumVotes").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (final DocumentSnapshot doc: queryDocumentSnapshots
                             ) {
                            db.collection("Users").document(doc.getId()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot snapshot) {
                                            UserSumVote userSumVote = new UserSumVote();
                                            Log.d(TAG, "onSuccess: "+ snapshot.get("fullname") + " : " + doc.get("sum") );
                                            if (snapshot.get("imageuri") == null) {
                                                userSumVote.setImageuri(null);
                                                userSumVote.setUsername(snapshot.get("fullname").toString());
                                                userSumVote.setVote((Long) doc.get("sum"));
                                                userSumVote.setUserid(userId);
                                                userSumVotes.add(userSumVote);

                                                initRecylerview(userSumVotes);
                                            }
                                            else if (snapshot.get("fullname") == null) {
                                                userSumVote.setUsername("");
                                                userSumVote.setImageuri(snapshot.get("imageuri").toString());
                                                userSumVote.setVote((Long) doc.get("sum"));
                                                userSumVote.setUserid(userId);
                                                userSumVotes.add(userSumVote);

                                                initRecylerview(userSumVotes);
                                            }
                                            else if (doc.get("sum") == null) {
                                                userSumVote.setVote((long) 0);
                                                userSumVote.setImageuri(snapshot.get("imageuri").toString());
                                                userSumVote.setUsername(snapshot.get("fullname").toString());

                                                userSumVote.setUserid(userId);
                                                userSumVotes.add(userSumVote);

                                                initRecylerview(userSumVotes);
                                            }
                                            else {
                                                userSumVote.setImageuri(snapshot.get("imageuri").toString());
                                                userSumVote.setUsername(snapshot.get("fullname").toString());
                                                userSumVote.setVote((Long) doc.get("sum"));
                                                userSumVote.setUserid(userId);
                                                userSumVotes.add(userSumVote);
                                                initRecylerview(userSumVotes);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    public Uri convertUri(String s) {
        Uri uri = Uri.parse(s);
        return uri;
    }


    public void initRecylerview(ArrayList<UserSumVote> userSumVotes ) {
        System.out.println("Hello");
        RankAdapter rankAdapter = new RankAdapter(userSumVotes, ManageUserActivity.this);
        recyclerView.setAdapter(rankAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ManageUserActivity.this));
    }


}
