package com.serverstats.db.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServerGlobalStatsEntity implements Serializable {

    private Long uptimeTicks;

    private Long playersOnline;

    private List<ServerChatMessageEntity> chatMessages;

    public void setUptimeTicks(final Long uptimeTicks) {
        this.uptimeTicks = uptimeTicks;
    }

    public Long getUptimeTicks() {
        if (this.uptimeTicks == null) this.uptimeTicks = 0L;
        return this.uptimeTicks;
    }

    public void setPlayersOnline(final Long playersOnline) {
        this.playersOnline = playersOnline;
    }

    public Long getPlayersOnline() {
        if (this.playersOnline == null) this.playersOnline = 0L;
        return this.playersOnline;
    }

    public void setChatMessages(final List<ServerChatMessageEntity> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public List<ServerChatMessageEntity> getChatMessages() {
        if (this.chatMessages == null) this.chatMessages = new ArrayList<>();
        return this.chatMessages;
    }

}
