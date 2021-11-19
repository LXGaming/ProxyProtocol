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

package io.github.lxgaming.proxyprotocol.forge.bridge.fml.network;

import io.github.lxgaming.proxyprotocol.forge.network.S2CResetMessage;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface FMLHandshakeHandlerBridge {
    
    void bridge$handleReset(S2CResetMessage message, Supplier<NetworkEvent.Context> contextSupplier);
}