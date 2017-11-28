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

    private HttpServer server;


    protected Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        messageHandler.put(Message.ACTION, WiseAppMessageHandler.MESSAGE);
        messageHandler.put(Join.ACTION, WiseAppMessageHandler.JOIN);
        messageHandler.put(Topic.ACTION, WiseAppMessageHandler.TOPIC);

        startServer();
//        startEventListener();
        startUserCountLog();
    }

    private void startServer() {

        server = vertx.createHttpServer().websocketHandler(event -> {
                final ClientID client = new ClientID(event.textHandlerID());
                client.setUsername(event.textHandlerID());

                event.handler(data -> {
                    System.out.println("Data from client : " + data.toString());
                    Packet packet = (Packet) (Serializer.unpack(data.toString(), Packet.class));
                    messageHandler.get(packet.getAction()).invoke(new Parameters(data.toString(), event, client, this));

                });

                event.closeHandler(close -> {
                    removeFromRoom(client.getRoom(), client);
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
     * the room does not exist in this context: the server is queried for the room
     * and returns the existing, or creates a new. This method is used as a callback for completion.
     *
     * @param client the client joining the room.
     * @param room   the room to be joined.
     */
    protected void joinRoom(ClientID client, Room room) {
        String name = room.getRoom();

        if (rooms.containsKey(name)) {
            sendBus(client.getId(), new Room(rooms.get(name).getSettings(), room.getCreated()));
            messageRoom(room.getRoom(), new UserEvent(room.getRoom(), client.getUsername(), true));
            sendBus(Configuration.NOTIFY, new UserEvent(room.getRoom(), client.getUsername(), true));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.NOTIFY, Serializer.pack(new RoomEvent(name, RoomEvent.RoomStatus.POPULATED)));
            sendBus(Configuration.NOTIFY, Serializer.pack(new Room(name, "/topic <string>", client.getUsername(), client.getId())));
        }
    }

    private void addToRoom(String name, ClientID client) {
        ChatRoom room = rooms.get(name);

        if (client.getRoom() != null)
            removeFromRoom(client.getRoom(), client);

        if (room != null) {
            room.add(client);
            client.setRoom(name);
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
        clients.put(client.getId(), client);
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

                sendBus(Configuration.NOTIFY, new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED));
            }
            sendBus(Configuration.NOTIFY, new UserEvent(room, client.getUsername(), false));
        }
    }


    /**
     * Broadcast a message to all users within the same room.
     *
     * @param name    of the room in which the message should be sent.
     * @param message which should be sent.
     */
    protected void messageRoom(String name, Object message) {
        ChatRoom room = rooms.get(name);

        if (room != null) {
            if (message instanceof Message)
                room.addHistory((Message) message);

            for (ClientID client : room.getClients().values()) {
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
