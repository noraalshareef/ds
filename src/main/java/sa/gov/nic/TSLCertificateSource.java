// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.util.List;
import eu.europa.esig.dss.tsl.ServiceInfo;
import eu.europa.esig.dss.x509.CertificateToken;
import java.security.cert.X509Certificate;
import eu.europa.esig.dss.x509.CertificateSource;

public interface TSLCertificateSource extends CertificateSource
{
    void addTSLCertificate(final X509Certificate p0);
    
    CertificateToken addCertificate(final CertificateToken p0, final ServiceInfo p1);
    
    List<CertificateToken> getCertificates();
    
    void invalidateCache();
    
    void refresh();
}
