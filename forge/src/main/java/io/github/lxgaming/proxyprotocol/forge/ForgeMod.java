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

package io.github.lxgaming.proxyprotocol.forge;

import io.github.lxgaming.proxyprotocol.common.ProxyProtocol;
import io.github.lxgaming.proxyprotocol.forge.configuration.ConfigImpl;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;

import java.lang.reflect.Field;

@Mod(value = ProxyProtocol.ID)
public class ForgeMod extends ProxyProtocol {
    
    public ForgeMod() {
        super(new ConfigImpl());
        
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, getConfig().getForgeConfigSpec());
        
        StartupMessageManager.addModMessage(String.format("%s v%s Initialized", ProxyProtocol.NAME, ProxyProtocol.VERSION));
        getLogger().info("{} v{} Initialized", ProxyProtocol.NAME, ProxyProtocol.VERSION);
    }
    
    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        Class<?> spigotConfigClass;
        try {
            spigotConfigClass = Class.forName("org.spigotmc.SpigotConfig");
            Field bungeeField = spigotConfigClass.getField("bungee");
            if (bungeeField.getBoolean(null)) {
                bungeeField.setBoolean(null, false);
                getLogger().warn("------------------------- WARNING -------------------------");
                getLogger().warn("Spigot BungeeCord support has been disabled, as it is incompatible with ProxyProtocol");
                getLogger().warn("------------------------- WARNING -------------------------");
            }
        } catch (Throwable throwable) {
            // no-op
        }
    }
    
    public static ForgeMod getInstance() {
        return (ForgeMod) ProxyProtocol.getInstance();
    }
    
    @Override
    public ConfigImpl getConfig() {
        return (ConfigImpl) super.getConfig();
    }
}