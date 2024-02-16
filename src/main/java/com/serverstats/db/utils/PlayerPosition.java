package com.serverstats.db.utils;

import net.minecraft.util.math.Vec3d;

public class PlayerPosition {

    private Vec3d coords;

    private String dimension;

    public PlayerPosition(final Vec3d coords, final String dimension) {
        this.coords = coords;
        this.dimension = dimension;
    }

    public double getX() {
        return this.coords.getX();
    }

    public double getY() {
        return this.coords.getY();
    }

    public double getZ() {
        return this.coords.getZ();
    }

    public String getDimension() {
        return this.dimension;
    }

}
