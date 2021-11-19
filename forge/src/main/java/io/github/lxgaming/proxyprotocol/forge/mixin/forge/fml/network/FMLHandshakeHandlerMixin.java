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
import io.github.lxgaming.proxyprotocol.forge.network.S2CResetMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(value = FMLHandshakeHandler.class, remap = false)
public abstract class FMLHandshakeHandlerMixin implements FMLHandshakeHandlerBridge {
    
    @Shadow
    @Final
    static Marker FMLHSMARKER;
    
    @Shadow
    @Final
    private static Logger LOGGER;
    
    @Override
    public void bridge$handleReset(S2CResetMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkManager networkManager = context.getNetworkManager();
        
        if (context.getDirection() != NetworkDirection.LOGIN_TO_CLIENT && context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            networkManager.disconnect(new StringTextComponent("Illegal packet received, terminating connection"));
            throw new IllegalStateException("Invalid packet received, aborting connection");
        }
        
        LOGGER.debug(FMLHSMARKER, "Received reset from server");
        CompletableFuture<Void> future = context.enqueueWork(() -> {
            LOGGER.debug(FMLHSMARKER, "Clearing");
            
            // Preserve
            ServerData serverData = Minecraft.getInstance().getCurrentServer();
            
            // Clear
            if (Minecraft.getInstance().level == null) {
                // Ensure the GameData is reverted in case the client is reset during the handshake.
                GameData.revertToFrozen();
            }
            
            Minecraft.getInstance().clearLevel(new DirtMessageScreen(new TranslationTextComponent("connect.negotiating")));
            
            // Restore
            Minecraft.getInstance().setCurrentServer(serverData);
        });
        
        LOGGER.debug(FMLHSMARKER, "Waiting for clear to complete");
        try {
            future.get();
        } catch (Exception ex) {
            LOGGER.error(FMLHSMARKER, "Failed to clear, closing connection");
            networkManager.disconnect(new StringTextComponent("Failed to clear, closing connection"));
            return;
        }
        
        LOGGER.debug("Clear complete, continuing reset");
        NetworkHooks.registerClientLoginChannel(networkManager);
        networkManager.setProtocol(ProtocolType.LOGIN);
        networkManager.setListener(new ClientLoginNetHandler(
                networkManager,
                Minecraft.getInstance(),
                null,
                (statusMessage) -> {
                }
        ));
        
        context.setPacketHandled(true);
        FMLNetworkConstantsAccessor.accessor$getHandshakeChannel().reply(
                new FMLHandshakeMessages.C2SAcknowledge(),
                NetworkEvent_ContextAccessor.invoker$new(networkManager, NetworkDirection.LOGIN_TO_CLIENT, 98)
        );
        
        LOGGER.debug("Reset complete");
    }
}