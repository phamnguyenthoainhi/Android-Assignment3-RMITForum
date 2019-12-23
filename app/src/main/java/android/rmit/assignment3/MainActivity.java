package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Utilities utilities = new Utilities();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

        updateToken();

        createNavBar();

    }

    public void updateToken(){
        if(mAuth.getCurrentUser()!=null){
            utilities.getToken();
        }
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
                        startActivity(new Intent(MainActivity.this,NotificationsListActivity.class));
                        //Toast.makeText(MainActivity.this, "Switch to Notification", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

}
