package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    EditText title;
    EditText content;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createPost = findViewById(R.id.create_post);

        Button postsList = findViewById(R.id.posts_list_button);

        bottomNavigationView = findViewById(R.id.botton_nav);
        Button toUserMana = findViewById(R.id.toUserMana);
        toUserMana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ManageUserActivity.class));
            }
        });

        postsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PostsListActivity.class);
                startActivity(intent);
            }
        });

        createNavBar();
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPostDialog();
            }
        });
    }

    public void showPostDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.invite,null);

        title = dialogView.findViewById(R.id.input_title);
        content = dialogView.findViewById(R.id.input_content);

        Button post = dialogView.findViewById(R.id.post);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost(new Post(title.getText().toString(),content.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public void toSignIn(View view) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void toSignUp(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void toCourse(View view) {
        startActivity(new Intent(this, CourseActivity.class ));

    }

    public void createPost(Post post){
        db.collection("Posts").add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, documentReference.getId(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,PostDetailActivity.class);
                        intent.putExtra("id",documentReference.getId());
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to upload post", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    public void createNavBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        Toast.makeText(MainActivity.this, "Switch To Home", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_user:
                        startActivity(new Intent(MainActivity.this, ManageUserActivity.class));
                        break;
                    case R.id.navigation_notifications:

                        Toast.makeText(MainActivity.this, "Switch to Notification", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }



}
