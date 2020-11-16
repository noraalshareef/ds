// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import java.util.zip.ZipEntry;
import eu.europa.esig.dss.DSSDocument;
import java.io.Serializable;

public class AsicEntry implements Serializable
{
    private String name;
    private String comment;
    private byte[] extraFieldData;
    private DSSDocument content;
    private boolean isSignature;
    
    public AsicEntry(final ZipEntry zipEntry) {
        this.name = zipEntry.getName();
        this.comment = zipEntry.getComment();
        this.extraFieldData = zipEntry.getExtra();
    }
    
    public DSSDocument getContent() {
        return this.content;
    }
    
    public void setContent(final DSSDocument content) {
        this.content = content;
    }
    
    public ZipEntry getZipEntry() {
        final ZipEntry entry = new ZipEntry(this.name);
        entry.setComment(this.comment);
        entry.setExtra(this.extraFieldData);
        return entry;
    }
    
    public boolean isSignature() {
        return this.isSignature;
    }
    
    public void setSignature(final boolean signature) {
        this.isSignature = signature;
    }
}
