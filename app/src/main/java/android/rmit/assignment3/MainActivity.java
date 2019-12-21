package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button toSignIn = findViewById(R.id.toSignIn);
//        Button to
    }

    public void toSignIn(View view) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void toSignUp(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}
