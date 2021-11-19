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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.network.handshake.client;

import io.github.lxgaming.proxyprotocol.common.network.ForwardingMode;
import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import io.github.lxgaming.proxyprotocol.forge.bridge.network.handshake.client.CHandshakePacketBridge;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = CHandshakePacket.class)
public abstract class CHandshakePacketMixin implements CHandshakePacketBridge {
    
    @Shadow
    private int protocolVersion;
    
    @Shadow
    private String hostName;
    
    @Shadow
    private int port;
    
    @Shadow
    private ProtocolType intention;
    
    @Shadow(remap = false)
    private String fmlVersion;
    
    /**
     * @author LX_Gaming
     * @reason Add IP Forwarding Support
     * - SpongePls (https://github.com/SpigotMC/BungeeCord/pull/1557)
     * - Waterfall (https://github.com/PaperMC/Waterfall/blob/master/BungeeCord-Patches/0012-Add-support-for-FML-with-IP-Forwarding-enabled.patch)
     */
    @Overwrite
    public void read(PacketBuffer buffer) {
        this.fmlVersion = FMLNetworkConstants.NOVERSION;
        this.protocolVersion = buffer.readVarInt();
        
        if (ForgeMod.getInstance().getConfig().getForwardingMode() == ForwardingMode.LEGACY) {
            this.hostName = buffer.readUtf(Short.MAX_VALUE);
            String[] split = this.hostName.split("\0\\|", 2);
            this.hostName = split[0];
            if (split.length == 2) {
                this.fmlVersion = NetworkHooks.getFMLVersion(split[1]);
            }
        } else {
            this.hostName = buffer.readUtf(255);
        }
        
        if (!Objects.equals(this.fmlVersion, FMLNetworkConstants.NETVERSION)) {
            this.fmlVersion = NetworkHooks.getFMLVersion(this.hostName);
            if (Objects.equals(this.fmlVersion, FMLNetworkConstants.NETVERSION)) {
                this.hostName = this.hostName.split("\0")[0];
            }
        }
        
        this.port = buffer.readUnsignedShort();
        this.intention = ProtocolType.getById(buffer.readVarInt());
    }
    
    @Override
    public String bridge$getHostName() {
        return hostName;
    }
    
    @Override
    public void bridge$setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    @Override
    public String bridge$getFMLVersion() {
        return fmlVersion;
    }
    
    @Override
    public void bridge$setFMLVersion(String fmlVersion) {
        this.fmlVersion = fmlVersion;
    }
}