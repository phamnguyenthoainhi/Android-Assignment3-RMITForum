package android.rmit.assignment3;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

public class Utilities {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
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


}
