package com.nova.simplechat.simplechat;


/**
 * Created by Raji Zakakriyya
 * <p>
 * Handles events received from the connector backend service.
 */
enum EventHandler {
    MESSAGE() {
        @Override
        public void invoke(Event event) {
            Message message = (Message) Serializer.unpack(event.data, Message.class);
            event.handler.messageRoom(message.getRoom(), message);
        }
    },

    ROOM() {
        @Override
        public void invoke(Event event) {
            Room room = (Room) Serializer.unpack(event.data, Room.class);

            event.handler.getRooms().put(room.getRoom(), new ChatRoom(room));
            event.handler.joinRoom(event.handler.getClient(event.actor), room);
        }
    },

    JOIN() {
        @Override
        public void invoke(Event event) {
            UserEvent userEvent = (UserEvent) Serializer.unpack(event.data, UserEvent.class);
            event.handler.messageRoom(userEvent.getRoom(), userEvent);
        }
    },

    TOPIC() {
        @Override
        public void invoke(Event event) {
            Topic topic = (Topic) Serializer.unpack(event.data, Topic.class);
            event.handler.setRoomTopic(topic, false);
        }
    };

//    SERVERS() {
//        @Override
//        public void invoke(Event event) {
//            ServerList servers = (ServerList) Serializer.unpack(event.data, ServerList.class);
//            event.handler.sendBus(event.handler.getClient(event.actor).getId(), servers);
//        }
//    };

    public abstract void invoke(Event event);
}
