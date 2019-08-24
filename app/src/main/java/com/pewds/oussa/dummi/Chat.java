package com.pewds.oussa.dummi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Chat extends AppCompatActivity {
    ArrayList<String> messages = new ArrayList<>();
    MessageAdapter messageAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageAdapter  = new MessageAdapter(this,messages);
        final ImageView send = findViewById(R.id.send);
        final EditText messageEdit = findViewById(R.id.messageEdit);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Chat.this, MainActivity.class));
            }
        });
        final ListView listMessages = findViewById(R.id.list_of_messages);
        listMessages.setAdapter(messageAdapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String unfor = messageEdit.getText().toString().trim();
                final String message = messageEdit.getText().toString().trim().replaceAll("[^a-zA-Z ]", "").toLowerCase();
                if(isOnline() && !message.isEmpty() ){
                    messages.add(messageEdit.getText().toString()+"└"+"0");
                    messageAdapter = new MessageAdapter(Chat.this,messages);
                    listMessages.setAdapter(messageAdapter);
                    messageEdit.setText("");
                    FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(message)){
                                ArrayList<String>answers = new ArrayList<>();
                                for(DataSnapshot d :dataSnapshot.child(message).getChildren()){
                                    answers.add(d.getValue().toString());
                                }
                                messages.add(answers.get((int) (Math.random() * answers.size() + 0))+"└"+"1");
                                messageAdapter = new MessageAdapter(Chat.this,messages);
                                listMessages.setAdapter(messageAdapter);
                            }else {
                                messages.add(unfor+"└"+"2");
                                messageAdapter = new MessageAdapter(Chat.this,messages);
                                listMessages.setAdapter(messageAdapter);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }

    public class MessageAdapter extends ArrayAdapter<String>{
        public MessageAdapter(Context context, ArrayList<String> messages) {
            super(context, 0,messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String[] message = getItem(position).split("└");
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message,parent,false);
            }
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Text", message[0]);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(),"message copied to clipboard",Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            if(message[1].equals("0")){
                TextView me = convertView.findViewById(R.id.message_text_me);
                me.setText(message[0]);
                convertView.findViewById(R.id.message_text_him).setVisibility(View.GONE);
            } else if (message[1].equals("1")) {
                TextView him = convertView.findViewById(R.id.message_text_him);
                him.setText(message[0]);
                convertView.findViewById(R.id.message_text_me).setVisibility(View.GONE);
            }else if (message[1].equals("2")){
                TextView him = convertView.findViewById(R.id.message_text_him);
                him.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Chat.this,Help.class);
                        i.putExtra("new",message[0]);
                        startActivity(i);
                    }
                });
                him.setText("I have never heard of this before, help me learn by tapping here");
                convertView.findViewById(R.id.message_text_me).setVisibility(View.GONE);
            }
            return convertView;
        }
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