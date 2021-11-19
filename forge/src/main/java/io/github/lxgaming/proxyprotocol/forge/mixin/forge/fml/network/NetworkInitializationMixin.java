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

import io.github.lxgaming.proxyprotocol.forge.bridge.fml.network.FMLHandshakeHandlerBridge;
import io.github.lxgaming.proxyprotocol.forge.network.S2CCommandsMessage;
import io.github.lxgaming.proxyprotocol.forge.network.S2CResetMessage;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraftforge.fml.network.NetworkInitialization", remap = false)
public abstract class NetworkInitializationMixin {
    
    @Inject(
            method = "getHandshakeChannel",
            at = @At(
                    value = "RETURN"
            )
    )
    private static void onGetHandshakeChannel(CallbackInfoReturnable<SimpleChannel> callbackInfoReturnable) {
        SimpleChannel handshakeChannel = callbackInfoReturnable.getReturnValue();
        handshakeChannel.messageBuilder(S2CResetMessage.class, 98)
                .loginIndex(S2CResetMessage::getLoginIndex, S2CResetMessage::setLoginIndex)
                .decoder(S2CResetMessage::decode)
                .encoder(S2CResetMessage::encode)
                .consumer(FMLHandshakeHandler.biConsumerFor((handler, message, contextSupplier) -> {
                    ((FMLHandshakeHandlerBridge) handler).bridge$handleReset(message, contextSupplier);
                }))
                .add();
    }
    
    @Inject(
            method = "getPlayChannel",
            at = @At(
                    value = "RETURN"
            )
    )
    private static void onGetPlayChannel(CallbackInfoReturnable<SimpleChannel> callbackInfoReturnable) {
        SimpleChannel playChannel = callbackInfoReturnable.getReturnValue();
        playChannel.messageBuilder(S2CCommandsMessage.class, 4, NetworkDirection.PLAY_TO_CLIENT).
                decoder(S2CCommandsMessage::decode).
                consumer(S2CCommandsMessage::handle).
                add();
    }
}