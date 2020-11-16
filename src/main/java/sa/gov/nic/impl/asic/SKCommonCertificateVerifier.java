// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.asic.tsl.LazyCertificatePool;
import eu.europa.esig.dss.x509.CertificatePool;
import eu.europa.esig.dss.x509.ocsp.ListOCSPSource;
import eu.europa.esig.dss.x509.crl.ListCRLSource;
import eu.europa.esig.dss.client.http.DataLoader;
import sa.gov.nic.impl.asic.tsl.LazyTslCertificateSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import sa.gov.nic.impl.asic.tsl.ClonedTslCertificateSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.slf4j.Logger;
import eu.europa.esig.dss.validation.CertificateVerifier;
import java.io.Serializable;

public class SKCommonCertificateVerifier implements Serializable, CertificateVerifier
{
    private static final Logger logger;
    private transient CommonCertificateVerifier commonCertificateVerifier;
    private transient CertificateSource trustedCertSource;
    
    public SKCommonCertificateVerifier() {
        this.commonCertificateVerifier = new CommonCertificateVerifier();
    }
    
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.commonCertificateVerifier = new CommonCertificateVerifier();
    }
    
    public CertificateSource getTrustedCertSource() {
        if (this.trustedCertSource instanceof ClonedTslCertificateSource && ((ClonedTslCertificateSource)this.trustedCertSource).getTrustedListsCertificateSource() != null) {
            SKCommonCertificateVerifier.logger.debug("get TrustedListCertificateSource from ClonedTslCertificateSource");
            return (CertificateSource)((ClonedTslCertificateSource)this.trustedCertSource).getTrustedListsCertificateSource();
        }
        return this.commonCertificateVerifier.getTrustedCertSource();
    }
    
    public OCSPSource getOcspSource() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getOcspSource();
    }
    
    public CRLSource getCrlSource() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getCrlSource();
    }
    
    public void setCrlSource(final CRLSource crlSource) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setCrlSource(crlSource);
    }
    
    public void setOcspSource(final OCSPSource ocspSource) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setOcspSource(ocspSource);
    }
    
    public void setTrustedCertSource(final CertificateSource trustedCertSource) {
        final ClonedTslCertificateSource clonedTslCertificateSource = new ClonedTslCertificateSource(trustedCertSource);
        this.trustedCertSource = (CertificateSource)clonedTslCertificateSource;
        if (trustedCertSource instanceof LazyTslCertificateSource) {
            SKCommonCertificateVerifier.logger.debug("get TrustedCertSource from LazyTslCertificateSource");
            this.commonCertificateVerifier.setTrustedCertSource((CertificateSource)((LazyTslCertificateSource)trustedCertSource).getTslLoader().getTslCertificateSource());
        }
        else {
            this.commonCertificateVerifier.setTrustedCertSource((CertificateSource)clonedTslCertificateSource);
        }
    }
    
    public CertificateSource getAdjunctCertSource() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getAdjunctCertSource();
    }
    
    public void setAdjunctCertSource(final CertificateSource adjunctCertSource) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setAdjunctCertSource(adjunctCertSource);
    }
    
    public DataLoader getDataLoader() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getDataLoader();
    }
    
    public void setDataLoader(final DataLoader dataLoader) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setDataLoader(dataLoader);
    }
    
    public ListCRLSource getSignatureCRLSource() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getSignatureCRLSource();
    }
    
    public void setSignatureCRLSource(final ListCRLSource signatureCRLSource) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setSignatureCRLSource(signatureCRLSource);
    }
    
    public ListOCSPSource getSignatureOCSPSource() {
        SKCommonCertificateVerifier.logger.debug("");
        return this.commonCertificateVerifier.getSignatureOCSPSource();
    }
    
    public void setSignatureOCSPSource(final ListOCSPSource signatureOCSPSource) {
        SKCommonCertificateVerifier.logger.debug("");
        this.commonCertificateVerifier.setSignatureOCSPSource(signatureOCSPSource);
    }
    
    public CertificatePool createValidationPool() {
        SKCommonCertificateVerifier.logger.debug("");
        if (this.trustedCertSource == null) {
            return this.commonCertificateVerifier.createValidationPool();
        }
        return new LazyCertificatePool(this.trustedCertSource);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SKCommonCertificateVerifier.class);
    }
}
