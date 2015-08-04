package net.pingfang.xmpp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.fragment.AccountFragment;
import net.pingfang.xmpp.fragment.ChatHistoryFragment;
import net.pingfang.xmpp.fragment.RosterItemFragment;
import net.pingfang.xmpp.service.ChatService;
import net.pingfang.xmpp.util.GlobalApplication;
import net.pingfang.xmpp.util.OnFragmentInteractionListener;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends FragmentActivity implements View.OnClickListener, OnFragmentInteractionListener {

    ChatService chatService;

    TextView tv_activity_title;
    FrameLayout fl_container;
    ViewPager pager;
    ChatHistoryFragment chatHistoryFragment;
    RosterItemFragment roasterFragment;
    AccountFragment accountFragment;
    Button btn_list_chat;
    Button btn_list_friend;
    Button btn_account_management;

    Map<String,TextView> map = new HashMap<>();
    MessageReceiver receiver;

    CollectionPagerAdapter adapter;
    List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        chatService = ChatService.newInstance(getApplicationContext());

        initView();
        initAdapter();
    }

    private void initView() {
        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        pager = (ViewPager) findViewById(R.id.pager);
        btn_list_chat = (Button) findViewById(R.id.btn_list_chat);
        btn_list_chat.setOnClickListener(this);
        btn_list_friend = (Button) findViewById(R.id.btn_list_friend);
        btn_list_friend.setOnClickListener(this);
        btn_account_management = (Button) findViewById(R.id.btn_account_management);
        btn_account_management.setOnClickListener(this);
    }

    private void initAdapter() {
        adapter = new CollectionPagerAdapter(getSupportFragmentManager());
        chatHistoryFragment = ChatHistoryFragment.newInstance();
        adapter.add(chatHistoryFragment);
        roasterFragment = RosterItemFragment.newInstance();
        adapter.add(roasterFragment);
        accountFragment = AccountFragment.newInstance();
        adapter.add(accountFragment);
        pager.setAdapter(adapter);


        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        tv_activity_title.setText(R.string.tv_activity_title_roster);
                        break;
                    case 2:
                        tv_activity_title.setText(R.string.tv_activity_title_account);
                        break;
                    case 0:
                        tv_activity_title.setText(R.string.tv_activity_title_history);
                        break;
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(receiver != null) {
            unregisterReceiver(receiver);
        }

    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_MESSAGE_INCOMING);
        registerReceiver(receiver,filter);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch(viewId) {
            case R.id.btn_list_chat:
                pager.setCurrentItem(0);
                break;
            case R.id.btn_list_friend:
                pager.setCurrentItem(1);
                break;
            case R.id.btn_account_management:
                pager.setCurrentItem(2);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(String name,String jid) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),ChatActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("jid",jid);
        startActivity(intent);
    }

    @Override
    public void loadRoster() {
        List<RosterEntry> entries = chatService.getRosterEntries();
        roasterFragment = (RosterItemFragment) adapter.getItem(1);
        roasterFragment.addEntries(entries);
    }

    @Override
    public void loadAccount() {
        AccountFragment fragment = (AccountFragment) adapter.getItem(2);
        String username = chatService.getAccountAttribute("name");
        if(!TextUtils.isEmpty(username)) {
            fragment.loadAccount(username);
        }
    }

    @Override
    public void logout() {
        chatService.logout();
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class CollectionPagerAdapter extends FragmentPagerAdapter {

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void add(Fragment fragment) {
            fragments.add(fragment);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GlobalApplication.ACTION_INTENT_MESSAGE_INCOMING)) {
                String name= intent.getStringExtra("name");
                String jid = intent.getStringExtra("jid");
                String body = intent.getStringExtra("body");

                ChatHistoryFragment fragment = (ChatHistoryFragment) adapter.getItem(0);
                fragment.updateMessage(name,jid,body);
            }
        }
    }
}
