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

package io.github.lxgaming.proxyprotocol.forge.configuration;

import io.github.lxgaming.proxyprotocol.common.ProxyProtocol;
import io.github.lxgaming.proxyprotocol.common.configuration.Config;
import io.github.lxgaming.proxyprotocol.common.network.ForwardingMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigImpl implements Config {
    
    private final ForgeConfigSpec forgeConfigSpec;
    private final ForgeConfigSpec.ConfigValue<Boolean> disableTags;
    private final ForgeConfigSpec.ConfigValue<String> forwardingChannel;
    private final ForgeConfigSpec.ConfigValue<ForwardingMode> forwardingMode;
    private final ForgeConfigSpec.ConfigValue<String> secret;
    
    public ConfigImpl() {
        this(new ForgeConfigSpec.Builder());
    }
    
    private ConfigImpl(ForgeConfigSpec.Builder builder) {
        builder.comment("General configuration settings")
                .push("general");
        
        this.disableTags = builder
                .comment("If 'true', disables tag support to allow modded clients to connect to the server like a vanilla client")
                .translation(ProxyProtocol.ID + ".config.disable-tags")
                .define("disable-tags", false);
        
        this.forwardingChannel = builder
                .comment("Configure the forwarding channel used by the upstream proxy")
                .translation(ProxyProtocol.ID + ".config.forwarding-channel")
                .define("forwarding-channel", "velocity:player_info");
        
        this.forwardingMode = builder
                .comment("Configure the forwarding mode used by the upstream proxy")
                .translation(ProxyProtocol.ID + ".config.forwarding-mode")
                .defineEnum("forwarding-mode", ForwardingMode.NONE, ForwardingMode.values());
        
        this.secret = builder
                .comment("Configure the forwarding secret used by the upstream proxy")
                .translation(ProxyProtocol.ID + ".config.secret")
                .define("secret", "");
        
        builder.pop();
        
        this.forgeConfigSpec = builder.build();
    }
    
    public ForgeConfigSpec getForgeConfigSpec() {
        return forgeConfigSpec;
    }
    
    @Override
    public String getForwardingChannel() {
        return forwardingChannel.get();
    }
    
    @Override
    public ForwardingMode getForwardingMode() {
        return forwardingMode.get();
    }
    
    @Override
    public String getSecret() {
        return secret.get();
    }
    
    @Override
    public boolean isDisableTags() {
        return disableTags.get();
    }
}