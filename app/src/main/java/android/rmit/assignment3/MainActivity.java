package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Utilities utilities = new Utilities();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

        updateToken();


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




}
