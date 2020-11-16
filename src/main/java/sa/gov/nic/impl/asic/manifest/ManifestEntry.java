// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.manifest;

import org.slf4j.LoggerFactory;
import java.util.Objects;
import org.slf4j.Logger;
import java.io.Serializable;

public final class ManifestEntry implements Serializable
{
    private static final Logger logger;
    private String fileName;
    private String mimeType;
    
    public ManifestEntry(final String fileName, final String mimeType) {
        this.fileName = fileName;
        this.mimeType = mimeType;
    }
    
    public String getFileName() {
        ManifestEntry.logger.debug("Filename: " + this.fileName);
        return this.fileName;
    }
    
    public String getMimeType() {
        ManifestEntry.logger.debug("Mime type: " + this.mimeType);
        return this.mimeType;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ManifestEntry && this.fileName.equals(((ManifestEntry)obj).getFileName()) && this.mimeType.equals(((ManifestEntry)obj).getMimeType());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.fileName, this.mimeType);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ManifestEntry.class);
    }
}
