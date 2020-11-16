// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.tsl;

import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.io.IOException;
import sa.gov.nic.exceptions.TslKeyStoreNotFoundException;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import sa.gov.nic.impl.asic.CachingDataLoader;
import eu.europa.esig.dss.client.http.Protocol;
import eu.europa.esig.dss.client.http.DataLoader;
import java.util.List;
import sa.gov.nic.exceptions.DigiDoc4JException;
import org.apache.commons.io.FileUtils;
import eu.europa.esig.dss.DSSException;
import sa.gov.nic.exceptions.TslCertificateSourceInitializationException;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import sa.gov.nic.Configuration;
import java.io.File;
import org.slf4j.Logger;
import java.io.Serializable;

public class TslLoader implements Serializable
{
    private static final Logger logger;
    public static final File fileCacheDirectory;
    private boolean checkSignature;
    private Configuration configuration;
    private transient TSLRepository tslRepository;
    private transient TSLCertificateSourceImpl tslCertificateSource;
    private transient TSLValidationJob tslValidationJob;
    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    
    public TslLoader(final Configuration configuration) {
        this.checkSignature = true;
        this.configuration = configuration;
    }
    
    public void prepareTsl() {
        try {
            this.tslCertificateSource = new TSLCertificateSourceImpl();
            (this.tslRepository = new TSLRepository()).setTrustedListsCertificateSource((TrustedListsCertificateSource)this.tslCertificateSource);
            this.tslValidationJob = this.createTslValidationJob(this.tslRepository);
        }
        catch (DSSException e) {
            TslLoader.logger.error("Unable to load TSL: " + e.getMessage());
            throw new TslCertificateSourceInitializationException(e.getMessage());
        }
    }
    
    public static void invalidateCache() {
        TslLoader.logger.info("Cleaning TSL cache directory at " + TslLoader.fileCacheDirectory.getPath());
        try {
            if (TslLoader.fileCacheDirectory.exists()) {
                FileUtils.cleanDirectory(TslLoader.fileCacheDirectory);
            }
            else {
                TslLoader.logger.debug("TSL cache directory doesn't exist");
            }
        }
        catch (Exception e) {
            TslLoader.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
    }
    
    public void setCheckSignature(final boolean checkSignature) {
        this.checkSignature = checkSignature;
    }
    
    public TSLCertificateSourceImpl getTslCertificateSource() {
        return this.tslCertificateSource;
    }
    
    public TSLValidationJob getTslValidationJob() {
        return this.tslValidationJob;
    }
    
    public TSLRepository getTslRepository() {
        return this.tslRepository;
    }
    
    private TSLValidationJob createTslValidationJob(final TSLRepository tslRepository) {
        final TSLValidationJob tslValidationJob = new TSLValidationJob();
        tslValidationJob.setDataLoader(this.createDataLoader());
        tslValidationJob.setOjContentKeyStore(this.getKeyStore());
        tslValidationJob.setLotlUrl(this.configuration.getTslLocation());
        tslValidationJob.setLotlCode("EU");
        tslValidationJob.setRepository(tslRepository);
        tslValidationJob.setCheckLOTLSignature(this.checkSignature);
        tslValidationJob.setCheckTSLSignatures(this.checkSignature);
        tslValidationJob.setFilterTerritories((List)this.configuration.getTrustedTerritories());
        return tslValidationJob;
    }
    
    private DataLoader createDataLoader() {
        if (Protocol.isHttpUrl(this.configuration.getTslLocation())) {
            final CachingDataLoader dataLoader = new CachingDataLoader(this.configuration);
            dataLoader.setTimeoutConnection(this.configuration.getConnectionTimeout());
            dataLoader.setTimeoutSocket(this.configuration.getSocketTimeout());
            dataLoader.setCacheExpirationTime(this.configuration.getTslCacheExpirationTime());
            dataLoader.setFileCacheDirectory(TslLoader.fileCacheDirectory);
            TslLoader.logger.debug("Using file cache directory for storing TSL: " + TslLoader.fileCacheDirectory);
            return (DataLoader)dataLoader;
        }
        return (DataLoader)new CommonsDataLoader();
    }
    
    private KeyStoreCertificateSource getKeyStore() {
        final File tslKeystoreFile = this.getTslKeystoreFile();
        try {
            return new KeyStoreCertificateSource(tslKeystoreFile, "JKS", this.configuration.getTslKeyStorePassword());
        }
        catch (IOException e) {
            TslLoader.logger.error(e.getMessage());
            throw new TslKeyStoreNotFoundException(e.getMessage());
        }
    }
    
    private File getTslKeystoreFile() throws TslKeyStoreNotFoundException {
        try {
            final String keystoreLocation = this.configuration.getTslKeyStoreLocation();
            if (Files.exists(Paths.get(keystoreLocation, new String[0]), new LinkOption[0])) {
                return new File(keystoreLocation);
            }
            final File tempFile = File.createTempFile("temp-tsl-keystore", ".jks");
            final InputStream in = this.getClass().getClassLoader().getResourceAsStream(keystoreLocation);
            if (in == null) {
                TslLoader.logger.error("keystore not found in location " + keystoreLocation);
                throw new TslKeyStoreNotFoundException("keystore not found in location " + keystoreLocation);
            }
            FileUtils.copyInputStreamToFile(in, tempFile);
            return tempFile;
        }
        catch (IOException e) {
            TslLoader.logger.error(e.getMessage());
            throw new TslKeyStoreNotFoundException(e.getMessage());
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TslLoader.class);
        fileCacheDirectory = new File(System.getProperty("java.io.tmpdir") + "/digidoc4jTSLCache");
    }
}
