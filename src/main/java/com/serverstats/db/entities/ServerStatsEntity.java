package com.serverstats.db.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;

public class ServerStatsEntity implements Serializable {

    private ServerGlobalStatsEntity global;

    // [username, stats]
    private Map<String, ServerUserStatsEntity> users;

    public void setGlobal(final ServerGlobalStatsEntity global) {
        this.global = global;
    }

    public ServerGlobalStatsEntity getGlobal() {
        if (this.global == null) this.global = new ServerGlobalStatsEntity();
        return this.global;
    }

    public void setUsers(final Map<String, ServerUserStatsEntity> users) {
        this.users = users;
    }

    public Map<String, ServerUserStatsEntity> getUsers() {
        if (this.users == null) this.users = new HashMap<>();
        return this.users;
    }

    public ServerUserStatsEntity getUser(final PlayerEntity user) {
        final String name = user.getEntityName();
        if (!getUsers().containsKey(name)) getUsers().put(name, new ServerUserStatsEntity());
        return getUsers().get(name);
    }

}
