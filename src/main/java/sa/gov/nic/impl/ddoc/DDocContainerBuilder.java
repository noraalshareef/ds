// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.Container;
import sa.gov.nic.ContainerBuilder;

public class DDocContainerBuilder extends ContainerBuilder
{
    private String temporaryDirectoryPath;
    
    @Override
    protected Container createNewContainer() {
        if (this.configuration == null) {
            return new DDocContainer();
        }
        return new DDocContainer(this.configuration);
    }
    
    @Override
    protected Container openContainerFromFile() {
        final DDocOpener opener = this.createDocOpener();
        if (this.configuration == null) {
            return opener.open(this.containerFilePath);
        }
        return opener.open(this.containerFilePath, this.configuration);
    }
    
    @Override
    protected Container openContainerFromStream() {
        final DDocOpener opener = this.createDocOpener();
        if (this.configuration == null) {
            return opener.open(this.containerInputStream);
        }
        return opener.open(this.containerInputStream, this.configuration);
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        this.temporaryDirectoryPath = temporaryDirectoryPath;
        return this;
    }
    
    private DDocOpener createDocOpener() {
        final DDocOpener opener = new DDocOpener();
        if (StringUtils.isNotBlank((CharSequence)this.temporaryDirectoryPath)) {
            opener.useTemporaryDirectoryPath(this.temporaryDirectoryPath);
        }
        return opener;
    }
}
