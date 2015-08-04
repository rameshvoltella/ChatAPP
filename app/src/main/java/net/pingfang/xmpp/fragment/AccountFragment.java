package net.pingfang.xmpp.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.util.OnFragmentInteractionListener;


public class AccountFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    TextView tv_account_name;
    TextView tv_account_pwd;
    Button btn_account_logout;

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        tv_account_name = (TextView) view.findViewById(R.id.tv_account_name);
        tv_account_pwd = (TextView) view.findViewById(R.id.tv_account_pwd);
        btn_account_logout = (Button) view.findViewById(R.id.btn_account_logout);
        btn_account_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.logout();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (null != mListener) {
            mListener.loadAccount();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void loadAccount(String username) {
        tv_account_name.setText(getString(R.string.tv_account_name,username));
    }

}
