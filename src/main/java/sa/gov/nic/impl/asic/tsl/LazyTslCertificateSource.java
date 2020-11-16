// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DSSException;
import sa.gov.nic.exceptions.TslCertificateSourceInitializationException;
import java.util.Date;
import eu.europa.esig.dss.tsl.ServiceInfo;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CertificatePool;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import org.slf4j.Logger;
import sa.gov.nic.TSLCertificateSource;

public class LazyTslCertificateSource implements TSLCertificateSource
{
    private static final Logger logger;
    private TSLCertificateSource certificateSource;
    private transient TSLValidationJob tslValidationJob;
    private Long lastCacheReloadingTime;
    private Long cacheExpirationTime;
    private TslLoader tslLoader;
    
    public LazyTslCertificateSource(final TslLoader tslLoader) {
        LazyTslCertificateSource.logger.debug("Initializing lazy TSL certificate source");
        this.tslLoader = tslLoader;
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
    
    @Override
    public void addTSLCertificate(final X509Certificate certificate) {
        this.getCertificateSource().addTSLCertificate(certificate);
    }
    
    @Override
    public CertificateToken addCertificate(final CertificateToken certificate, final ServiceInfo serviceInfo) {
        return this.getCertificateSource().addCertificate(certificate, serviceInfo);
    }
    
    @Override
    public List<CertificateToken> getCertificates() {
        return this.getCertificateSource().getCertificates();
    }
    
    @Override
    public void invalidateCache() {
        LazyTslCertificateSource.logger.debug("Invalidating TSL cache");
        TslLoader.invalidateCache();
    }
    
    @Override
    public void refresh() {
        this.refreshTsl();
    }
    
    public void setCacheExpirationTime(final Long cacheExpirationTime) {
        this.cacheExpirationTime = cacheExpirationTime;
    }
    
    protected void refreshIfCacheExpired() {
        if (this.isCacheExpired()) {
            this.initTsl();
        }
    }
    
    public Long getCacheExpirationTime() {
        return this.cacheExpirationTime;
    }
    
    public Long getLastCacheReloadingTime() {
        return this.lastCacheReloadingTime;
    }
    
    private TSLCertificateSource getCertificateSource() {
        LazyTslCertificateSource.logger.debug("Accessing TSL");
        this.refreshIfCacheExpired();
        return this.certificateSource;
    }
    
    private synchronized void initTsl() {
        if (this.isCacheExpired()) {
            LazyTslCertificateSource.logger.debug("Initializing TSL");
            this.refreshTsl();
        }
    }
    
    private synchronized void refreshTsl() {
        try {
            this.populateTsl();
            LazyTslCertificateSource.logger.debug("Refreshing TSL");
            this.tslValidationJob.refresh();
            this.lastCacheReloadingTime = new Date().getTime();
            if (LazyTslCertificateSource.logger.isDebugEnabled()) {
                LazyTslCertificateSource.logger.debug("Finished refreshing TSL, cache expires at " + this.getNextCacheExpirationDate());
            }
        }
        catch (DSSException e) {
            LazyTslCertificateSource.logger.error("Unable to load TSL: " + e.getMessage());
            throw new TslCertificateSourceInitializationException(e.getMessage());
        }
    }
    
    private void populateTsl() {
        if (this.tslValidationJob == null || this.certificateSource == null) {
            this.tslLoader.prepareTsl();
            this.tslValidationJob = this.tslLoader.getTslValidationJob();
            this.certificateSource = this.tslLoader.getTslCertificateSource();
        }
    }
    
    public TslLoader getTslLoader() {
        return this.tslLoader;
    }
    
    private boolean isCacheExpired() {
        if (this.lastCacheReloadingTime == null) {
            return true;
        }
        final long currentTime = new Date().getTime();
        final long timeToReload = this.lastCacheReloadingTime + this.cacheExpirationTime;
        return currentTime > timeToReload;
    }
    
    private String getNextCacheExpirationDate() {
        return new Date(this.lastCacheReloadingTime + this.cacheExpirationTime).toString();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)LazyTslCertificateSource.class);
    }
}
