package com.nova.simplechat.simplechat;

    /**
     * Created by Raji Zakariyya
     * <p>
     * Handles incoming messages from clients.
     */
    enum WiseAppMessageHandler {
        MESSAGE() {
            @Override
            public void invoke(Parameters params) {
                Message message = (Message) Serializer.unpack(params.data, Message.class);

                message.setSender(params.client.getPhoneNumber());//getUsername()); //Original
//                message.setRoom(params.client.getRoom());

                System.out.println(params.client.getId() + " sent a Message to Room : " + message.getRoom() );
                if(message.getRoom() != null){
                    params.handler.messageRoom(message.getRoom(), message, message.getSender());
                    params.handler.sendBus(Configuration.UPSTREAM, Serializer.pack(message));
                }

                else {
                    params.handler.messageClient(message.getReceiver(), message);
                    params.handler.sendBus(Configuration.UPSTREAM, Serializer.pack(message));
                }

            }
        },

        JOIN() {
            @Override
            public void invoke(Parameters params) {
                Join join = (Join) Serializer.unpack(params.data, Join.class);

                System.out.println(params.client.getId() + " is joining room " + join.getRoom() );

                if (!join.getRoom().equals(params.client.getRoom()))    {
                    params.handler.joinRoom(params.client, new Room().setRoom(join.getRoom()));
                    System.out.println(params.client.getId() + " has join room " + join.getRoom() );
                }
                else {
                    params.handler.sendBus(params.client.getId(), Serializer.pack(new Room().setErrorInsideAlready(true)));
                }
            }
        },

        TOPIC() {
            @Override
            public void invoke(Parameters params) {
                Topic topic = (Topic) Serializer.unpack(params.data, Topic.class);
                params.handler.trySetTopic(topic.setRoom(params.client.getRoom()), params.client);
            }
        },

        SERVERS() {
            @Override
            public void invoke(Parameters params) {
                params.handler.sendBus(Configuration.UPSTREAM, new ServerList(params.client.getId()));
            }
        },

        ONLINE() {
            @Override
            public void invoke(Parameters params) {
                OnlineContact onlineContact = (OnlineContact) Serializer.unpack(params.data, OnlineContact.class);
                params.handler.checkOnlineStatus(onlineContact.getContacts(), onlineContact.getSender());
            }
        },

        GETROOMS() {
            @Override
            public void invoke(Parameters params) {
                RoomFinder roomChecker = (RoomFinder) Serializer.unpack(params.data, RoomFinder.class);
                params.handler.fetchMyRooms(roomChecker);
            }
        };

        public abstract void invoke(Parameters params);
}
