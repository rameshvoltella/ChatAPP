package net.pingfang.xmpp.util;

/**
 * Created by gongguopei87@gmail.com on 2015/7/31.
 */
public interface OnFragmentInteractionListener {

    public void onFragmentInteraction(String name,String jid);
    public void loadRoster();
    public void loadAccount();
    public void logout();

}
