package com.serverstats.db.entities;

import java.io.Serializable;

public class ServerChatMessageEntity implements Serializable {

    private Long timestampMs;

    private String username;

    private String content;

    public void setTimestampMs(final Long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public Long getTimestampMs() {
        return this.timestampMs;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

}
