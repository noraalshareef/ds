// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.pades;

import sa.gov.nic.ContainerOpener;
import sa.gov.nic.Container;
import sa.gov.nic.exceptions.NotYetImplementedException;
import sa.gov.nic.impl.asic.asice.AsicEContainer;
import sa.gov.nic.ContainerBuilder;

public class PadesContainerBuilder extends ContainerBuilder
{
    @Override
    protected AsicEContainer createNewContainer() {
        throw new NotYetImplementedException();
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
        return null;
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        return null;
    }
}
