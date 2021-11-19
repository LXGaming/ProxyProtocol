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

package io.github.lxgaming.proxyprotocol.common;

import io.github.lxgaming.proxyprotocol.common.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ProxyProtocol {
    
    public static final String ID = "proxyprotocol";
    public static final String NAME = "ProxyProtocol";
    public static final String VERSION = "@version@";
    
    private static ProxyProtocol instance;
    private final Logger logger;
    private final Config config;
    
    public ProxyProtocol(Config config) {
        instance = this;
        this.logger = LogManager.getLogger(ProxyProtocol.NAME);
        this.config = config;
    }
    
    public static ProxyProtocol getInstance() {
        return instance;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Config getConfig() {
        return config;
    }
}