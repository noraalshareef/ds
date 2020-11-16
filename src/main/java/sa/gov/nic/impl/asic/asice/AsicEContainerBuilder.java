// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice;

import org.slf4j.LoggerFactory;
import sa.gov.nic.ContainerOpener;
import sa.gov.nic.Container;
import org.slf4j.Logger;
import java.io.Serializable;
import sa.gov.nic.ContainerBuilder;

public class AsicEContainerBuilder extends ContainerBuilder implements Serializable
{
    private static final Logger logger;
    
    @Override
    protected Container createNewContainer() {
        if (this.configuration == null) {
            return new AsicEContainer();
        }
        return new AsicEContainer(this.configuration);
    }
    
    @Override
    protected Container openContainerFromFile() {
        if (this.configuration == null) {
            return ContainerOpener.open(this.containerFilePath);
        }
        return ContainerOpener.open(this.containerFilePath, this.configuration);
    }
    
    @Override
    protected Container openContainerFromStream() {
        if (this.configuration == null) {
            final boolean actAsBigFilesSupportEnabled = true;
            return ContainerOpener.open(this.containerInputStream, actAsBigFilesSupportEnabled);
        }
        return ContainerOpener.open(this.containerInputStream, this.configuration);
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        AsicEContainerBuilder.logger.warn("ASiCE containers don't support setting temp directories");
        return this;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicEContainerBuilder.class);
    }
}
