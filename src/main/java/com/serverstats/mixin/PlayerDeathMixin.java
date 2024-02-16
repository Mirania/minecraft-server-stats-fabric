package com.serverstats.mixin;

import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.serverstats.db.DatabaseWrapper;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {

    @Inject(method="onDeath", at=@At("TAIL"))
    private void init(final CallbackInfo info) {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        final var userStats = DatabaseWrapper.getInMemoryData().getUser(player);

        var deaths = userStats.getDeaths();
        userStats.setDeaths(++deaths);
        userStats.setAliveTicks(0L);
        DatabaseWrapper.postUserStatToDatabase(player, "deaths", deaths);
        DatabaseWrapper.postUserStatToDatabase(player, "aliveTicks", 0L);

        DatabaseWrapper.setLastKnownPosition(player, null);
    }
}