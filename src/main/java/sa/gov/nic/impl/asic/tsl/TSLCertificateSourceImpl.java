// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.util.TimeDependentValues;
import java.util.Date;
import java.util.Map;
import eu.europa.esig.dss.tsl.ServiceInfoStatus;
import java.util.Arrays;
import eu.europa.esig.dss.tsl.Condition;
import java.util.List;
import java.util.HashMap;
import eu.europa.esig.dss.tsl.KeyUsageCondition;
import eu.europa.esig.dss.tsl.KeyUsageBit;
import eu.europa.esig.dss.tsl.ServiceInfo;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import sa.gov.nic.TSLCertificateSource;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;

public class TSLCertificateSourceImpl extends TrustedListsCertificateSource implements TSLCertificateSource
{
    private static final Logger logger;
    
    public void addTSLCertificate(final X509Certificate certificate) {
        final ServiceInfo serviceInfo = new ServiceInfo();
        final Condition condition = (Condition)new KeyUsageCondition(KeyUsageBit.nonRepudiation, true);
        final Map<String, List<Condition>> qualifiersAndConditions = new HashMap<String, List<Condition>>();
        qualifiersAndConditions.put("http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/QCWithSSCD", Arrays.asList(condition));
        final ServiceInfoStatus status = new ServiceInfoStatus("http://uri.etsi.org/TrstSvc/Svctype/CA/QC", "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/undersupervision", (Map)qualifiersAndConditions, (List)null, (Date)null, certificate.getNotBefore(), (Date)null);
        final TimeDependentValues timeDependentValues = new TimeDependentValues((Iterable)Arrays.asList(status));
        serviceInfo.setStatus(timeDependentValues);
        this.addCertificate(new CertificateToken(certificate), serviceInfo);
    }
    
    public void invalidateCache() {
        TSLCertificateSourceImpl.logger.debug("Invalidating TSL cache");
        TslLoader.invalidateCache();
    }
    
    public void refresh() {
        TSLCertificateSourceImpl.logger.warn("Not possible to refresh this certificate source");
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TSLCertificateSourceImpl.class);
    }
}
