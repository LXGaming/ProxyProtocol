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

package io.github.lxgaming.proxyprotocol.forge.mixin.forge.network;

import net.minecraft.network.NetworkManager;
import net.minecraftforge.network.NetworkFilters;
import net.minecraftforge.network.VanillaPacketFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = NetworkFilters.class, remap = false)
public abstract class NetworkFiltersMixin {
    
    @Inject(
            method = "lambda$injectIfNecessary$1",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void onInjectIfNecessary(NetworkManager networkManager, String key, Function<NetworkManager, VanillaPacketFilter> filterFactory, CallbackInfo callbackInfo) {
        if (networkManager.channel().pipeline().get(key) != null) {
            callbackInfo.cancel();
        }
    }
}