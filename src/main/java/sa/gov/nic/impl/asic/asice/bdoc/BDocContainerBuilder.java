// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice.bdoc;

import org.slf4j.LoggerFactory;
import sa.gov.nic.ContainerBuilder;
import sa.gov.nic.Container;
import org.slf4j.Logger;
import java.io.Serializable;
import sa.gov.nic.impl.asic.asice.AsicEContainerBuilder;

public class BDocContainerBuilder extends AsicEContainerBuilder implements Serializable
{
    private static final Logger logger;
    
    @Override
    protected Container createNewContainer() {
        if (this.configuration == null) {
            return new BDocContainer();
        }
        return new BDocContainer(this.configuration);
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        BDocContainerBuilder.logger.warn("BDoc containers don't support setting temp directories");
        return this;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BDocContainerBuilder.class);
    }
}
