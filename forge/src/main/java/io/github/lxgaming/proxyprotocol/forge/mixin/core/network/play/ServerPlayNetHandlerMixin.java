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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.network.play;

import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.STagsListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin {
    
    @Inject(
            method = "send(Lnet/minecraft/network/IPacket;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onSend(IPacket<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo callbackInfo) {
        if (packet instanceof STagsListPacket && ForgeMod.getInstance().getConfig().isDisableTags()) {
            callbackInfo.cancel();
        }
    }
}