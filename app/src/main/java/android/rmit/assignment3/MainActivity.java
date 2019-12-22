package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    EditText title;
    EditText content;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createPost = findViewById(R.id.create_post);

        Button postsList = findViewById(R.id.posts_list_button);

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


}
