// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl;

import org.slf4j.LoggerFactory;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;

public class ConfigurationSingeltonHolder
{
    private static final Logger logger;
    private static volatile Configuration configuration;
    
    public static Configuration getInstance() {
        if (ConfigurationSingeltonHolder.configuration == null) {
            synchronized (ConfigurationSingeltonHolder.class) {
                if (ConfigurationSingeltonHolder.configuration == null) {
                    ConfigurationSingeltonHolder.logger.info("Creating a new configuration instance");
                    ConfigurationSingeltonHolder.configuration = new Configuration();
                }
            }
        }
        else {
            ConfigurationSingeltonHolder.logger.info("Using existing configuration instance");
        }
        return ConfigurationSingeltonHolder.configuration;
    }
    
    protected static void reset() {
        ConfigurationSingeltonHolder.configuration = null;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ConfigurationSingeltonHolder.class);
    }
}
