package endpoints;

import coders.MessageDecoder;
import coders.MessageEncoder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Message;

import javax.websocket.*;
import javax.websocket.server.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Main endpoint
 * value -  URI where the endpoint is deployed
 * encoders - takes a Java object and produces a typical representation suitable for transmission as a message as JSON representation.
 * decoders - is used to transform data back into a Java object
 */
@ServerEndpoint(value = "/members/{username}", encoders = {MessageEncoder.class}, decoders = {MessageDecoder.class})
public class MembersEndpoint {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        PropertyConfigurator.configure("log4j.properties");
    }
    private static final Logger LOGGER = Logger.getLogger(MembersEndpoint.class);

    //auxiliary variable, used in logic when checking a name for uniqueness
    private boolean flag =false;
    private Session session = null;
    private String username = "unknown";
    private static final List<String> usernameList = new LinkedList<>();
    private static final List<Session> sessionList = new LinkedList<>();
    //the map stores the state of the user's hand
    private static final Map<String, String> map = new HashMap<String, String>();

    /**
     * Get session and WebSocket connection
     * Callback hook for Connection open events
     * @param session - the session which is opened.
     * @param username - nickname of the new user
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        //checking name for uniqueness
        for (String s : usernameList) {
            if (s.equalsIgnoreCase(username)) {
                onMessage(session, new Message(username, "0", "checkUnique", ""));
                this.flag = true;
                LOGGER.info(String.format("Connected: session %s without unique name", session.getId()));
            }
        }
        if (this.flag){
            LOGGER.info(String.format("Disconnected: session %s with a non-unique name", session.getId()));
            onClose(session, this.username);
        } else{
            //current username is unique therefore create the session
            this.session = session;
            sessionList.add(session);
            this.username = username;
            usernameList.add(username);
            map.put(username, "0");
            LOGGER.info(String.format("Connected: session %s", session.getId()));
            onMessage(session, new Message(username, "0", "membersList", ""));
        }
    }

    /**
     * Callback hook for Connection close events
     * @param session - the session which is getting closed
     * @param username - current username
     */
    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        if(!flag){
            sessionList.remove(session);
            usernameList.remove(username);
            map.remove(username);
            LOGGER.info(String.format("method: onClose, text: session %s disconnected", session.getId()));
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message
     * @param session - current session
     * @param message - the message
     */
    @OnMessage
    public void onMessage(Session session, Message message)  {
        message.setName(this.username);
        map.put(message.getName(), message.getStatus());
        if(map.containsKey("unknown")){
            map.remove("unknown");
        }
//        System.out.println("==========================");
//        usernameList.forEach(System.out::println);

        //using switch instead of if-else-if because
        //the switch statement is faster to execute than the if-else-if ladder
        //This is due to the compiler's ability to optimise the switch statement.
        LOGGER.info("Received: " + message);
        switch (message.getAction()){
            case "changeHand": {
                LOGGER.info("method: onMessage, text:changeHand");
                broadcast(message);
                break;
            }
            case "membersList": {
                LOGGER.info("method: onMessage, text:membersList");
                toJsonAndSend(message);
                break;
            }
            case "deleteFromAndUpdateMembersList": {
                LOGGER.info("method: onMessage, text:deleteFromAndUpdateMembersList");
                toJsonAndSend(message);
                break;
            }
            case "checkUnique":{
                LOGGER.info("method: onMessage, text:checkUnique");
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                    LOGGER.error("method: onMessage");
                }
                break;
            }
            default: onError(session,new UnsupportedOperationException());
        }

    }

    /**
     * Update list of users for each session
     * @param message - the message
     */
    private void toJsonAndSend(Message message) {
        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        String gsonString = gson.toJson(map,gsonType);
        message.setMemberList(gsonString);
        broadcast(message);
    }

    /**
     * send a message to all users
     * @param message - the message
     */
    private static void broadcast(Message message) {
        sessionList.forEach(s -> {
            try {
                s.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
                LOGGER.error("method: broadcast");
            }
        });
    }

    /**
     * Callback hook for Error Events. This is where error handling happens.
     * @param session - current session
     * @param throwable - the reason for the error
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
        LOGGER.info(String.format("method: onMessage, text:session %s caused error", session.getId()));
    }


}
