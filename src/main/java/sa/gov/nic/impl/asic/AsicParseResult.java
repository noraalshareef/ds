// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import sa.gov.nic.impl.asic.manifest.ManifestParser;
import sa.gov.nic.DataFile;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import java.io.Serializable;

public class AsicParseResult implements Serializable
{
    private List<DSSDocument> signatures;
    private List<DataFile> dataFiles;
    private List<DSSDocument> detachedContents;
    private Integer currentUsedSignatureFileIndex;
    private String zipFileComment;
    private List<AsicEntry> asicEntries;
    private ManifestParser manifestParser;
    private DataFile timeStampToken;
    private String mimeType;
    
    public List<DataFile> getDataFiles() {
        return this.dataFiles;
    }
    
    public void setDataFiles(final List<DataFile> dataFiles) {
        this.dataFiles = dataFiles;
    }
    
    public List<DSSDocument> getSignatures() {
        return this.signatures;
    }
    
    public void setSignatures(final List<DSSDocument> signatures) {
        this.signatures = signatures;
    }
    
    public List<DSSDocument> getDetachedContents() {
        return this.detachedContents;
    }
    
    public void setDetachedContents(final List<DSSDocument> detachedContents) {
        this.detachedContents = detachedContents;
    }
    
    public Integer getCurrentUsedSignatureFileIndex() {
        return this.currentUsedSignatureFileIndex;
    }
    
    public void setCurrentUsedSignatureFileIndex(final Integer currentUsedSignatureFileIndex) {
        this.currentUsedSignatureFileIndex = currentUsedSignatureFileIndex;
    }
    
    public String getZipFileComment() {
        return this.zipFileComment;
    }
    
    public void setZipFileComment(final String zipFileComment) {
        this.zipFileComment = zipFileComment;
    }
    
    public List<AsicEntry> getAsicEntries() {
        return this.asicEntries;
    }
    
    public void setAsicEntries(final List<AsicEntry> asicEntries) {
        this.asicEntries = asicEntries;
    }
    
    public ManifestParser getManifestParser() {
        return this.manifestParser;
    }
    
    public void setManifestParser(final ManifestParser manifestParser) {
        this.manifestParser = manifestParser;
    }
    
    public void setTimeStampToken(final DataFile timeStampToken) {
        this.timeStampToken = timeStampToken;
    }
    
    public DataFile getTimeStampToken() {
        return this.timeStampToken;
    }
    
    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }
}
