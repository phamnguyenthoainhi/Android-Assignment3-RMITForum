package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostDetailActivity extends AppCompatActivity {

    String TAG = "Post Detail";
    String id;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Post post = new Post();
    TextView title;
    TextView content;
    //Reply newReply = new Reply();
    EditText newReplyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        title = findViewById(R.id.title_text);
        content = findViewById(R.id.content_text);

        Button replyButton = findViewById(R.id.reply_button);

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReplyDialog();
            }
        });

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();

        fetchPost(bundle.getString("id"));
        id = bundle.getString("id");

        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();


    }

    protected void fetchPost (String id){
        db.collection("Posts").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        post = documentSnapshot.toObject(Post.class);
                        title.setText(post.getTitle());
                        content.setText(post.getContent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to fetch");
                    }
                });
    }

    public void showReplyDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.reply,null);


        newReplyContent = dialogView.findViewById(R.id.reply_input_content);

        Button reply = dialogView.findViewById(R.id.create_reply);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReply(new Reply(id,newReplyContent.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    protected void createReply(Reply reply){
        db.collection("Replies").add(reply)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostDetailActivity.this, "Successfully posted reply.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailActivity.this, "Failed to post reply. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
