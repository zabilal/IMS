package com.nova.simplechat.simplechat;

/**
 * Created by Raji Zakariyya
 *
 * Logs the number of Chat users to Service
 */
public class LogUserCount {


    private String name = Configuration.REGISTER_NAME;
    private String type = "logging.users";
    private Integer count;

    public LogUserCount() {
    }

    public LogUserCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
