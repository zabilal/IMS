package com.nova.simplechat.simplechat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raji Zakariyya
 * <p>
 * Handles messages from the client and passes them to the EventHandler.
 */

public class ChatVerticle extends AbstractVerticle {

    private Map<String, ClientID> clients = new HashMap<>();
    private Map<String, ChatRoom> rooms = new HashMap<>();
    private Map<String, WiseAppMessageHandler> messageHandler = new HashMap<>();
    private Map<String, AuthenticationHandler> authenticationHandler = new HashMap<>();
    private Map<String, EventHandler> eventHandler = new HashMap<>();

    private HttpServer server;


    protected Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // Bind action names to methods, using an enum.
        authenticationHandler.put(Authenticate.ACTION, AuthenticationHandler.AUTHENTICATE);

        messageHandler.put(Message.ACTION, WiseAppMessageHandler.MESSAGE);
        messageHandler.put(Join.ACTION, WiseAppMessageHandler.JOIN);
        messageHandler.put(Topic.ACTION, WiseAppMessageHandler.TOPIC);

        eventHandler.put(Message.ACTION, EventHandler.MESSAGE);
        eventHandler.put(Authenticate.ACTION, EventHandler.AUTHENTICATE);
        eventHandler.put(Room.ACTION, EventHandler.ROOM);
        eventHandler.put(UserEvent.ACTION, EventHandler.JOIN);
        eventHandler.put(Topic.ACTION, EventHandler.TOPIC);

        startServer();
//        startEventListener();
        startUserCountLog();
    }

    private void startServer() {

        server = vertx.createHttpServer().websocketHandler(event -> {
                final ClientID client = new ClientID(event.textHandlerID());
                client.setUsername(event.textHandlerID());

                String clientPhoneNumber = event.headers().get("id");
                client.setPhoneNumber(clientPhoneNumber);

                event.handler(data -> {
                    System.out.println("Data from client : " + data.toString() + " from Client : " + client);
                    Packet packet = (Packet) (Serializer.unpack(data.toString(), Packet.class));

                    messageHandler.get(packet.getAction()).invoke(new Parameters(data.toString(), event, client, this));
//
                });

                event.closeHandler(close -> {
//                    removeFromRoom(client.getRoom(), client);
                    removeClient(client);
                });
                addClient(client);
                sendBus(client.getId(), Serializer.pack(new Room(Configuration.SERVER_ROOM, Configuration.SERVER_TOPIC).setSystem(true)));
            }
        ).listen(Configuration.LISTEN_PORT);
        System.out.println("Room running on port " + Configuration.LISTEN_PORT);
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
            messageRoom(room.getRoom(), Serializer.pack(new UserEvent(room.getRoom(), client.getUsername(), true)));
            sendBus(Configuration.NOTIFY, Serializer.pack(new UserEvent(room.getRoom(), client.getUsername(), true)));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.NOTIFY, Serializer.pack(new RoomEvent(name, RoomEvent.RoomStatus.POPULATED)));
            sendBus(Configuration.NOTIFY, Serializer.pack(new Room(name, room.getTopic(), client.getUsername(), client.getId())));
            rooms.put(name, new ChatRoom(room));
            addToRoom(name, client);
        }
    }

    private void addToRoom(String name, ClientID client) {
        ChatRoom room = rooms.get(name);

//        if (client.getRoom() != null)
//            removeFromRoom(client.getRoom(), client);

        if (room != null) {
            room.add(client);
//            client.setRoom(name);
            System.out.println("Client " + client.getUsername() + " addedd to Room " + name);
        }
    }


    /**
     * Pushes a message onto the event bus for the given address.
     *
     * @param address of the bus.
     * @param data    as an object that is Serializable to JSON.
     */
    protected void sendBus(String address, Object data) {
        vertx.eventBus().send(address, data);
    }

    private void addClient(ClientID client) {
        clients.put(client.getPhoneNumber(), client);
//        loadManager.manage(clients.size());
    }

    private void removeClient(ClientID client) {
        clients.remove(client.getId());
//        loadManager.manage(clients.size());
    }

    protected ClientID getClient(String id) {
        return clients.get(id);
    }

    private void removeFromRoom(String room, ClientID client) {

        if (rooms.get(room) != null) {
            rooms.get(room).remove(client);

            client.setRoom(null);
            messageRoom(room, new UserEvent(room, client.getUsername(), false));

            if (rooms.get(room).getClients().isEmpty()) {
                rooms.remove(room);

                sendBus(Configuration.NOTIFY, Serializer.pack(new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED)));
            }
            sendBus(Configuration.NOTIFY, Serializer.pack(new UserEvent(room, client.getUsername(), false)));
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
                sendBus(client.getId(), Serializer.pack(message));
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
        System.out.println("Available clients : " + clients);
        ClientID client = clients.get(receiver);
        System.out.println("Receiving Client : " + client.toString());

        if (client != null) {
            if (message instanceof Message){
                sendBus(client.getId(), Serializer.pack(message));
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
            sendBus(Configuration.NOTIFY, topic);
    }


    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        server.close();
    }



}
