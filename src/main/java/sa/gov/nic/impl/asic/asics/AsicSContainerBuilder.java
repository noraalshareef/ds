// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asics;

import org.slf4j.LoggerFactory;
import sa.gov.nic.Container;
import org.slf4j.Logger;
import java.io.Serializable;
import sa.gov.nic.ContainerBuilder;

public class AsicSContainerBuilder extends ContainerBuilder implements Serializable
{
    private static final Logger logger;
    
    @Override
    protected Container createNewContainer() {
        if (this.configuration == null) {
            return new AsicSContainer();
        }
        return new AsicSContainer(this.configuration);
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        AsicSContainerBuilder.logger.warn("BDoc containers don't support setting temp directories");
        return this;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicSContainerBuilder.class);
    }
}
