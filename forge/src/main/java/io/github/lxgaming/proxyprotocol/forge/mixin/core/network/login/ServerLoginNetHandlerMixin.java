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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.network.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.lxgaming.proxyprotocol.common.network.Forwarding;
import io.github.lxgaming.proxyprotocol.common.network.ForwardingMode;
import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import io.github.lxgaming.proxyprotocol.forge.bridge.network.NetworkManagerBridge;
import io.github.lxgaming.proxyprotocol.forge.mixin.core.network.login.client.CCustomPayloadLoginPacketAccessor;
import io.github.lxgaming.proxyprotocol.forge.mixin.core.network.login.server.SCustomPayloadLoginPacketAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(value = ServerLoginNetHandler.class)
public abstract class ServerLoginNetHandlerMixin {
    
    @Shadow
    @Final
    private MinecraftServer server;
    
    @Shadow
    @Final
    public NetworkManager connection;
    
    @Shadow
    private GameProfile gameProfile;
    
    @Shadow
    public abstract void disconnect(ITextComponent p_194026_1_);
    
    @Shadow
    public abstract void handleHello(CLoginStartPacket p_147316_1_);
    
    private int proxyprotocol$transactionId = -1;
    
    @Inject(
            method = "handleHello",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/network/login/ServerLoginNetHandler;gameProfile:Lcom/mojang/authlib/GameProfile;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    @SuppressWarnings("ConstantConditions")
    private void onHandleHello(CallbackInfo callbackInfo) {
        ForwardingMode forwardingMode = ForgeMod.getInstance().getConfig().getForwardingMode();
        if (this.server.usesAuthentication() || forwardingMode == ForwardingMode.NONE) {
            return;
        }
        
        if (forwardingMode == ForwardingMode.LEGACY) {
            UUID id;
            if (((NetworkManagerBridge) this.connection).bridge$getSpoofedId() != null) {
                id = ((NetworkManagerBridge) this.connection).bridge$getSpoofedId();
            } else {
                id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.gameProfile.getName()).getBytes(StandardCharsets.UTF_8));
            }
            
            this.gameProfile = new GameProfile(id, this.gameProfile.getName());
            
            if (((NetworkManagerBridge) this.connection).bridge$getSpoofedProperties() != null) {
                for (Property property : ((NetworkManagerBridge) this.connection).bridge$getSpoofedProperties()) {
                    this.gameProfile.getProperties().put(property.getName(), property);
                }
            }
            
            return;
        }
        
        if (forwardingMode == ForwardingMode.MODERN) {
            if (this.proxyprotocol$transactionId != -1) {
                return;
            }
            
            this.proxyprotocol$transactionId = ThreadLocalRandom.current().nextInt();
            
            SCustomPayloadLoginPacket packet = new SCustomPayloadLoginPacket();
            ((SCustomPayloadLoginPacketAccessor) packet).accessor$setTransactionId(proxyprotocol$transactionId);
            ((SCustomPayloadLoginPacketAccessor) packet).accessor$setIdentifier(new ResourceLocation(ForgeMod.getInstance().getConfig().getForwardingChannel()));
            ((SCustomPayloadLoginPacketAccessor) packet).accessor$setData(new PacketBuffer(Unpooled.EMPTY_BUFFER));
            
            this.connection.send(packet);
            callbackInfo.cancel();
        }
    }
    
    @Inject(
            method = "handleCustomQueryPacket",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onHandleCustomQueryPacket(CCustomPayloadLoginPacket packet, CallbackInfo callbackInfo) {
        if (ForgeMod.getInstance().getConfig().getForwardingMode() != ForwardingMode.MODERN) {
            return;
        }
        
        int transactionId = ((CCustomPayloadLoginPacketAccessor) packet).accessor$getTransactionId();
        if (transactionId != this.proxyprotocol$transactionId) {
            return;
        }
        
        PacketBuffer packetBuffer = ((CCustomPayloadLoginPacketAccessor) packet).accessor$getData();
        if (packetBuffer == null) {
            this.disconnect(new StringTextComponent("This server requires you to connect with Velocity.").withStyle(TextFormatting.RED));
            callbackInfo.cancel();
            return;
        }
        
        if (!Forwarding.checkIntegrity(packetBuffer)) {
            this.disconnect(new StringTextComponent("Unable to verify player details").withStyle(TextFormatting.RED));
            callbackInfo.cancel();
            return;
        }
        
        ((NetworkManagerBridge) this.connection).bridge$setAddress(new InetSocketAddress(
                Forwarding.readAddress(packetBuffer),
                ((InetSocketAddress) this.connection.getRemoteAddress()).getPort()
        ));
        
        this.gameProfile = Forwarding.createProfile(packetBuffer);
        
        handleHello(new CLoginStartPacket(gameProfile));
        callbackInfo.cancel();
    }
    
    // TODO Make this an optional Mixin
//    @Dynamic("Added by Spigot")
//    @Inject(
//            method = "initUUID",
//            at = @At(
//                    value = "HEAD"
//            ),
//            remap = false,
//            cancellable = true
//    )
//    private void onInitUUID(CallbackInfo callbackInfo) {
//        callbackInfo.cancel();
//    }
}