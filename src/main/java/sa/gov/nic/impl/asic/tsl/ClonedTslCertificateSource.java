// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CertificatePool;
import java.io.Serializable;
import org.apache.commons.lang3.SerializationUtils;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import org.slf4j.Logger;
import eu.europa.esig.dss.x509.CertificateSource;

public class ClonedTslCertificateSource implements CertificateSource
{
    private static final Logger logger;
    private CertificateSource certificateSource;
    private CertificateSource clonedCertificateSource;
    private TrustedListsCertificateSource trustedListsCertificateSource;
    
    public ClonedTslCertificateSource(final CertificateSource certificateSource) {
        ClonedTslCertificateSource.logger.debug("Instantiating cloned tsl cert source");
        this.certificateSource = certificateSource;
    }
    
    private CertificateSource getCertificateSource() {
        ClonedTslCertificateSource.logger.debug("Accessing TSL");
        if (this.clonedCertificateSource == null) {
            this.initializeClonedTsl();
        }
        return this.clonedCertificateSource;
    }
    
    private void initializeClonedTsl() {
        if (this.certificateSource instanceof LazyTslCertificateSource) {
            ((LazyTslCertificateSource)this.certificateSource).refreshIfCacheExpired();
            this.trustedListsCertificateSource = ((LazyTslCertificateSource)this.certificateSource).getTslLoader().getTslCertificateSource();
        }
        ClonedTslCertificateSource.logger.debug("Cloning TSL");
        this.clonedCertificateSource = (CertificateSource)SerializationUtils.clone((Serializable)this.certificateSource);
        ClonedTslCertificateSource.logger.debug("Finished cloning TSL");
    }
    
    public TrustedListsCertificateSource getTrustedListsCertificateSource() {
        return this.trustedListsCertificateSource;
    }
    
    public CertificatePool getCertificatePool() {
        return this.getCertificateSource().getCertificatePool();
    }
    
    public CertificateToken addCertificate(final CertificateToken certificate) {
        return this.getCertificateSource().addCertificate(certificate);
    }
    
    public List<CertificateToken> get(final X500Principal x500Principal) {
        return (List<CertificateToken>)this.getCertificateSource().get(x500Principal);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ClonedTslCertificateSource.class);
    }
}
