/*
 * Copyright 2021 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.proxyprotocol.forge.mixin.forge.fml.network;

import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraftforge.fml.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkHooks.class, remap = false)
public abstract class NetworkHooksMixin {
    
    @Inject(
            method = "syncCustomTagTypes(Lnet/minecraft/tags/ITagCollectionSupplier;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void onSyncCustomTagTypes(ITagCollectionSupplier supplier, CallbackInfo callbackInfo) {
        if (ForgeMod.getInstance().getConfig().isDisableTags()) {
            callbackInfo.cancel();
        }
    }
    
    @Inject(
            method = "syncCustomTagTypes(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/tags/ITagCollectionSupplier;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void onSyncCustomTagTypes(ServerPlayerEntity player, ITagCollectionSupplier supplier, CallbackInfo callbackInfo) {
        if (ForgeMod.getInstance().getConfig().isDisableTags()) {
            callbackInfo.cancel();
        }
    }
}