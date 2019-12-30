package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationsListActivity extends AppCompatActivity implements NotificationAdapter.NotificationViewHolder.OnNotificationListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Notification> notifications = new ArrayList<>();
    RecyclerView.Adapter adapter;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "NotificationsListActivi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        if(mAuth.getCurrentUser()!=null){
            Toast.makeText(this, mAuth.getUid(), Toast.LENGTH_SHORT).show();
            fetchNotifications(mAuth.getUid());
        }
        else{
            Toast.makeText(this, "no user", Toast.LENGTH_SHORT).show();
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
        }
    }

    protected void fetchNotifications(String id){
        Toast.makeText(this, "fetching notif", Toast.LENGTH_SHORT).show();
        db.collection("Notifications").whereEqualTo("user",id).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Notification notification = documentSnapshot.toObject(Notification.class);
                            Toast.makeText(NotificationsListActivity.this, notification.getContent(), Toast.LENGTH_SHORT).show();
                            notification.setId(documentSnapshot.getId());
                            notifications.add(notification);
                        }
                        Log.d(TAG, "onSuccess: "+ notifications);
                        initRecyclerView();

                    }
                });
    }
}
