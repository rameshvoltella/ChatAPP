package net.pingfang.xmpp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.service.ChatService;
import net.pingfang.xmpp.util.OnFragmentInteractionListener;
import net.pingfang.xmpp.util.StringUtilsCompat;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.List;

public class RosterItemFragment extends Fragment implements AbsListView.OnItemClickListener{

    private OnFragmentInteractionListener mListener;

    private List<RosterEntry> entries = new ArrayList<>();

    private ListView mListView;

    private RosterListAdapter mAdapter;

    public static RosterItemFragment newInstance() {
        RosterItemFragment fragment = new RosterItemFragment();
        return fragment;
    }

    public RosterItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new RosterListAdapter(getActivity());

        if (null != mListener) {
            mListener.loadRoster();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        return view;
    }

    public void addEntries(List<RosterEntry> entryList) {
        entries.addAll(entryList);
        mAdapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            String fullJid = entries.get(position).getUser() + "/" + ChatService.RESOURCE;
            mListener.onFragmentInteraction(StringUtilsCompat.parseName(fullJid),StringUtilsCompat.parseBareAddress(fullJid));
        }
    }

    private class RosterListAdapter extends BaseAdapter {
        Context context;

        public RosterListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Object getItem(int position) {
            return entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.list_item_roster, null);
                RosterEntry entry = entries.get(position);
                TextView tv_roster_entry_name = (TextView) view.findViewById(R.id.tv_roster_entry_name);
                tv_roster_entry_name.setText(entry.getName());
                view.setTag(entry.getUser());
                convertView = view;
            } else {
                RosterEntry entry = entries.get(position);
                TextView tv_roster_entry_name = (TextView) convertView.findViewById(R.id.tv_roster_entry_name);
                tv_roster_entry_name.setText(entry.getName());
            }
            return convertView;
        }
    }

}
