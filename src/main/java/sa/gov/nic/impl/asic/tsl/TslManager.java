// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import sa.gov.nic.Configuration;
import sa.gov.nic.TSLCertificateSource;
import org.slf4j.Logger;
import java.io.Serializable;

public class TslManager implements Serializable
{
    private static final Logger logger;
    private TSLCertificateSource tslCertificateSource;
    private Configuration configuration;
    
    public TslManager(final Configuration configuration) {
        this.configuration = configuration;
    }
    
    public TSLCertificateSource getTsl() {
        if (this.tslCertificateSource != null) {
            TslManager.logger.debug("Using TSL cached copy");
            return this.tslCertificateSource;
        }
        this.loadTsl();
        return this.tslCertificateSource;
    }
    
    public void setTsl(final TSLCertificateSource certificateSource) {
        this.tslCertificateSource = certificateSource;
    }
    
    private synchronized void loadTsl() {
        if (this.tslCertificateSource == null) {
            TslManager.logger.debug("Loading TSL in a synchronized block");
            final TslLoader tslLoader = new TslLoader(this.configuration);
            tslLoader.setCheckSignature(this.configuration.shouldValidateTslSignature());
            final LazyTslCertificateSource lazyTsl = new LazyTslCertificateSource(tslLoader);
            lazyTsl.setCacheExpirationTime(this.configuration.getTslCacheExpirationTime());
            this.tslCertificateSource = lazyTsl;
            TslManager.logger.debug("Finished loading TSL in a synchronized block");
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TslManager.class);
    }
}
