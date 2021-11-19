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

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ProtocolUtils {
    
    private static final int DEFAULT_MAX_LENGTH = 65536; // 64KiB
    
    public static int readVarInt(ByteBuf byteBuf) {
        int read = readVarIntSafely(byteBuf);
        if (read == Integer.MIN_VALUE) {
            throw new IllegalStateException("VarInt too big");
        }
        
        return read;
    }
    
    public static int readVarIntSafely(ByteBuf byteBuf) {
        int value = 0;
        int maxLength = Math.min(5, byteBuf.readableBytes());
        for (int length = 0; length < maxLength; length++) {
            int read = byteBuf.readByte();
            value |= (read & 0x7F) << length * 7;
            if ((read & 0x80) != 128) {
                return value;
            }
        }
        
        return Integer.MIN_VALUE;
    }
    
    public static String readString(ByteBuf byteBuf) {
        return readString(byteBuf, DEFAULT_MAX_LENGTH);
    }
    
    public static String readString(ByteBuf byteBuf, int maxLength) {
        int length = readVarInt(byteBuf);
        return readString(byteBuf, length, maxLength);
    }
    
    public static String readString(ByteBuf byteBuf, int length, int maxLength) {
        if (length < 0) {
            throw new IllegalArgumentException(String.format("Got a negative-length string (%s)", length));
        }
        
        if (length > maxLength * 4) {
            throw new IllegalArgumentException(String.format("Bad string size (got %s, maximum is %s)", length, maxLength));
        }
        
        if (!byteBuf.isReadable(length)) {
            throw new IllegalStateException(String.format("Trying to read a string that is too long (wanted %s, only have %s)", length, byteBuf.readableBytes()));
        }
        
        String string = byteBuf.toString(byteBuf.readerIndex(), length, StandardCharsets.UTF_8);
        byteBuf.skipBytes(length);
        
        if (string.length() > maxLength) {
            throw new IllegalStateException(String.format("Got a too-long string (got %s, max %s)", string.length(), maxLength));
        }
        
        return string;
    }
    
    public static UUID readUUID(ByteBuf byteBuf) {
        long mostSigBits = byteBuf.readLong();
        long leastSigBits = byteBuf.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }
}