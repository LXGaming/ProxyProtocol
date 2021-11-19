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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.network.handshake;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import io.github.lxgaming.proxyprotocol.common.network.ForwardingMode;
import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import io.github.lxgaming.proxyprotocol.forge.bridge.network.NetworkManagerBridge;
import io.github.lxgaming.proxyprotocol.forge.bridge.network.handshake.client.CHandshakePacketBridge;
import io.github.lxgaming.proxyprotocol.forge.mixin.forge.fml.server.ServerLifecycleHooksAccessor;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.ServerHandshakeNetHandler;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(value = ServerHandshakeNetHandler.class)
public abstract class ServerHandshakeNetHandlerMixin {
    
    private static final Gson proxyprotocol$GSON = new Gson();
    
    @Shadow
    @Final
    private NetworkManager connection;
    
    @Inject(
            method = "handleIntention",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onHandleIntention(CHandshakePacket packet, CallbackInfo callbackInfo) {
        if (!ServerLifecycleHooksAccessor.accessor$getAllowLogins().get() || packet.getIntention() != ProtocolType.LOGIN) {
            return;
        }
        
        if (ForgeMod.getInstance().getConfig().getForwardingMode() == ForwardingMode.LEGACY) {
            String hostName = ((CHandshakePacketBridge) packet).bridge$getHostName();
            String[] split = hostName.split("\0\\|", 2)[0].split("\0");
            
            if (split.length == 3 || split.length == 4) {
                ((CHandshakePacketBridge) packet).bridge$setHostName(split[0]);
                ((NetworkManagerBridge) this.connection).bridge$setAddress(new InetSocketAddress(split[1], ((InetSocketAddress) this.connection.getRemoteAddress()).getPort()));
                ((NetworkManagerBridge) this.connection).bridge$setSpoofedId(UUIDTypeAdapter.fromString(split[2]));
                
                if (split.length == 4) {
                    Property[] properties = proxyprotocol$GSON.fromJson(split[3], Property[].class);
                    for (Property property : properties) {
                        if (property.getName().equalsIgnoreCase("forgeClient")) {
                            ((CHandshakePacketBridge) packet).bridge$setFMLVersion(property.getValue());
                        }
                    }
                    
                    ((NetworkManagerBridge) this.connection).bridge$setSpoofedProperties(properties);
                }
            } else {
                this.connection.setProtocol(ProtocolType.LOGIN);
                this.connection.disconnect(new StringTextComponent("If you wish to use IP forwarding, please enable it in your proxy config as well!").withStyle(TextFormatting.RED));
            }
        }
        
        if (ForgeMod.getInstance().getConfig().isDisableTags()) {
            ((CHandshakePacketBridge) packet).bridge$setFMLVersion(FMLNetworkConstants.NOVERSION);
        }
    }
}