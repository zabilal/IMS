package com.nova.simplechat.simplechat;

import java.util.Random;

/**
 * Created by Raji Zakariyya
 * <p>
 * Mapping the bus channels to constants.
 */

public class Configuration {

    public static final String REGISTER_NAME = "vertx.server." + new Random().nextInt(999);
    public final static String SERVER_ROOM = "DEVELOPER ROOM";
    public final static String DEFAULT_ROOM = "Public";
    public final static String SERVER_NAME = "VERT.Y";
    public final static String SERVER_TOPIC = "WiseAppServer Development";
    public final static Integer LISTEN_PORT = 8080;//4000 + new Random().nextInt(2999); //dynamic port change on server restart

    public static final int LOGGER_PORT = 5454;
    public static final String BUS_LOGGER = "logging.upload";
    public static final int LOG_INTERVAL = 1000;

    // maximum number of users before the server emits a FULL message.
    public static final Integer LOAD_MAX_USERS = 40;

    // the number of users which has too leave before advertising READY.
    public static final Integer LOAD_DELTA_BUFFER = 30;

    public static final String NOTIFY = "notify";
    public static final String EVENT = "event";

    // configuration for rooms.
    public static final int MAX_HISTORY = 50;


}
