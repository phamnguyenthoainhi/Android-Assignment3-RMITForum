package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Utilities utilities = new Utilities();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView ;
    FirebaseUser currentUser;
    WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView welcome = findViewById(R.id.welcometext);
        Button started = findViewById(R.id.getstarted);

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            welcome.setText("Welcome, "+ currentUser.getDisplayName());
            started.setText("Get Started!  ");
            started.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, CourseActivity.class));

                }
            });
        } else {
            welcome.setText("Welcome");
            started.setText("Get Sign In  ");
            started.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            });
        }

        wifiManager = (WifiManager) getApplicationContext().getSystemService(MainActivity.this.WIFI_SERVICE);


        bottomNavigationView = findViewById(R.id.botton_nav);




        updateToken();

        createNavBar();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intent = new IntentFilter(wifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiReceiver);
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiState = intent.getIntExtra(wifiManager.EXTRA_WIFI_STATE, wifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Toast.makeText(context, "Wifi is off", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder buider = new AlertDialog.Builder(MainActivity.this).setMessage("Wifi must be on to continue")
                            .setNegativeButton("Turn on", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                }
                            });
                    buider.setCancelable(false);
                    buider.create().show();
                    break;

            }
        }
    };

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

                        break;
                    case R.id.navigation_user:
                        startActivity(new Intent(MainActivity.this, ManageUserActivity.class));
                        break;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(MainActivity.this,NotificationsListActivity.class));

                        break;
                }
                return true;
            }
        });
    }

}
