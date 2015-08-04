package net.pingfang.xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.service.ChatService;


public class MainActivity extends FragmentActivity {

    ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatService = ChatService.newInstance(getApplicationContext());

        if(chatService.isAuthenticated()) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void login(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
