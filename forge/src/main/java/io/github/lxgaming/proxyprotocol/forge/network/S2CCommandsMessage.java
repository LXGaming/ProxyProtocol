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

import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCommandListPacket;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class S2CCommandsMessage {
    
    private final RootCommandNode<ISuggestionProvider> root;
    
    private S2CCommandsMessage(RootCommandNode<ISuggestionProvider> root) {
        this.root = root;
    }
    
    public static S2CCommandsMessage decode(PacketBuffer buf) {
        try {
            SCommandListPacket packet = new SCommandListPacket();
            packet.read(buf);
            return new S2CCommandsMessage(packet.getRoot());
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static void handle(S2CCommandsMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                return;
            }
            
            msg.getRoot().getChildren().forEach(connection.getCommands().getRoot()::addChild);
        });
    }
    
    public RootCommandNode<ISuggestionProvider> getRoot() {
        return root;
    }
}