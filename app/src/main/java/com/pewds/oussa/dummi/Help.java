package com.pewds.oussa.dummi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class Help extends AppCompatActivity {
    String questionDB;
    ArrayList<String> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final TextView question = findViewById(R.id.question);
        final EditText answerExisting = findViewById(R.id.answer_existing);
        final EditText questionNew = findViewById(R.id.question_new);
        final EditText answerNew = findViewById(R.id.answer_new);
        if(getIntent().getExtras() != null &&!getIntent().getStringExtra("new").isEmpty()){
            questionNew.setText(getIntent().getStringExtra("new"));
            answerNew.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Help.this, MainActivity.class));
                Help.this.finish();
            }
        });
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    questions.add(d.getKey());
                }
                if (!questions.isEmpty()) {
                    questionDB = questions.get((int) (Math.random() * questions.size() + 0));
                    question.setText(questionDB);
                    findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            questionDB = questions.get((int) (Math.random() * questions.size() + 0));
                            question.setText(questionDB);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Problem accessing database", Toast.LENGTH_SHORT).show();

            }
        });
        answerExisting.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, final KeyEvent event) {
                final String answer = answerExisting.getText().toString().trim();
                if (actionId == EditorInfo.IME_ACTION_DONE && isOnline() && !answer.isEmpty() && questionDB != null) {
                    check_pushAnswer(answer, questionDB);
                }
                return false;
            }
        });
        answerNew.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String question = questionNew.getText().toString().trim().replaceAll("[^a-zA-Z ]", "").toLowerCase();
                    final String answer = answerNew.getText().toString().trim();
                    if (isOnline() && !question.isEmpty() && !answer.isEmpty()) {
                        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(question)) {
                                    Toast.makeText(getApplicationContext(), "Message already exists", Toast.LENGTH_SHORT).show();
                                    check_pushAnswer(answer, question);
                                } else {
                                     FirebaseDatabase.getInstance().getReference().child(question).push().setValue(answer).addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful()){
                                             Toast.makeText(getApplicationContext(), "Message added successfully", Toast.LENGTH_SHORT).show();
                                         }else {
                                             Toast.makeText(getApplicationContext(), "Problem accessing database", Toast.LENGTH_SHORT).show();

                                         }
                                         }
                                     });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Problem accessing database", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
                return false;
            }
        });
    }

    public void check_pushAnswer(final String answer, final String question) {
        FirebaseDatabase.getInstance().getReference().child(question).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean exist = false;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d.getValue().toString().equals(answer)) {
                        exist = true;
                        Toast.makeText(getApplicationContext(), "Answer already exists", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (!exist) {
                    FirebaseDatabase.getInstance().getReference().child(question).push().setValue(answer).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Answer added successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Problem accessing database", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Snackbar.make(findViewById(android.R.id.content), "Please check your internet connection", Snackbar.LENGTH_SHORT).show();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
