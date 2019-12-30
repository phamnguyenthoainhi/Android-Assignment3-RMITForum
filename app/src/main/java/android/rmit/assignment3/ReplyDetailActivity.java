package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReplyDetailActivity extends AppCompatActivity{

    String id;
    String TAG="REPLY DETAIL";
    Reply reply = new Reply();
    ArrayList<Comment> comments = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    RecyclerView.Adapter adapter;

    TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_detail);

        content = findViewById(R.id.content_text);

        onNewIntent(getIntent());

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                comments = new ArrayList<>();
                fetchComments(id);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();

        fetchReply((String)bundle.get("id"));
        id =(String) bundle.get("id");

        fetchComments(id);

        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();


    }

    public void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CommentAdapter(comments);

        recyclerView.setAdapter(adapter);
    }

    protected void fetchReply (String id){
        db.collection("Replies").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        reply = documentSnapshot.toObject(Reply.class);
                        content.setText(reply.getContent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to fetch");
                    }
                });
    }

    protected  void fetchComments(String replyId){
        db.collection("Comments").whereEqualTo("reply",replyId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Comment comment = documentSnapshot.toObject(Comment.class);
                            comment.setId(documentSnapshot.getId());
                            comments.add(comment);
                            initRecyclerView();


                        }

                    }
                });

    }
}
