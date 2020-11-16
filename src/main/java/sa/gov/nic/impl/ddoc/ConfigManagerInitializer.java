// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.slf4j.LoggerFactory;
import java.util.Hashtable;
import ee.sk.utils.ConfigManager;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import java.io.Serializable;

public class ConfigManagerInitializer implements Serializable
{
    private static final Logger logger;
    protected static boolean configManagerInitialized;
    
    public void initConfigManager(final Configuration configuration) {
        if (!ConfigManagerInitializer.configManagerInitialized) {
            this.initializeJDigidocConfigManager(configuration);
        }
        else {
            ConfigManagerInitializer.logger.debug("Skipping DDoc configuration manager initialization");
        }
    }
    
    public static synchronized void forceInitConfigManager(final Configuration configuration) {
        ConfigManagerInitializer.logger.info("Initializing DDoc configuration manager");
        ConfigManager.init((Hashtable)configuration.getJDigiDocConfiguration());
        ConfigManager.addProvider();
        ConfigManagerInitializer.configManagerInitialized = true;
    }
    
    public static boolean isConfigManagerInitialized() {
        return ConfigManagerInitializer.configManagerInitialized;
    }
    
    protected synchronized void initializeJDigidocConfigManager(final Configuration configuration) {
        if (!ConfigManagerInitializer.configManagerInitialized) {
            forceInitConfigManager(configuration);
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ConfigManagerInitializer.class);
        ConfigManagerInitializer.configManagerInitialized = false;
    }
}
