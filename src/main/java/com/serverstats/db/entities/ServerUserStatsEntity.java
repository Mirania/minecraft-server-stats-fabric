package com.serverstats.db.entities;

import java.io.Serializable;

public class ServerUserStatsEntity implements Serializable {

    private Long blocksMined;

    private Long oresMined;

    private Long blocksPlaced;

    private Long kills;

    private Long deaths;

    private Long overworldTicks;

    private Long netherTicks;

    private Long aetherTicks;

    private Long endTicks;

    // ticks played since last death
    private Long aliveTicks;

    private Double distanceTraveled;

    public Long getBlocksMined() {
        if (this.blocksMined == null) this.blocksMined = 0L;
        return this.blocksMined;
    }

    public void setBlocksMined(final Long blocksMined) {
        this.blocksMined = blocksMined;
    }

    public Long getOresMined() {
        if (this.oresMined == null) this.oresMined = 0L;
        return this.oresMined;
    }

    public void setOresMined(final Long oresMined) {
        this.oresMined = oresMined;
    }

    public Long getBlocksPlaced() {
        if (this.blocksPlaced == null) this.blocksPlaced = 0L;
        return this.blocksPlaced;
    }

    public void setBlocksPlaced(final Long blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }

    public Long getKills() {
        if (this.kills == null) this.kills = 0L;
        return this.kills;
    }

    public void setKills(final Long kills) {
        this.kills = kills;
    }

    public Long getDeaths() {
        if (this.deaths == null) this.deaths = 0L;
        return this.deaths;
    }

    public void setDeaths(final Long deaths) {
        this.deaths = deaths;
    }

    public Long getOverworldTicks() {
        if (this.overworldTicks == null) this.overworldTicks = 0L;
        return this.overworldTicks;
    }

    public void setOverworldTicks(final Long overworldTicks) {
        this.overworldTicks = overworldTicks;
    }

    public Long getNetherTicks() {
        if (this.netherTicks == null) this.netherTicks = 0L;
        return this.netherTicks;
    }

    public void setNetherTicks(final Long netherTicks) {
        this.netherTicks = netherTicks;
    }

    public Long getAetherTicks() {
        if (this.aetherTicks == null) this.aetherTicks = 0L;
        return this.aetherTicks;
    }

    public void setAetherTicks(final Long aetherTicks) {
        this.aetherTicks = aetherTicks;
    }

    public Long getEndTicks() {
        if (this.endTicks == null) this.endTicks = 0L;
        return this.endTicks;
    }

    public void setEndTicks(final Long endTicks) {
        this.endTicks = endTicks;
    }

    public Long getAliveTicks() {
        if (this.aliveTicks == null) this.aliveTicks = 0L;
        return this.aliveTicks;
    }

    public void setAliveTicks(final Long aliveTicks) {
        this.aliveTicks = aliveTicks;
    }

    public Double getDistanceTraveled() {
        if (this.distanceTraveled == null) this.distanceTraveled = 0d;
        return this.distanceTraveled;
    }

    public void setDistanceTraveled(final Double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

}
