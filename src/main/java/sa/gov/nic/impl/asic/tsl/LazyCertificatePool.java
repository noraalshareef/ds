// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import java.util.Set;
import eu.europa.esig.dss.tsl.ServiceInfo;
import eu.europa.esig.dss.x509.CertificateSourceType;
import eu.europa.esig.dss.x509.CertificateToken;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import eu.europa.esig.dss.x509.CertificateSource;
import org.slf4j.Logger;
import eu.europa.esig.dss.x509.CertificatePool;

public class LazyCertificatePool extends CertificatePool
{
    private static final Logger logger;
    private CertificateSource trustedCertSource;
    
    public LazyCertificatePool(final CertificateSource trustedCertSource) {
        LazyCertificatePool.logger.debug("Initializing lazy certificate pool");
        this.trustedCertSource = trustedCertSource;
    }
    
    public List<CertificateToken> get(final X500Principal x500Principal) {
        return (List<CertificateToken>)this.getCertificatePool().get(x500Principal);
    }
    
    public List<CertificateToken> getCertificateTokens() {
        return (List<CertificateToken>)this.getCertificatePool().getCertificateTokens();
    }
    
    public CertificateToken getInstance(final CertificateToken cert, final CertificateSourceType certSource) {
        return this.getCertificatePool().getInstance(cert, certSource);
    }
    
    public CertificateToken getInstance(final CertificateToken cert, final CertificateSourceType certSource, final ServiceInfo serviceInfo) {
        return this.getCertificatePool().getInstance(cert, certSource, serviceInfo);
    }
    
    public CertificateToken getInstance(final CertificateToken certificateToAdd, final Set<CertificateSourceType> sources, final Set<ServiceInfo> services) {
        return this.getCertificatePool().getInstance(certificateToAdd, (Set)sources, (Set)services);
    }
    
    public int getNumberOfCertificates() {
        return this.getCertificatePool().getNumberOfCertificates();
    }
    
    public void merge(final CertificatePool certPool) {
        this.getCertificatePool().merge(certPool);
    }
    
    private CertificatePool getCertificatePool() {
        LazyCertificatePool.logger.debug("Accessing certificate pool");
        return this.trustedCertSource.getCertificatePool();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)LazyCertificatePool.class);
    }
}
