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
                message.setSender(params.client.getUsername());
                message.setRoom(params.client.getRoom());

                params.handler.messageRoom(params.client.getRoom(), message);
                params.handler.sendBus(Configuration.NOTIFY, Serializer.pack(message));
            }
        },

        JOIN() {
            @Override
            public void invoke(Parameters params) {
                Join join = (Join) Serializer.unpack(params.data, Join.class);

                if (!join.getRoom().equals(params.client.getRoom()))
                    params.handler.joinRoom(params.client, new Room().setRoom(join.getRoom()));
                else {
                    params.handler.sendBus(params.client.getId(), new Room().setErrorInsideAlready(true));
                }
            }
        },

        TOPIC() {
            @Override
            public void invoke(Parameters params) {
                Topic topic = (Topic) Serializer.unpack(params.data, Topic.class);
                params.handler.trySetTopic(topic.setRoom(params.client.getRoom()), params.client);
            }
        };


        public abstract void invoke(Parameters params);
}
