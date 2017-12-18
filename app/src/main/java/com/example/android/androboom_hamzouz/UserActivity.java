package com.example.android.androboom_hamzouz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;

public class UserActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            Log.v("AndroBoum_Hamzouz", "je suis déjà connecté sous l'email :"
                    +auth.getCurrentUser());
        } else{
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Array.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUi.FACEBOOK_PROVIDER).build()
            )).build(), 42);
        }
    }

    protected  void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            IdpResponse rep = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                Log.v("AndroBoum", "je me suis connecté et mon email est : "+
                rep.getEmail());
                return;
            } else{
                if (rep == null){
                    Log.v("Androboum", "back button On");
                    finish();
                    return;
                }
                if (rep.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.v("Androboum", "Erreur réseau");
                    finish();
                    return;
                }
                if (rep.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.v("Androboum", "Erreur inconnue");
                    finish();
                    return;
                }
            }
            Log.v("AndroBoum", "Réponse inconnue");
        }
    }
}
