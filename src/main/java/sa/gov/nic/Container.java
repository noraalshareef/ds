// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import eu.europa.esig.dss.MimeType;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

public interface Container extends Serializable
{
    DataFile addDataFile(final String p0, final String p1);
    
    DataFile addDataFile(final InputStream p0, final String p1, final String p2);
    
    DataFile addDataFile(final File p0, final String p1);
    
    void addDataFile(final DataFile p0);
    
    void addSignature(final Signature p0);
    
    List<DataFile> getDataFiles();
    
    String getType();
    
    List<Signature> getSignatures();
    
    void removeDataFile(final DataFile p0);
    
    void removeSignature(final Signature p0);
    
    void extendSignatureProfile(final SignatureProfile p0);
    
    File saveAsFile(final String p0);
    
    InputStream saveAsStream();
    
    ValidationResult validate();
    
    void setTimeStampToken(final DataFile p0);
    
    @Deprecated
    SignedInfo prepareSigning(final X509Certificate p0);
    
    @Deprecated
    String getSignatureProfile();
    
    @Deprecated
    void setSignatureParameters(final SignatureParameters p0);
    
    @Deprecated
    DigestAlgorithm getDigestAlgorithm();
    
    @Deprecated
    void addRawSignature(final byte[] p0);
    
    @Deprecated
    void addRawSignature(final InputStream p0);
    
    @Deprecated
    DataFile getDataFile(final int p0);
    
    @Deprecated
    int countDataFiles();
    
    @Deprecated
    void removeDataFile(final String p0);
    
    @Deprecated
    void removeSignature(final int p0);
    
    @Deprecated
    void save(final String p0);
    
    void save(final OutputStream p0);
    
    @Deprecated
    Signature sign(final SignatureToken p0);
    
    @Deprecated
    Signature signRaw(final byte[] p0);
    
    @Deprecated
    Signature getSignature(final int p0);
    
    @Deprecated
    int countSignatures();
    
    @Deprecated
    DocumentType getDocumentType();
    
    @Deprecated
    String getVersion();
    
    @Deprecated
    void extendTo(final SignatureProfile p0);
    
    @Deprecated
    void setSignatureProfile(final SignatureProfile p0);
    
    public enum DocumentType
    {
        BDOC, 
        DDOC, 
        ASICS, 
        ASICE, 
        PADES;
        
        @Override
        public String toString() {
            if (this == DocumentType.BDOC || this == DocumentType.ASICE) {
                return MimeType.ASICE.getMimeTypeString();
            }
            if (this == DocumentType.ASICS) {
                return MimeType.ASICS.getMimeTypeString();
            }
            return super.toString();
        }
    }
}
