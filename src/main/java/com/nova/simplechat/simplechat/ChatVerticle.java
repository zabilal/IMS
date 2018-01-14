package com.nova.simplechat.simplechat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Raji Zakariyya
 * <p>
 * Handles messages from the client and passes them to the EventHandler.
 */

public class ChatVerticle extends AbstractVerticle {

    private Map<String, AuthenticationHandler> authenticationHandler = new HashMap<>();
    private Map<String, ClientID> clients = new HashMap<>();
    private Map<String, ChatRoom> rooms = new HashMap<>();
    private Map<String, WiseAppMessageHandler> messageHandler = new HashMap<>();
    private Map<String, EventHandler> eventHandler = new HashMap<>();
    private List<String> onlineContacts = new ArrayList<>();

    private HttpServer server;
    private LoadManager loadManager;


    protected Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        loadManager = new LoadManager(vertx);

        // Bind action names to methods, using an enum.
        authenticationHandler.put(Token.ACTION, AuthenticationHandler.AUTHENTICATE_TOKEN);
        authenticationHandler.put(Authenticate.ACTION, AuthenticationHandler.AUTHENTICATE);

        messageHandler.put(Message.ACTION, WiseAppMessageHandler.MESSAGE);
        messageHandler.put(Join.ACTION, WiseAppMessageHandler.JOIN);
        messageHandler.put(Topic.ACTION, WiseAppMessageHandler.TOPIC);
        messageHandler.put(ServerList.ACTION, WiseAppMessageHandler.SERVERS);
        messageHandler.put(OnlineContact.ACTION, WiseAppMessageHandler.ONLINE);

        eventHandler.put(Message.ACTION, EventHandler.MESSAGE);
        eventHandler.put(Authenticate.ACTION, EventHandler.AUTHENTICATE);
        eventHandler.put(Room.ACTION, EventHandler.ROOM);
        eventHandler.put(UserEvent.ACTION, EventHandler.JOIN);
        eventHandler.put(Topic.ACTION, EventHandler.TOPIC);
        eventHandler.put(ServerList.ACTION, EventHandler.SERVERS);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        startServer();
        startEventListener();
        startUserCountLog();
    }

    private void startServer() {

        server = vertx.createHttpServer().websocketHandler(event -> {
                final ClientID client = new ClientID(event.textHandlerID());
                client.setId(event.textHandlerID());

                String clientPhoneNumber = event.headers().get("userId");
                client.setPhoneNumber(clientPhoneNumber);

                System.out.println("Client Phone Number ::: " + clientPhoneNumber);

                event.handler(data -> {
                    System.out.println("Data from client : " + data.toString() + " from Client : " + client.getPhoneNumber());
                    Packet packet = (Packet) (Serializer.unpack(data.toString(), Packet.class));

                    messageHandler.get(packet.getAction()).invoke(new Parameters(data.toString(), event, client, this));
//
                });

                event.closeHandler(close -> {
//                    removeFromRoom(client.getRoomMessage from Se(), client);
                    removeClient(client);
                });
                addClient(client);
//                sendBus(client.getId(), Serializer.pack(new Room(Configuration.SERVER_ROOM, Configuration.SERVER_TOPIC).setSystem(true)));
            }
        ).listen(Configuration.LISTEN_PORT);
        System.out.println("CHAT ROOM SERVER running on port " + Configuration.LISTEN_PORT);
    }

    private void sendAuthenticationFailed(ClientID client) {
        Authenticate authenticate = new Authenticate().setAuthenticated(false);
        sendBus(client.getId(), Serializer.pack(authenticate));
    }

    private void startEventListener() {
        vertx.eventBus().consumer(Configuration.DOWNSTREAM, handler -> {
            Packet packet = (Packet) Serializer.unpack(handler.body().toString(), Packet.class);
            eventHandler.get(packet.getAction()).invoke(new Event(handler.body().toString(), this));
        });
    }

    private void startUserCountLog() {
        vertx.setPeriodic(Configuration.LOG_INTERVAL, event -> sendBus(Configuration.BUS_LOGGER, Serializer.pack(new LogUserCount(clients.size()))));
    }

    /**
     * Triggers the event chain for joining a room,
     * the room is cached locally: the client is moved to the room and the room is notified.
     * If the room does not exist in this context: the server is queried for the room
     * and returns the existing, or creates a new. This method is used as a callback for completion.
     *
     * @param client the client joining the room.
     * @param room   the room to be joined.
     */
    protected void joinRoom(ClientID client, Room room) {
        String name = room.getRoom();

        if (rooms.containsKey(name)) {
            sendBus(client.getId(), Serializer.pack(new Room(rooms.get(name).getSettings(), room.getCreated())));
            messageRoom(room.getRoom(), Serializer.pack(new UserEvent(room.getRoom(), client.getPhoneNumber(), true)));
            sendBus(Configuration.UPSTREAM, Serializer.pack(new UserEvent(room.getRoom(), client.getPhoneNumber(), true)));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.UPSTREAM, Serializer.pack(new RoomEvent(name, RoomEvent.RoomStatus.POPULATED)));
            sendBus(Configuration.UPSTREAM, Serializer.pack(new Room(name, room.getTopic(), client.getPhoneNumber(), client.getId())));
            rooms.put(name, new ChatRoom(room));
            addToRoom(name, client);
        }
    }

    private void addToRoom(String name, ClientID client) {
        ChatRoom room = rooms.get(name);

//        if (client.getRoom() != null)
//            removeFromRoom(client.getRoom(), client);
        System.out.println("Logging Room Members ::: " + room.getClients() );
        if (room != null && room.get(client.getId()) == null) {
            room.add(client);
//            client.setRoom(name);
            System.out.println("Client " + client.getPhoneNumber() + " added to Room " + name);
        }
    }


    /**
     * Pushes a message onto the event bus for the given address.
     *
     * @param address of the bus.
     * @param data    as an object that is Serializable to JSON.
     */
    protected void sendBus(String address, Object data) {
        vertx.eventBus().send(address, Serializer.pack(data));
    }

    private void addClient(ClientID client) {

        if (clients.containsKey(client.getPhoneNumber())) {
            System.out.println("Client is Already Registered on the Server, Thanks");
        }
        else {
            clients.put(client.getPhoneNumber(), client);
            System.out.println("Client Registered at : " + client.getId() + " :: Serial :: " + client.getPhoneNumber());
            loadManager.manage(clients.size());
        }
    }

    private void removeClient(ClientID client) {
        clients.remove(client.getPhoneNumber());
        System.out.println("Client " + client.getId() + " Has been Removed ");
        loadManager.manage(clients.size());
    }

    protected ClientID getClient(String id) {
        return clients.get(id);
    }

    private void removeFromRoom(String room, ClientID client) {

        if (rooms.get(room) != null) {
            rooms.get(room).remove(client);

//            client.setRoom(null);
            messageRoom(room, new UserEvent(room, client.getUsername(), false));

            if (rooms.get(room).getClients().isEmpty()) {
                rooms.remove(room);

                sendBus(Configuration.UPSTREAM, Serializer.pack(new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED)));
            }
            sendBus(Configuration.UPSTREAM, Serializer.pack(new UserEvent(room, client.getPhoneNumber(), false)));
        }
    }


    /**
     * Broadcast a message to all users within the same room.
     *
     * @param name    of the room in which the message should be sent.
     * @param message which should be sent.
     */
    protected void messageRoom(String name, Object message) {
        System.out.println("Available Rooms : " + rooms);
        ChatRoom room = rooms.get(name);
        System.out.println("Room Name from ChatRooms : " + room.toString());
        System.out.println("Clients in the Room : " + room.getClients().toString());

        if (room != null) {
            if (message instanceof Message){
                room.addHistory((Message) message);
                System.out.println("Message arrived in Room :  " + room.toString() );
            }

            for (ClientID client : room.getClients().values()) {
                    sendBus(client.getId(), message);
            }
        }
    }

    protected void messageRoom(String name, Message message, String sender) {
        System.out.println("Available Rooms : " + rooms);
        ChatRoom room = rooms.get(name);
        System.out.println("Room Name from ChatRooms : " + room.toString());
        System.out.println("Clients in the Room : " + room.getClients().toString());

        if (room != null) {
            if (message instanceof Message){
                room.addHistory(message);
                System.out.println("Message arrived in Room :  " + room.toString() );
            }

            for (ClientID client : room.getClients().values()) {
                if (client.getPhoneNumber() != sender)
                    sendBus(client.getId(), message);
            }
        }
    }

    /**
     * Sends a message to specific user[Private Chat].
     *
     * @param receiver of the user that will receive the message
     *           this id is used to fetch client handler from clients map.
     * @param message which should be sent.
     */
    protected void messageClient(String receiver, Object message) {
//        System.out.println("Available clients : " + clients);
        if (clients.get(receiver) != null) {
            ClientID client = clients.get(receiver);
            System.out.println("Receiving Client : " + client.toString());

            if (client != null) {
                if (message instanceof Message){
                    System.out.println("Message to Client :::: " + message.toString());
                    sendBus(client.getId(), message);
                }
            }
        }
    }


    protected Map<String, ClientID> getClients() {
        return clients;
    }


    /**
     * Request a change of the rooms topic, the request is rejected when the requestor is not the owner
     * of the room.
     *
     * @param topic  the new topic.
     * @param client the initiator of the request.
     */
    protected void trySetTopic(Topic topic, ClientID client) {
        Room room = rooms.get(topic.getRoom()).getSettings();

        if (room.getOwner().equals(client.getUsername())) {
            setRoomTopic(topic, true);
        } else {
            messageRoom(topic.getRoom(), new Topic().setRejected(true));
        }
    }


    /**
     * Sets the topic for a room.
     *
     * @param topic            the topic and room.
     * @param locallyInitiated indicates whether a directly connected client initiated the change.
     *                         if set to false indicating that it was received as an event. (should not be broadcast)
     */
    protected void setRoomTopic(Topic topic, boolean locallyInitiated) {
        ChatRoom room = rooms.get(topic.getRoom());
        room.getSettings().setTopic(topic.getTopic());

        for (ClientID client : room.getClients().values())
            sendBus(client.getId(), topic);

        if (locallyInitiated)
            sendBus(Configuration.UPSTREAM, topic);
    }

    /**
     * Receives contacts from clients and if those contacts are online, then sends back
     * a list containing only those that are online
     *
     * @param contacts a list of contacts(phone numbers) received from client on connnection     *
     * @param sender the client making the enquiries for online contacts
     *
     * */
    protected void checkOnlineStatus(String contacts, String sender){
        onlineContacts.clear(); //clear the map for next client

        ClientID client = clients.get(sender);

        String [] contactArray = contacts.split(",");

        for (int i = 0; i < contactArray.length; i++) {
            if (clients.containsKey(contactArray[i])){
                onlineContacts.add(contactArray[i]);
            }
        }

        sendBus(client.getId(), onlineContacts);
    }


    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        server.close();
    }



}
