package com.unitechstudio.voicenotification.activities.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.widgets.R;

public class AlertDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_alert_dialog);

        Intent intent = getIntent();

        String missingLanaguage = intent.getStringExtra("language");

        String result = String.format("The voice data for %s is missing, so the app will not speak properly. Select OK and follow the instructions to install", missingLanaguage);

        AlertDialog.Builder builder = new AlertDialog.Builder(AlertDialogActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("NOTICE");
        builder.setMessage(result);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(installIntent);
                finish();
            }
        });//second parameter used for onclicklistener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        builder.show();

    }
}
