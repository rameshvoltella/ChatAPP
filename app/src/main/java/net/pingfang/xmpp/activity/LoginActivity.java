package net.pingfang.xmpp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.service.ChatService;


public class LoginActivity extends FragmentActivity {

    EditText et_user_name;
    EditText et_password;

    TextView tv_warning;

    ProgressDialog pd;
    ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_warning = (TextView) findViewById(R.id.tv_warning);

        chatService = ChatService.newInstance(getApplicationContext());
    }

    public void login(View view) {
        if(checkLoginParam()) {
            login(et_user_name.getText().toString().trim(), et_password.getText().toString().trim());
        }
    }

    private boolean checkLoginParam() {
        if(!TextUtils.isEmpty(et_user_name.getText().toString().trim()) && !TextUtils.isEmpty(et_password.getText().toString().trim())) {
            return true;
        } else if(TextUtils.isEmpty(et_user_name.getText().toString().trim())) {
            tv_warning.setText(getString(R.string.tv_warning_message_username_invalid));
        } else{
            tv_warning.setText(getString(R.string.tv_warning_message_password_invalid));
        }

        return false;
    }

    private void login(String username, String password) {
        new AsyncTask<String, Void, Integer>(){
            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(LoginActivity.this);
                pd.setTitle(getString(R.string.pd_title));
                pd.setMessage(getString(R.string.pd_loading));
                pd.show();
            }
            @Override
            protected Integer doInBackground(String... strings) {
                return chatService.login(strings[0], strings[1]);
            }
            @Override
            protected void onPostExecute(Integer resultCode) {
                pd.dismiss();
                if (resultCode == 1){
                    //成功登陆SUCCESS
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else if(resultCode == 2){
                    Log.e("error", "resultCode == " + resultCode);
                    tv_warning.setText(getString(R.string.tv_warning_message_connect_error));
                } else if(resultCode == 3){
                    Log.e("error", "resultCode == " + resultCode);
                    tv_warning.setText(getString(R.string.tv_warning_message_connect_ok_sign_in_error));
                } else {
                    Log.e("error", "resultCode == " + resultCode);
                    tv_warning.setText(getString(R.string.tv_warning_message_login_invalid));
                }
            }
        }.execute(username,password);
    }

}

