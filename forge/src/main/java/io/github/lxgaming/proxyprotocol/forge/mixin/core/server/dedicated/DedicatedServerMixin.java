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

package io.github.lxgaming.proxyprotocol.forge.mixin.core.server.dedicated;

import io.github.lxgaming.proxyprotocol.common.network.ForwardingMode;
import io.github.lxgaming.proxyprotocol.forge.ForgeMod;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DedicatedServer.class)
public abstract class DedicatedServerMixin {
    
    @Redirect(
            method = "initServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/dedicated/DedicatedServer;setUsesAuthentication(Z)V"
            )
    )
    private void onSetUsesAuthentication(DedicatedServer dedicatedServer, boolean value) {
        if (ForgeMod.getInstance().getConfig().getForwardingMode() != ForwardingMode.NONE) {
            dedicatedServer.setUsesAuthentication(false);
        } else {
            dedicatedServer.setUsesAuthentication(value);
        }
    }
    
    @Inject(
            method = "getCompressionThreshold",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onGetCompressionThreshold(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (ForgeMod.getInstance().getConfig().getForwardingMode() != ForwardingMode.NONE) {
            callbackInfoReturnable.setReturnValue(-1);
        }
    }
}