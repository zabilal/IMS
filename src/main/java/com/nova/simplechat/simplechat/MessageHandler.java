/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nova.simplechat.simplechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author NOVA SYSTEM
 */
public class MessageHandler implements Handler<Buffer> {
    
    private AbstractVerticle vert;
    private String chatRoom;
    private ServerWebSocket ws;

    public MessageHandler(AbstractVerticle vert, ServerWebSocket ws, String chatRoom) {
        this.vert = vert;
        this.chatRoom = chatRoom;
        this.ws  = ws;
    }

    @Override
    public void handle(Buffer data) {
       
        ObjectMapper m = new ObjectMapper();
        try {
            JsonNode messageFromClient = m.readTree(data.toString());

            ((ObjectNode) messageFromClient).put("received", new Date().toString());
            
            String jsonOutput = m.writeValueAsString(messageFromClient);
            
            System.out.println("json generated: " + jsonOutput);
            
            String userNumber =  new JsonObject(jsonOutput).getString("sender");
            
            vert.getVertx().sharedData().getLocalMap("chat.room." + chatRoom).put(userNumber, userNumber);
            
            System.out.println("registering new connection with id: " + userNumber + " for chat-room: " + userNumber);
//                vertx.sharedData().getLocalMap("chat.room." + chatRoom).put(id, id);
                
            System.out.println("Number of Users in Chatroom : " + vert.getVertx().sharedData().getLocalMap("chat.room." + chatRoom).size());            

            LocalMap<Object, Object> localMap = vert.getVertx().sharedData().getLocalMap("chat.room." + chatRoom);
            
            JsonObject messageJson = new JsonObject(jsonOutput);
            
            String action = messageJson.getString("action");
            
            handleMessageProcessing(action, messageJson, localMap);
            
            sendMessage(jsonOutput, localMap);            

        } catch (IOException e) {
            e.printStackTrace();
            ws.reject();
        }
    }

    private void handleMessageProcessing(String action, JsonObject messageJson, LocalMap<Object, Object> localMap) {
             
        
        switch(action){
            case "send" : sendMessage(messageJson.toString(), localMap);
            default: System.out.println("Error make you supply the send parameter !!! ");
        }
    }
    
    private void sendMessage(String jsonOutput, LocalMap<Object, Object> localMap) {
        
        JsonObject messageJson = new JsonObject(jsonOutput);
        
        String sender = messageJson.getString("sender");
        String receiver = messageJson.getString("receiver");
        
        
        if(messageJson.containsKey("room")){                //Broadcasting to Entire Room
            
            for (Map.Entry<Object, Object> entry : localMap.entrySet()) {
                
                String value = (String)entry.getValue();
                System.err.println("Address : " + (String)value);
                
                if(!value.equals(sender)){
                    vert.getVertx().eventBus().send(value, jsonOutput);
                }               

            }
        }else{                                              //Sending Message to Private 
            if(localMap.containsValue(receiver)){           //Checking if User is still in Room
                vert.getVertx().eventBus().send(receiver, jsonOutput);
            }
        }
        
        
        
    }

    
}
