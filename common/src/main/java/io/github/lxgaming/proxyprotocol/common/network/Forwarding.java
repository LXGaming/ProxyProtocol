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

package io.github.lxgaming.proxyprotocol.common.network;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.lxgaming.proxyprotocol.common.ProxyProtocol;
import io.netty.buffer.ByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Forwarding {
    
    private static final int SUPPORTED_FORWARDING_VERSION = 1;
    
    public static boolean checkIntegrity(ByteBuf byteBuf) {
        byte[] signature = new byte[32];
        byteBuf.readBytes(signature);
        
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), data);
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(ProxyProtocol.getInstance().getConfig().getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new AssertionError(ex);
        }
        
        int version = ProtocolUtils.readVarInt(byteBuf);
        if (version != SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + SUPPORTED_FORWARDING_VERSION);
        }
        
        return true;
    }
    
    @SuppressWarnings("UnstableApiUsage")
    public static InetAddress readAddress(ByteBuf byteBuf) {
        return InetAddresses.forString(ProtocolUtils.readString(byteBuf));
    }
    
    public static GameProfile createProfile(ByteBuf byteBuf) {
        GameProfile profile = new GameProfile(ProtocolUtils.readUUID(byteBuf), ProtocolUtils.readString(byteBuf, 16));
        readProperties(byteBuf, profile);
        return profile;
    }
    
    private static void readProperties(ByteBuf byteBuf, GameProfile profile) {
        int properties = ProtocolUtils.readVarInt(byteBuf);
        for (int index = 0; index < properties; index++) {
            String name = ProtocolUtils.readString(byteBuf);
            String value = ProtocolUtils.readString(byteBuf);
            String signature = byteBuf.readBoolean() ? ProtocolUtils.readString(byteBuf) : null;
            profile.getProperties().put(name, new Property(name, value, signature));
        }
    }
}