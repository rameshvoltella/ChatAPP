package net.pingfang.xmpp.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.pingfang.xmpp.R;
import net.pingfang.xmpp.util.GlobalApplication;
import net.pingfang.xmpp.util.MediaFileUtils;
import net.pingfang.xmpp.util.StringUtilsCompat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by gongguopei87@gmail.com on 2015/7/27.
 */
public class ChatService {

    public static final String TAG = ChatService.class.getSimpleName();

    public static final String HOST = "192.168.0.137";
    public static final int PORT = 5222;
    public static final int CONNECT_TIME_OUT = 20000;
    public static final String RESOURCE = "Smack";

    public static final long MAX_SIZE = 15 * 1024 * 1024;

    private XMPPTCPConnectionConfiguration connectionConfig;
    private XMPPTCPConnection connection;
    private ChatManager chatManager;
    private ChatMessageListener chatMessageListener;
    private ChatManagerListener chatManagerListener;
    private Roster roster;
    private AccountManager accountManager;
    private FileTransferManager fileTransferManager;


    private Context context;

    private static ChatService chatService;

    private ChatService(Context ctx) {

        this.context = ctx;

        chatMessageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                String body = message.getBody();
                if(!TextUtils.isEmpty(body)) {
                    Intent intent = new Intent();
                    intent.setAction(GlobalApplication.ACTION_INTENT_MESSAGE_INCOMING);
                    intent.putExtra("name",StringUtilsCompat.parseName(message.getFrom()));
                    intent.putExtra("jid", StringUtilsCompat.parseBareAddress(message.getFrom()));
                    intent.putExtra("body", body);
                    context.sendBroadcast(intent);
                }
            }
        };

        chatManagerListener = new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(chatMessageListener);
            }
        };

        initConnection();
    }

    public static ChatService newInstance(Context context) {
        if(chatService == null) {
            chatService = new ChatService(context);
        }
        return chatService;
    }

    public XMPPTCPConnection getConnection() throws Exception{
        if(connection == null) {
            throw new Exception(context.getString(R.string.xmpp_connection_not_initial));
        }
        return connection;
    }

    private void initConnection() {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setHost(HOST);
        configBuilder.setPort(PORT);
        configBuilder.setServiceName("192.168.0.137");
        configBuilder.setResource(RESOURCE);
        configBuilder.setCompressionEnabled(true);
        configBuilder.setConnectTimeout(CONNECT_TIME_OUT);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        connectionConfig = configBuilder.build();
        connection = new XMPPTCPConnection(connectionConfig);
        accountManager = AccountManager.getInstance(connection);
        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(chatManagerListener);

        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {

            }
        });

        fileTransferManager = FileTransferManager.getInstanceFor(connection);
        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                String fileName = request.getFileName();
                String mimeType = request.getMimeType();
                long fileSize = request.getFileSize();
                String requestor = request.getRequestor();

                if(fileSize > 0 && fileSize < MAX_SIZE) {
                    new IncomingFileTransferAsyncTask(request).execute(fileName,mimeType,requestor);
                } else {
                    try {
                        request.reject();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public Integer login(String username,String password) {
        try {
            if(!connection.isConnected()) {
                connection.connect();
            }
            if(connection.isConnected()) {
                if(!connection.isAuthenticated()) {
                    connection.login(username,password);
                }

                if(connection.isAuthenticated()) {
                    Presence presence = new Presence(Presence.Type.available);
                    presence.setStatus("Hello");
                    connection.sendStanza(presence);
                    return 1;
                } else {
                    return 3;
                }

            } else {
                return 2;
            }
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean isAuthenticated() {
        if(connection != null && connection.isConnected() && connection.isAuthenticated()) {
            return true;
        } else {
            return false;
        }
    }

    public void sendMessage(String to, String message) {
        Chat newChat = chatManager.createChat(to);

        Message newMessage = new Message();
        newMessage.setBody(message);
        try {
            if(connection.isConnected() && connection.isAuthenticated()) {
                newChat.sendMessage(newMessage);
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(String toJid, String filePath) {
        new OutgoingFileTransferAsyncTask().execute(toJid, filePath);
    }

    public List<RosterEntry> getRosterEntries() {
        List<RosterEntry> entries = new ArrayList<>();
        if(isAuthenticated()) {
            Collection<RosterEntry> rosterEntries = roster.getEntries();
            for (RosterEntry entry : rosterEntries) {
                entries.add(entry);
            }
        }

        return entries;
    }

    public String getAccountAttribute(String attribute) {
        if(isAuthenticated()) {
            try {
                String value =  accountManager.getAccountAttribute(attribute);
                return value;
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    public void logout() {
        if(isAuthenticated()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            presence.setStatus("Good bye!");
            try {
                connection.sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                initConnection();
            }

        }
    }

    private class IncomingFileTransferAsyncTask extends AsyncTask<String,String,String> {

        FileTransferRequest request;

        public IncomingFileTransferAsyncTask(FileTransferRequest request) {
            this.request = request;
        }

        @Override
        protected String doInBackground(String... params) {
            IncomingFileTransfer incomingFileTransfer = request.accept();
            if(MediaFileUtils.isExternalStorageWritable()) {
                File path = MediaFileUtils.getAlbumStorageDir(context,"ChatApp");
                try {
                    incomingFileTransfer.recieveFile(new File(path,params[0]));
                    while(!incomingFileTransfer.isDone()) {
                        if(incomingFileTransfer.getStatus().equals(FileTransfer.Status.error)) {
                            System.out.println("incomingFileTransfer " + incomingFileTransfer.getError());
                        } else {
                            if(incomingFileTransfer.getStatus().equals(FileTransfer.Status.in_progress)) {
                                publishProgress(incomingFileTransfer.getProgress() * 100 + "%");
                            } else if(incomingFileTransfer.getStatus().equals(FileTransfer.Status.refused)){
                                publishProgress("incomingFileTransfer refused");
                                break;
                            } else if(incomingFileTransfer.getStatus().equals(FileTransfer.Status.cancelled)) {
                                publishProgress("incomingFileTransfer cancelled");
                                break;
                            } else {
                                publishProgress(incomingFileTransfer.getStatus().toString());
                            }
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    if(incomingFileTransfer.isDone()) {
                        return "incomingFileTransfer finish";
                    } else {
                        return "incomingFileTransfer stop";
                    }
                } catch (SmackException e) {
                    e.printStackTrace();
                    return "incomingFileTransfer error";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "file io  error";
                }
            } else {
                return "ExternalStorage unavailable";
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("IncomingFileTransfer",values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("IncomingFileTransfer",s);
            if("incomingFileTransfer finish".equals(s)) {
                Intent intent = new Intent();
                intent.setAction(GlobalApplication.ACTION_INTENT_IMAGE_INCOMING);
                intent.putExtra("name", StringUtilsCompat.parseName(request.getRequestor()));
                intent.putExtra("jid", StringUtilsCompat.parseBareAddress(request.getRequestor()));
                intent.putExtra("path",MediaFileUtils.getAlbumStorageDir(context, "ChatApp").getPath());
                intent.putExtra("file",request.getFileName());
                context.sendBroadcast(intent);
            }
        }
    }

    private class OutgoingFileTransferAsyncTask extends AsyncTask<String,String,String> {

        OutgoingFileTransfer outgoingFileTransfer;

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("OutgoingFileTransfer",values[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            outgoingFileTransfer = fileTransferManager.createOutgoingFileTransfer(params[0] + "/" + RESOURCE);
            try {
                outgoingFileTransfer.sendFile(new File(params[1]), "You won't believe this!");
                while(!outgoingFileTransfer.isDone()) {
                    if(outgoingFileTransfer.getStatus().equals(FileTransfer.Status.error)) {
                        System.out.println("outgoingFileTransfer " + outgoingFileTransfer.getError());
                    } else {
                        if(outgoingFileTransfer.getStatus().equals(FileTransfer.Status.in_progress)) {
                            publishProgress(outgoingFileTransfer.getProgress() * 100 + "%");
                        } else if(outgoingFileTransfer.getStatus().equals(FileTransfer.Status.refused)){
                            publishProgress("outgoingFileTransfer refused");
                            break;
                        } else if(outgoingFileTransfer.getStatus().equals(FileTransfer.Status.cancelled)) {
                            publishProgress("outgoingFileTransfer cancelled");
                            break;
                        } else {
                            publishProgress(outgoingFileTransfer.getStatus().toString());
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                if(outgoingFileTransfer.isDone()) {
                    return "outgoingFileTransfer finish";
                } else {
                    return "outgoingFileTransfer stop";
                }
            } catch (SmackException e) {
                e.printStackTrace();
                return "outgoingFileTransfer error";
            }
        }
    }
}

