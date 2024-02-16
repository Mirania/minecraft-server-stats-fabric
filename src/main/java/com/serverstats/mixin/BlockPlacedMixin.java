package com.serverstats.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.serverstats.db.DatabaseWrapper;

@Mixin(BlockItem.class)
public class BlockPlacedMixin {

    @Inject(method="postPlacement(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)Z", at=@At("HEAD"))
    private void init(final BlockPos pos, final World world, final PlayerEntity player, final ItemStack stack,
            final BlockState state, final CallbackInfoReturnable<Boolean> cir) {

        if (player instanceof ServerPlayerEntity) {
            final var userStats = DatabaseWrapper.getInMemoryData().getUser(player);
            userStats.setBlocksPlaced(userStats.getBlocksPlaced() + 1);
        }
    }
}