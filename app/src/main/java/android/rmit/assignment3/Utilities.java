package android.rmit.assignment3;

import android.app.Activity;
import android.util.Log;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class Utilities {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public void createUser(final User currentUser, final Context context) {

        db.collection("Users").document(currentUser.getId()).set(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context,"Sign Up successfully !", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(context, "Sign Up failed. Please try again !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
//    Hide the keyboard
    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void getToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                updateToken(token);
            }
        });
    }

    public void updateToken(String token){
        if(mAuth.getUid()!=null) {
            HashMap<String, String> document = new HashMap<>();
            document.put("token",token);
            db.collection("Tokens").document(mAuth.getUid()).set(document)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TOKEN ID", "Updated.");
                        }
                    });
        }
    }


}
