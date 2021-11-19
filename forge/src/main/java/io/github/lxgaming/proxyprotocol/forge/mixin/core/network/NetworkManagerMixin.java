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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.network;

import com.mojang.authlib.properties.Property;
import io.github.lxgaming.proxyprotocol.forge.bridge.network.NetworkManagerBridge;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.SocketAddress;
import java.util.UUID;

@Mixin(value = NetworkManager.class)
public abstract class NetworkManagerMixin implements NetworkManagerBridge {
    
    @Shadow
    private SocketAddress address;
    
    private UUID proxyprotocol$spoofedId;
    private Property[] proxyprotocol$spoofedProperties;
    
    @Override
    public SocketAddress bridge$getAddress() {
        return address;
    }
    
    @Override
    public void bridge$setAddress(SocketAddress address) {
        this.address = address;
    }
    
    @Override
    public UUID bridge$getSpoofedId() {
        return proxyprotocol$spoofedId;
    }
    
    @Override
    public void bridge$setSpoofedId(UUID spoofedId) {
        this.proxyprotocol$spoofedId = spoofedId;
    }
    
    @Override
    public Property[] bridge$getSpoofedProperties() {
        return proxyprotocol$spoofedProperties;
    }
    
    @Override
    public void bridge$setSpoofedProperties(Property[] spoofedProperties) {
        this.proxyprotocol$spoofedProperties = spoofedProperties;
    }
}