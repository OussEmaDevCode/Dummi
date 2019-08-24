package com.pewds.oussa.dummi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.how).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle("How is Dummi ?")
                        .setMessage("Dummi is an interactive chatBot that learns from its rich and active community. " +
                                "You can help us improve Dummi by pressing the Help button or test it by pressing the Chat button")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(true).create().show();

            }
        });
        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(MainActivity.this,Help.class));
            }
        });
        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Chat.class));
            }
        });
    }
}
