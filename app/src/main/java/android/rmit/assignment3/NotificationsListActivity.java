package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationsListActivity extends AppCompatActivity implements NotificationAdapter.NotificationViewHolder.OnNotificationListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Notification> notifications = new ArrayList<>();
    RecyclerView.Adapter adapter;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "NotificationsList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        Button back = findViewById(R.id.fromnoti);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(mAuth.getCurrentUser()!=null){
            fetchNotifications(mAuth.getUid());
        }

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notifications = new ArrayList<>();
                fetchNotifications(mAuth.getUid());
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            notifications = new ArrayList<>();
            fetchNotifications(mAuth.getUid());
            adapter.notifyDataSetChanged();
        }
    }

    protected void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NotificationAdapter(notifications,this);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public void onNotificationClick(int position) {
        Intent intent;
        switch(notifications.get(position).getType()){
            case "reply":
                intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra("id",notifications.get(position).getTargetId());
                startActivity(intent);
                break;
            case "comment":
                intent = new Intent(this,ReplyDetailActivity.class);
                intent.putExtra("id",notifications.get(position).getTargetId());
                startActivity(intent);
                break;
            case "post":
                intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra("id",notifications.get(position).getTargetId());
                startActivity(intent);
                break;
        }
    }

    protected void fetchNotifications(String id){
        db.collection("Notifications").whereEqualTo("user",id).orderBy("dateTime", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Toast.makeText(NotificationsListActivity.this, "Successfully fetched notifs", Toast.LENGTH_SHORT).show();
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Notification notification = documentSnapshot.toObject(Notification.class);
                            notification.setId(documentSnapshot.getId());
                            notifications.add(notification);
                            updateNotificationStatus(documentSnapshot.getId());
                        }
                        initRecyclerView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    protected void updateNotificationStatus(String id){
        db.collection("Notifications").document(id).update("seen",true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Set done.");
                    }
                });
    }

}
