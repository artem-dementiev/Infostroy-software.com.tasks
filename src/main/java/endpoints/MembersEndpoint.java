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

@ServerEndpoint(value = "/members/{username}", encoders = {MessageEncoder.class}, decoders = {MessageDecoder.class})
public class MembersEndpoint {

    private boolean flag =false;
    private Session session = null;
    private String username = "unknown";
    private static List<String> usernameList = new LinkedList<>();
    private static List<Session> sessionList = new LinkedList<>();
    private static Map<String, String> map = new HashMap<String, String>();


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {

        Iterator<String> it = usernameList.iterator();
        while (it.hasNext()){
            if (it.next().equalsIgnoreCase(username)){
                onMessage(session, new Message(username, "0", "checkUnique", ""));
                this.flag = true;
            }
        }
        if (this.flag){
            onClose(session, this.username);
        } else{
            this.session = session;
            sessionList.add(session);
            this.username = username;
            usernameList.add(username);
            map.put(username, "0");
            onMessage(session, new Message(username, "0", "membersList", ""));
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        System.out.println("onClose");
        if(!flag){
            sessionList.remove(session);
            usernameList.remove(username);
            map.remove(username);
        }
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        message.setName(this.username);
        map.put(message.getName(), message.getStatus());
        if(map.containsKey("unknown")){
            map.remove("unknown");
        }
//        System.out.println("==========================");
//        usernameList.forEach(System.out::println);
        if (message.getAction().equalsIgnoreCase("changeHand")) {
            sessionList.forEach(s -> {
                try {
                    s.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            });
        } else if (message.getAction().equalsIgnoreCase("membersList")) {
            System.out.println("membersList");
            toJsonAndSend(message);
        } else if(message.getAction().equalsIgnoreCase("deleteFromAndUpdateMembersList")){
            System.out.println("deleteFromAndUpdateMembersList");
            toJsonAndSend(message);
        } else if(message.getAction().equalsIgnoreCase("checkUnique")){
            System.out.println("checkUnique");
            try {
                session.getBasicRemote().sendObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (EncodeException e) {
                e.printStackTrace();
            }
        }

    }
    private void toJsonAndSend(Message message){
        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        String gsonString = gson.toJson(map,gsonType);
        message.setMemberList(gsonString);
        sessionList.forEach(s -> {
            try {
                s.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
        System.out.println("OnError");
    }


}
