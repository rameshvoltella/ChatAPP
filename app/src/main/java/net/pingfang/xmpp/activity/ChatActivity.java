package net.pingfang.xmpp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.service.ChatService;
import net.pingfang.xmpp.util.GlobalApplication;

public class ChatActivity extends FragmentActivity implements View.OnClickListener{

    ChatService chatService;

    String name;
    String jid;

    TextView btn_activity_back;
    TextView tv_activity_title;
    LinearLayout ll_message_container;
    EditText et_message;
    Button btn_send;

    MessageReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatService = ChatService.newInstance(getApplicationContext());
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        jid = intent.getStringExtra("jid");

        initView();
    }

    private void initView() {

        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(getString(R.string.title_activity_chat, name));

        ll_message_container = (LinearLayout) findViewById(R.id.ll_message_container);

        et_message = (EditText) findViewById(R.id.et_message);
        et_message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }
                return handled;
            }
        });

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_MESSAGE_INCOMING);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GlobalApplication.ACTION_INTENT_MESSAGE_INCOMING)) {
                String nameFrom= intent.getStringExtra("name");
                String jidFrom = intent.getStringExtra("jid");
                String body = intent.getStringExtra("body");

                if(nameFrom.equals(name)) {

                    TextView textView = new TextView(getApplicationContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setTextColor(Color.BLACK);
                    textView.setText(name + "\n" + body);
                    textView.setGravity(Gravity.RIGHT);
                    ll_message_container.addView(textView);
                } else {

                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_send:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        if(!TextUtils.isEmpty(et_message.getText().toString().trim())) {
            chatService.sendMessage(jid,et_message.getText().toString().trim());
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(name + "\n" + et_message.getText().toString().trim());
            textView.setTextColor(Color.RED);
            textView.setGravity(Gravity.LEFT);
            ll_message_container.addView(textView);
        }
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if(NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }
}
