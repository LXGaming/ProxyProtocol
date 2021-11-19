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

package io.github.lxgaming.proxyprotocol.forge.network;

import net.minecraft.network.PacketBuffer;

import java.util.function.IntSupplier;

public class S2CResetMessage implements IntSupplier {
    
    private int loginIndex;
    
    public static S2CResetMessage decode(PacketBuffer buffer) {
        return new S2CResetMessage();
    }
    
    public void encode(PacketBuffer buffer) {
    }
    
    @Override
    public int getAsInt() {
        return getLoginIndex();
    }
    
    public int getLoginIndex() {
        return loginIndex;
    }
    
    public void setLoginIndex(int loginIndex) {
        this.loginIndex = loginIndex;
    }
}