package com.nova.simplechat.simplechat;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Raji Zakariyya
 * <p>
 * General transfer object, used to access the header of unknown-type messages.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Packet {
    private Header header;

    public Packet() {
    }

    public String getAction() {
        return header.getAction();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}

