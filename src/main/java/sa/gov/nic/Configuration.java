//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic;

import eu.europa.esig.dss.client.http.Protocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import sa.gov.nic.Constant.Production;
import sa.gov.nic.exceptions.ConfigurationException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.impl.ConfigurationSingeltonHolder;
import sa.gov.nic.impl.asic.tsl.TslManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class Configuration implements Serializable {
    private final Logger log;
    private final Configuration.Mode mode;
    private transient ExecutorService threadExecutor;
    private TslManager tslManager;
    private Hashtable<String, String> jDigiDocConfiguration;
    private ConfigurationRegistry registry;
    private HashMap<String, Map<ConfigurationParameter, String>> tspMap;
    private List<String> trustedTerritories;
    private ArrayList<String> inputSourceParseErrors;
    private LinkedHashMap configurationFromFile;
    private String configurationInputSourceName;

    public static Configuration getInstance() {
        return ConfigurationSingeltonHolder.getInstance();
    }

    public static Configuration of(Configuration.Mode mode) {
        return new Configuration(mode);
    }

    public Configuration() {
        this(Configuration.Mode.TEST.name().equalsIgnoreCase(System.getProperty("digidoc4j.mode")) ? Configuration.Mode.TEST : Configuration.Mode.PROD);
    }

    public Configuration(Configuration.Mode mode) {
        this.log = LoggerFactory.getLogger(Configuration.class);
        this.jDigiDocConfiguration = new Hashtable();
        this.registry = new ConfigurationRegistry();
        this.tspMap = new HashMap();
        this.trustedTerritories = new ArrayList();
        this.inputSourceParseErrors = new ArrayList();
        if (this.log.isInfoEnabled() && !this.log.isDebugEnabled()) {
            this.log.info("DigiDoc4J will be executed in <{}> mode", mode);
        }

        this.log.debug("------------------------ <MODE: {}> ------------------------", mode);
        this.mode = mode;
        this.loadConfiguration("digidoc4j.yaml");
        this.initDefaultValues();
        this.log.debug("------------------------ </MODE: {}> ------------------------", mode);
        if (!this.log.isDebugEnabled()) {
            this.log.info("Configuration loaded ...");
        }

    }

    public boolean isOCSPSigningConfigurationAvailable() {
        boolean available = StringUtils.isNotBlank(this.getOCSPAccessCertificateFileName()) && this.getOCSPAccessCertificatePassword().length != 0;
        this.log.debug("Is OCSP signing configuration available? {}", available);
        return available;
    }

    public String getOCSPAccessCertificateFileName() {
        String ocspAccessCertificateFile = this.getConfigurationParameter(ConfigurationParameter.OcspAccessCertificateFile);
        return ocspAccessCertificateFile == null ? "" : ocspAccessCertificateFile;
    }

    public char[] getOCSPAccessCertificatePassword() {
        char[] result = new char[0];
        String password = this.getConfigurationParameter(ConfigurationParameter.OcspAccessCertificatePassword);
        if (StringUtils.isNotEmpty(password)) {
            result = password.toCharArray();
        }

        return result;
    }

    public String getOCSPAccessCertificatePasswordAsString() {
        return this.getConfigurationParameter(ConfigurationParameter.OcspAccessCertificatePassword);
    }

    public void setOCSPAccessCertificateFileName(String fileName) {
        this.setConfigurationParameter(ConfigurationParameter.OcspAccessCertificateFile, fileName);
        this.setJDigiDocParameter("DIGIDOC_PKCS12_CONTAINER", fileName);
    }

    public void setOCSPAccessCertificatePassword(char[] password) {
        String value = String.valueOf(password);
        this.setConfigurationParameter(ConfigurationParameter.OcspAccessCertificatePassword, value);
        this.setJDigiDocParameter("DIGIDOC_PKCS12_PASSWD", value);
    }

    public void setSignOCSPRequests(boolean shouldSignOcspRequests) {
        String value = String.valueOf(shouldSignOcspRequests);
        this.setConfigurationParameter(ConfigurationParameter.SignOcspRequests, value);
        this.setJDigiDocParameter("SIGN_OCSP_REQUESTS", value);
    }

    public Hashtable<String, String> loadConfiguration(InputStream stream) {
        this.configurationInputSourceName = "stream";
        return this.loadConfigurationSettings(stream);
    }

    public Hashtable<String, String> loadConfiguration(String file) {
        return this.loadConfiguration(file, true);
    }

    public Hashtable<String, String> loadConfiguration(String file, boolean isReloadFromYaml) {
        if (!isReloadFromYaml) {
            this.log.debug("Should not reload conf from yaml when open container");
            return this.jDigiDocConfiguration;
        } else {
            this.log.debug("Loading configuration from file <{}>", file);
            this.configurationInputSourceName = file;
            Object resourceAsStream = null;

            try {
                resourceAsStream = new FileInputStream(file);
            } catch (FileNotFoundException var5) {
                this.log.debug("Configuration file <{}> not found. Trying to search from jar file", file);
            }

            if (resourceAsStream == null) {
                resourceAsStream = this.getResourceAsStream(file);
            }

            return this.loadConfigurationSettings((InputStream)resourceAsStream);
        }
    }

    public Hashtable<String, String> getJDigiDocConfiguration() {
        this.loadCertificateAuthoritiesAndCertificates();
        this.reportFileParseErrors();
        return this.jDigiDocConfiguration;
    }

    /** @deprecated */
    @Deprecated
    public void enableBigFilesSupport(long maxFileSizeCachedInMB) {
        this.log.debug("Set maximum datafile cached to: " + maxFileSizeCachedInMB);
        String value = Long.toString(maxFileSizeCachedInMB);
        if (this.isValidIntegerParameter("DIGIDOC_MAX_DATAFILE_CACHED", value)) {
            this.jDigiDocConfiguration.put("DIGIDOC_MAX_DATAFILE_CACHED", value);
        }

    }

    public void setMaxFileSizeCachedInMemoryInMB(long maxFileSizeCachedInMB) {
        this.enableBigFilesSupport(maxFileSizeCachedInMB);
    }

    /** @deprecated */
    @Deprecated
    public boolean isBigFilesSupportEnabled() {
        return this.getMaxDataFileCachedInMB() >= 0L;
    }

    public boolean storeDataFilesOnlyInMemory() {
        long maxDataFileCachedInMB = this.getMaxDataFileCachedInMB();
        return maxDataFileCachedInMB == -1L || maxDataFileCachedInMB == 9223372036854775807L;
    }

    public boolean hasToBeOCSPRequestSigned() {
        String signOcspRequests = this.getConfigurationParameter(ConfigurationParameter.SignOcspRequests);
        return StringUtils.equalsIgnoreCase("true", signOcspRequests);
    }

    public long getMaxDataFileCachedInMB() {
        String maxDataFileCached = (String)this.jDigiDocConfiguration.get("DIGIDOC_MAX_DATAFILE_CACHED");
        this.log.debug("Maximum datafile cached in MB: " + maxDataFileCached);
        return maxDataFileCached == null ? -1L : Long.parseLong(maxDataFileCached);
    }

    public long getMaxDataFileCachedInBytes() {
        long maxDataFileCachedInMB = this.getMaxDataFileCachedInMB();
        return maxDataFileCachedInMB == -1L ? -1L : maxDataFileCachedInMB * 1048576L;
    }

    public String getTslLocation() {
        String urlString = this.getConfigurationParameter(ConfigurationParameter.TslLocation);
        if (!Protocol.isFileUrl(urlString)) {
            return urlString;
        } else {
            try {
                String filePath = (new URL(urlString)).getPath();
                if (!(new File(filePath)).exists()) {
                    URL resource = this.getClass().getClassLoader().getResource(filePath);
                    if (resource != null) {
                        urlString = resource.toString();
                    }
                }
            } catch (MalformedURLException var4) {
                this.log.warn(var4.getMessage());
            }

            return urlString == null ? "" : urlString;
        }
    }

    public void setTSL(TSLCertificateSource certificateSource) {
        this.tslManager.setTsl(certificateSource);
    }

    public TSLCertificateSource getTSL() {
        return this.tslManager.getTsl();
    }

    public boolean shouldValidateTslSignature() {
        return this.mode != Configuration.Mode.TEST;
    }

    public void setTslLocation(String tslLocation) {
        this.setConfigurationParameter(ConfigurationParameter.TslLocation, tslLocation);
        this.tslManager.setTsl((TSLCertificateSource)null);
    }

    public String getTspSource() {
        return this.getConfigurationParameter(ConfigurationParameter.TspSource);
    }

    public String getTspSourceByCountry(String country) {
        if (this.tspMap.containsKey(country)) {
            String source = (String)((Map)this.tspMap.get(country)).get(ConfigurationParameter.TspCountrySource);
            if (StringUtils.isNotBlank(source)) {
                return source;
            }
        }

        this.log.info("Source by country <{}> not found, using default TSP source", country);
        return this.getTspSource();
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.setConfigurationParameter(ConfigurationParameter.ConnectionTimeoutInMillis, String.valueOf(connectionTimeout));
    }

    public void setSocketTimeout(int socketTimeoutMilliseconds) {
        this.setConfigurationParameter(ConfigurationParameter.SocketTimeoutInMillis, String.valueOf(socketTimeoutMilliseconds));
    }

    public int getConnectionTimeout() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.ConnectionTimeoutInMillis, Integer.class);
    }

    public int getSocketTimeout() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.SocketTimeoutInMillis, Integer.class);
    }

    public void setTspSource(String tspSource) {
        this.setConfigurationParameter(ConfigurationParameter.TspSource, tspSource);
    }

    public String getOcspSource() {
        return this.getConfigurationParameter(ConfigurationParameter.OcspSource);
    }

    public void setTslKeyStoreLocation(String tslKeyStoreLocation) {
        this.setConfigurationParameter(ConfigurationParameter.TslKeyStoreLocation, tslKeyStoreLocation);
    }

    public String getTslKeyStoreLocation() {
        return this.getConfigurationParameter(ConfigurationParameter.TslKeyStoreLocation);
    }

    public void setTslKeyStorePassword(String tslKeyStorePassword) {
        this.setConfigurationParameter(ConfigurationParameter.TslKeyStorePassword, tslKeyStorePassword);
    }

    public String getTslKeyStorePassword() {
        return this.getConfigurationParameter(ConfigurationParameter.TslKeyStorePassword);
    }

    public void setTslCacheExpirationTime(long cacheExpirationTimeInMilliseconds) {
        this.setConfigurationParameter(ConfigurationParameter.TslCacheExpirationTimeInMillis, String.valueOf(cacheExpirationTimeInMilliseconds));
    }

    public long getTslCacheExpirationTime() {
        return (Long)this.getConfigurationParameter(ConfigurationParameter.TslCacheExpirationTimeInMillis, Long.class);
    }

    public Integer getAllowedTimestampAndOCSPResponseDeltaInMinutes() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.AllowedTimestampAndOCSPResponseDeltaInMinutes, Integer.class);
    }

    public void setAllowedTimestampAndOCSPResponseDeltaInMinutes(int timeInMinutes) {
        this.setConfigurationParameter(ConfigurationParameter.AllowedTimestampAndOCSPResponseDeltaInMinutes, String.valueOf(timeInMinutes));
    }

    public void setOcspSource(String ocspSource) {
        this.setConfigurationParameter(ConfigurationParameter.OcspSource, ocspSource);
    }

    public String getValidationPolicy() {
        return this.getConfigurationParameter(ConfigurationParameter.ValidationPolicy);
    }

    public void setValidationPolicy(String validationPolicy) {
        this.setConfigurationParameter(ConfigurationParameter.ValidationPolicy, validationPolicy);
    }

    public int getRevocationAndTimestampDeltaInMinutes() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.RevocationAndTimestampDeltaInMinutes, Integer.class);
    }

    public void setRevocationAndTimestampDeltaInMinutes(int timeInMinutes) {
        this.setConfigurationParameter(ConfigurationParameter.RevocationAndTimestampDeltaInMinutes, String.valueOf(timeInMinutes));
    }

    public SignatureProfile getSignatureProfile() {
        return SignatureProfile.findByProfile(this.getConfigurationParameter(ConfigurationParameter.SignatureProfile));
    }

    public DigestAlgorithm getSignatureDigestAlgorithm() {
        return DigestAlgorithm.findByAlgorithm(this.getConfigurationParameter(ConfigurationParameter.SignatureDigestAlgorithm));
    }

    public String getHttpsProxyHost() {
        return this.getConfigurationParameter(ConfigurationParameter.HttpsProxyHost);
    }

    public void setHttpsProxyHost(String httpsProxyHost) {
        this.setConfigurationParameter(ConfigurationParameter.HttpsProxyHost, httpsProxyHost);
    }

    public Integer getHttpsProxyPort() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.HttpsProxyPort, Integer.class);
    }

    public void setHttpsProxyPort(int httpsProxyPort) {
        this.setConfigurationParameter(ConfigurationParameter.HttpsProxyPort, String.valueOf(httpsProxyPort));
    }

    public String getHttpProxyHost() {
        return this.getConfigurationParameter(ConfigurationParameter.HttpProxyHost);
    }

    public void setHttpProxyHost(String httpProxyHost) {
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyHost, httpProxyHost);
    }

    public Integer getHttpProxyPort() {
        return (Integer)this.getConfigurationParameter(ConfigurationParameter.HttpProxyPort, Integer.class);
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyPort, String.valueOf(httpProxyPort));
    }

    public void setHttpProxyUser(String httpProxyUser) {
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyUser, httpProxyUser);
    }

    public String getHttpProxyUser() {
        return this.getConfigurationParameter(ConfigurationParameter.HttpProxyUser);
    }

    public void setHttpProxyPassword(String httpProxyPassword) {
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyPassword, httpProxyPassword);
    }

    public String getHttpProxyPassword() {
        return this.getConfigurationParameter(ConfigurationParameter.HttpProxyPassword);
    }

    public boolean isNetworkProxyEnabled() {
        return this.getConfigurationParameter(ConfigurationParameter.HttpProxyPort, Integer.class) != null && StringUtils.isNotBlank(this.getConfigurationParameter(ConfigurationParameter.HttpProxyHost)) || this.getConfigurationParameter(ConfigurationParameter.HttpsProxyPort, Integer.class) != null && StringUtils.isNotBlank(this.getConfigurationParameter(ConfigurationParameter.HttpsProxyHost));
    }

    public boolean isProxyOfType(Protocol protocol) {
        switch(protocol) {
            case HTTP:
                return this.getConfigurationParameter(ConfigurationParameter.HttpProxyPort, Integer.class) != null && StringUtils.isNotBlank(this.getConfigurationParameter(ConfigurationParameter.HttpProxyHost));
            case HTTPS:
                return this.getConfigurationParameter(ConfigurationParameter.HttpsProxyPort, Integer.class) != null && StringUtils.isNotBlank(this.getConfigurationParameter(ConfigurationParameter.HttpsProxyHost));
            default:
                throw new RuntimeException(String.format("Protocol <%s> not supported", protocol));
        }
    }

    public boolean isSslConfigurationEnabled() {
        return StringUtils.isNotBlank(this.getConfigurationParameter(ConfigurationParameter.SslKeystorePath));
    }

    public void setSslKeystorePath(String sslKeystorePath) {
        this.setConfigurationParameter(ConfigurationParameter.SslKeystorePath, sslKeystorePath);
    }

    public String getSslKeystorePath() {
        return this.getConfigurationParameter(ConfigurationParameter.SslKeystorePath);
    }

    public void setSslKeystoreType(String sslKeystoreType) {
        this.setConfigurationParameter(ConfigurationParameter.SslKeystoreType, sslKeystoreType);
    }

    public String getSslKeystoreType() {
        return this.getConfigurationParameter(ConfigurationParameter.SslKeystoreType);
    }

    public void setSslKeystorePassword(String sslKeystorePassword) {
        this.setConfigurationParameter(ConfigurationParameter.SslKeystorePassword, sslKeystorePassword);
    }

    public String getSslKeystorePassword() {
        return this.getConfigurationParameter(ConfigurationParameter.SslKeystorePassword);
    }

    public void setSslTruststorePath(String sslTruststorePath) {
        this.setConfigurationParameter(ConfigurationParameter.SslTruststorePath, sslTruststorePath);
    }

    public String getSslTruststorePath() {
        return this.getConfigurationParameter(ConfigurationParameter.SslTruststorePath);
    }

    public void setSslTruststoreType(String sslTruststoreType) {
        this.setConfigurationParameter(ConfigurationParameter.SslTruststoreType, sslTruststoreType);
    }

    public String getSslTruststoreType() {
        return this.getConfigurationParameter(ConfigurationParameter.SslTruststoreType);
    }

    public void setSslTruststorePassword(String sslTruststorePassword) {
        this.setConfigurationParameter(ConfigurationParameter.SslTruststorePassword, sslTruststorePassword);
    }

    public String getSslTruststorePassword() {
        return this.getConfigurationParameter(ConfigurationParameter.SslTruststorePassword);
    }

    public void setThreadExecutor(ExecutorService threadExecutor) {
        this.threadExecutor = threadExecutor;
    }

    public ExecutorService getThreadExecutor() {
        return this.threadExecutor;
    }

    public void setTrustedTerritories(String... trustedTerritories) {
        this.trustedTerritories = Arrays.asList(trustedTerritories);
    }

    public List<String> getTrustedTerritories() {
        return this.trustedTerritories;
    }

    public boolean isTest() {
        boolean isTest = Configuration.Mode.TEST.equals(this.mode);
        this.log.debug("Is test: " + isTest);
        return isTest;
    }

    public Configuration copy() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Configuration copyConfiguration = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            copyConfiguration = (Configuration)ois.readObject();
        } catch (Exception var9) {
            throw new DigiDoc4JException(var9);
        } finally {
            IOUtils.closeQuietly(oos);
            IOUtils.closeQuietly(ois);
            IOUtils.closeQuietly(bos);
        }

        return copyConfiguration;
    }

    protected ConfigurationRegistry getRegistry() {
        return this.registry;
    }

    private void initDefaultValues() {
        this.log.debug("------------------------ DEFAULTS ------------------------");
        this.tslManager = new TslManager(this);
        this.setConfigurationParameter(ConfigurationParameter.ConnectionTimeoutInMillis, String.valueOf(1000));
        this.setConfigurationParameter(ConfigurationParameter.SocketTimeoutInMillis, String.valueOf(1000));
        this.setConfigurationParameter(ConfigurationParameter.TslKeyStorePassword, "digidoc4j-password");
        this.setConfigurationParameter(ConfigurationParameter.RevocationAndTimestampDeltaInMinutes, String.valueOf(1440));
        this.setConfigurationParameter(ConfigurationParameter.TslCacheExpirationTimeInMillis, String.valueOf(86400000L));
        this.setConfigurationParameter(ConfigurationParameter.AllowedTimestampAndOCSPResponseDeltaInMinutes, "15");
        this.setConfigurationParameter(ConfigurationParameter.SignatureProfile, "LT");
        this.setConfigurationParameter(ConfigurationParameter.SignatureDigestAlgorithm, "SHA256");
        if (Configuration.Mode.TEST.equals(this.mode)) {
            this.setConfigurationParameter(ConfigurationParameter.TspSource, "http://demo.sk.ee/tsa");
            this.setConfigurationParameter(ConfigurationParameter.TslLocation, "https://open-eid.github.io/test-TL/tl-mp-test-EE.xml");
            this.setConfigurationParameter(ConfigurationParameter.TslKeyStoreLocation, "keystore/test-keystore.jks");
            this.setConfigurationParameter(ConfigurationParameter.ValidationPolicy, "conf/test_constraint.xml");
            this.setConfigurationParameter(ConfigurationParameter.OcspSource, "http://demo.sk.ee/ocsp");
            this.setConfigurationParameter(ConfigurationParameter.SignOcspRequests, "false");
            this.setJDigiDocParameter("SIGN_OCSP_REQUESTS", "false");
        } else {
            this.setConfigurationParameter(ConfigurationParameter.TspSource, "http://tsa.sk.ee");
            this.setConfigurationParameter(ConfigurationParameter.TslLocation, "https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml");
            this.setConfigurationParameter(ConfigurationParameter.TslKeyStoreLocation, "keystore/keystore.jks");
            this.setConfigurationParameter(ConfigurationParameter.ValidationPolicy, "conf/constraint.xml");
            this.setConfigurationParameter(ConfigurationParameter.OcspSource, "http://ocsp.sk.ee/");
            this.setConfigurationParameter(ConfigurationParameter.SignOcspRequests, "false");
            this.trustedTerritories = Production.DEFAULT_TRUESTED_TERRITORIES;
            this.setJDigiDocParameter("SIGN_OCSP_REQUESTS", "false");
        }

        this.log.debug("{} configuration: {}", this.mode, this.registry);
        this.loadInitialConfigurationValues();
    }

    private void loadInitialConfigurationValues() {
        this.log.debug("------------------------ LOADING INITIAL CONFIGURATION ------------------------");
        this.setJDigiDocConfigurationValue("DIGIDOC_SECURITY_PROVIDER", "org.bouncycastle.jce.provider.BouncyCastleProvider");
        this.setJDigiDocConfigurationValue("DIGIDOC_SECURITY_PROVIDER_NAME", "BC");
        this.setJDigiDocConfigurationValue("KEY_USAGE_CHECK", "false");
        this.setJDigiDocConfigurationValue("DIGIDOC_OCSP_SIGN_CERT_SERIAL", "");
        this.setJDigiDocConfigurationValue("DATAFILE_HASHCODE_MODE", "false");
        this.setJDigiDocConfigurationValue("CANONICALIZATION_FACTORY_IMPL", "ee.sk.digidoc.c14n.TinyXMLCanonicalizer");
        this.setJDigiDocConfigurationValue("DIGIDOC_MAX_DATAFILE_CACHED", "-1");
        this.setJDigiDocConfigurationValue("DIGIDOC_USE_LOCAL_TSL", "true");
        this.setJDigiDocConfigurationValue("DIGIDOC_NOTARY_IMPL", "ee.sk.digidoc.factory.BouncyCastleNotaryFactory");
        this.setJDigiDocConfigurationValue("DIGIDOC_TSLFAC_IMPL", "ee.sk.digidoc.tsl.DigiDocTrustServiceFactory");
        this.setJDigiDocConfigurationValue("DIGIDOC_OCSP_RESPONDER_URL", this.getOcspSource());
        this.setJDigiDocConfigurationValue("DIGIDOC_FACTORY_IMPL", "ee.sk.digidoc.factory.SAXDigiDocFactory");
        this.setJDigiDocConfigurationValue("DIGIDOC_DF_CACHE_DIR", (String)null);
        this.setConfigurationValue("TSL_LOCATION", ConfigurationParameter.TslLocation);
        this.setConfigurationValue("TSP_SOURCE", ConfigurationParameter.TspSource);
        this.setConfigurationValue("VALIDATION_POLICY", ConfigurationParameter.ValidationPolicy);
        this.setConfigurationValue("OCSP_SOURCE", ConfigurationParameter.OcspSource);
        this.setConfigurationValue("DIGIDOC_PKCS12_CONTAINER", ConfigurationParameter.OcspAccessCertificateFile);
        this.setConfigurationValue("DIGIDOC_PKCS12_PASSWD", ConfigurationParameter.OcspAccessCertificatePassword);
        this.setConfigurationValue("CONNECTION_TIMEOUT", ConfigurationParameter.ConnectionTimeoutInMillis);
        this.setConfigurationValue("SOCKET_TIMEOUT", ConfigurationParameter.SocketTimeoutInMillis);
        this.setConfigurationValue("SIGN_OCSP_REQUESTS", ConfigurationParameter.SignOcspRequests);
        this.setConfigurationValue("TSL_KEYSTORE_LOCATION", ConfigurationParameter.TslKeyStoreLocation);
        this.setConfigurationValue("TSL_KEYSTORE_PASSWORD", ConfigurationParameter.TslKeyStorePassword);
        this.setConfigurationValue("TSL_CACHE_EXPIRATION_TIME", ConfigurationParameter.TslCacheExpirationTimeInMillis);
        this.setConfigurationValue("REVOCATION_AND_TIMESTAMP_DELTA_IN_MINUTES", ConfigurationParameter.RevocationAndTimestampDeltaInMinutes);
        this.setConfigurationValue("ALLOWED_TS_AND_OCSP_RESPONSE_DELTA_IN_MINUTES", ConfigurationParameter.AllowedTimestampAndOCSPResponseDeltaInMinutes);
        this.setConfigurationValue("SIGNATURE_PROFILE", ConfigurationParameter.SignatureProfile);
        this.setConfigurationValue("SIGNATURE_DIGEST_ALGORITHM", ConfigurationParameter.SignatureDigestAlgorithm);
        this.setJDigiDocConfigurationValue("SIGN_OCSP_REQUESTS", Boolean.toString(this.hasToBeOCSPRequestSigned()));
        this.setJDigiDocConfigurationValue("DIGIDOC_PKCS12_CONTAINER", this.getOCSPAccessCertificateFileName());
        this.initOcspAccessCertPasswordForJDigidoc();
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyHost, this.getParameter("http.proxyHost", "HTTP_PROXY_HOST"));
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyPort, this.getParameter("http.proxyPort", "HTTP_PROXY_PORT"));
        this.setConfigurationParameter(ConfigurationParameter.HttpsProxyHost, this.getParameter("https.proxyHost", "HTTPS_PROXY_HOST"));
        this.setConfigurationParameter(ConfigurationParameter.HttpsProxyPort, this.getParameter("https.proxyPort", "HTTPS_PROXY_PORT"));
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyUser, this.getParameterFromFile("HTTP_PROXY_USER"));
        this.setConfigurationParameter(ConfigurationParameter.HttpProxyPassword, this.getParameterFromFile("HTTP_PROXY_PASSWORD"));
        this.setConfigurationParameter(ConfigurationParameter.SslKeystoreType, this.getParameterFromFile("SSL_KEYSTORE_TYPE"));
        this.setConfigurationParameter(ConfigurationParameter.SslTruststoreType, this.getParameterFromFile("SSL_TRUSTSTORE_TYPE"));
        this.setConfigurationParameter(ConfigurationParameter.SslKeystorePath, this.getParameter("javax.net.ssl.keyStore", "SSL_KEYSTORE_PATH"));
        this.setConfigurationParameter(ConfigurationParameter.SslKeystorePassword, this.getParameter("javax.net.ssl.keyStorePassword", "SSL_KEYSTORE_PASSWORD"));
        this.setConfigurationParameter(ConfigurationParameter.SslTruststorePath, this.getParameter("javax.net.ssl.trustStore", "SSL_TRUSTSTORE_PATH"));
        this.setConfigurationParameter(ConfigurationParameter.SslTruststorePassword, this.getParameter("javax.net.ssl.trustStorePassword", "SSL_TRUSTSTORE_PASSWORD"));
        this.loadYamlTrustedTerritories();
        this.loadYamlTSPs();
    }

    private Hashtable<String, String> loadConfigurationSettings(InputStream stream) {
        this.configurationFromFile = new LinkedHashMap();
        Yaml yaml = new Yaml();

        try {
            this.configurationFromFile = (LinkedHashMap)yaml.load(stream);
        } catch (Exception var5) {
            ConfigurationException exception = new ConfigurationException("Configuration from " + this.configurationInputSourceName + " is not correctly formatted");
            this.log.error(exception.getMessage());
            throw exception;
        }

        IOUtils.closeQuietly(stream);
        return this.mapToJDigiDocConfiguration();
    }

    private InputStream getResourceAsStream(String certFile) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(certFile);
        if (resourceAsStream == null) {
            String message = "File " + certFile + " not found in classpath.";
            this.log.error(message);
            throw new ConfigurationException(message);
        } else {
            return resourceAsStream;
        }
    }

    private String defaultIfNull(String configParameter, String defaultValue) {
        this.log.debug("Parameter: " + configParameter);
        if (this.configurationFromFile == null) {
            return defaultValue;
        } else {
            Object value = this.configurationFromFile.get(configParameter);
            if (value != null) {
                return this.valueIsAllowed(configParameter, value.toString()) ? value.toString() : "";
            } else {
                String configuredValue = (String)this.jDigiDocConfiguration.get(configParameter);
                return configuredValue != null ? configuredValue : defaultValue;
            }
        }
    }

    private boolean valueIsAllowed(String configParameter, String value) {
        List<String> mustBeBooleans = Arrays.asList("SIGN_OCSP_REQUESTS", "KEY_USAGE_CHECK", "DATAFILE_HASHCODE_MODE", "DIGIDOC_USE_LOCAL_TSL");
        List<String> mustBeIntegers = Arrays.asList("DIGIDOC_MAX_DATAFILE_CACHED", "HTTP_PROXY_PORT");
        boolean errorFound = false;
        if (mustBeBooleans.contains(configParameter)) {
            errorFound = !this.isValidBooleanParameter(configParameter, value);
        }

        if (mustBeIntegers.contains(configParameter)) {
            errorFound = !this.isValidIntegerParameter(configParameter, value) || errorFound;
        }

        return !errorFound;
    }

    private boolean isValidBooleanParameter(String configParameter, String value) {
        if (!"true".equals(value.toLowerCase()) && !"false".equals(value.toLowerCase())) {
            String errorMessage = "Configuration parameter " + configParameter + " should be set to true or false" + " but the actual value is: " + value + ".";
            this.logError(errorMessage);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidIntegerParameter(String configParameter, String value) {
        Integer parameterValue;
        try {
            parameterValue = Integer.parseInt(value);
        } catch (Exception var6) {
            String errorMessage = "Configuration parameter " + configParameter + " should have an integer value" + " but the actual value is: " + value + ".";
            this.logError(errorMessage);
            return false;
        }

        if (configParameter.equals("DIGIDOC_MAX_DATAFILE_CACHED") && parameterValue < -1) {
            String errorMessage = "Configuration parameter " + configParameter + " should be greater or equal -1" + " but the actual value is: " + value + ".";
            this.logError(errorMessage);
            return false;
        } else {
            return true;
        }
    }

    private void loadOCSPCertificates(LinkedHashMap digiDocCA, String caPrefix) {
        this.log.debug("");
        ArrayList<LinkedHashMap> ocsps = (ArrayList)digiDocCA.get("OCSPS");
        String errorMessage;
        if (ocsps == null) {
            errorMessage = "No OCSPS entry found or OCSPS entry is empty. Configuration from: " + this.configurationInputSourceName;
            this.logError(errorMessage);
        } else {
            int numberOfOCSPCertificates = ocsps.size();
            this.jDigiDocConfiguration.put(caPrefix + "_OCSPS", String.valueOf(numberOfOCSPCertificates));

            for(int i = 1; i <= numberOfOCSPCertificates; ++i) {
                String prefix = caPrefix + "_OCSP" + i;
                LinkedHashMap ocsp = (LinkedHashMap)ocsps.get(i - 1);
                List<String> entries = Arrays.asList("CA_CN", "CA_CERT", "CN", "URL");
                Iterator i$ = entries.iterator();

                while(i$.hasNext()) {
                    String entry = (String)i$.next();
                    if (!this.loadOCSPCertificateEntry(entry, ocsp, prefix)) {
                        errorMessage = "OCSPS list entry " + i + " does not have an entry for " + entry + " or the entry is empty\n";
                        this.logError(errorMessage);
                    }
                }

                if (!this.getOCSPCertificates(prefix, ocsp)) {
                    errorMessage = "OCSPS list entry " + i + " does not have an entry for CERTS or the entry is empty\n";
                    this.logError(errorMessage);
                }
            }

        }
    }

    private void loadYamlTSPs() {
        List<Map<String, Object>> tsps = (List)this.configurationFromFile.get("TSPS");
        if (tsps == null) {
            this.setConfigurationParameter(ConfigurationParameter.TspsCount, "0");
        } else {
            this.setConfigurationParameter(ConfigurationParameter.TspsCount, String.valueOf(tsps.size()));
            List<Pair<String, ConfigurationParameter>> entryPairs = Arrays.asList(Pair.of("TSP_SOURCE", ConfigurationParameter.TspCountrySource), Pair.of("TSP_KEYSTORE_PATH", ConfigurationParameter.TspCountryKeystorePath), Pair.of("TSP_KEYSTORE_TYPE", ConfigurationParameter.TspCountryKeystoreType), Pair.of("TSP_KEYSTORE_PASSWORD", ConfigurationParameter.TspCountryKeystorePassword));

            for(int i = 0; i < tsps.size(); ++i) {
                Map<String, Object> tsp = (Map)tsps.get(i);
                Object country = tsp.get("TSP_C").toString();
                if (country != null) {
                    this.tspMap.put(country.toString(), new HashMap());
                    Iterator i$ = entryPairs.iterator();

                    while(i$.hasNext()) {
                        Pair<String, ConfigurationParameter> pair = (Pair)i$.next();
                        Object entryValue = tsp.get(pair.getKey());
                        if (entryValue != null) {
                            ((Map)this.tspMap.get(country.toString())).put(pair.getValue(), entryValue.toString());
                        } else {
                            this.logError(String.format("No value found for an entry <%s(%s)>", pair.getKey(), i + 1));
                        }
                    }
                } else {
                    this.logError(String.format("No value found for an entry <TSP_C(%s)>", i + 1));
                }
            }

        }
    }

    private Hashtable<String, String> mapToJDigiDocConfiguration() {
        this.log.debug("loading JDigiDoc configuration");
        this.inputSourceParseErrors = new ArrayList();
        this.loadInitialConfigurationValues();
        this.reportFileParseErrors();
        return this.jDigiDocConfiguration;
    }

    private void loadCertificateAuthoritiesAndCertificates() {
        this.log.debug("");
        ArrayList<LinkedHashMap> digiDocCAs = (ArrayList)this.configurationFromFile.get("DIGIDOC_CAS");
        if (digiDocCAs == null) {
            String errorMessage = "Empty or no DIGIDOC_CAS entry";
            this.logError(errorMessage);
        } else {
            int numberOfDigiDocCAs = digiDocCAs.size();
            this.jDigiDocConfiguration.put("DIGIDOC_CAS", String.valueOf(numberOfDigiDocCAs));

            for(int i = 0; i < numberOfDigiDocCAs; ++i) {
                String caPrefix = "DIGIDOC_CA_" + (i + 1);
                LinkedHashMap digiDocCA = (LinkedHashMap)((LinkedHashMap)digiDocCAs.get(i)).get("DIGIDOC_CA");
                if (digiDocCA == null) {
                    String errorMessage = "Empty or no DIGIDOC_CA for entry " + (i + 1);
                    this.logError(errorMessage);
                } else {
                    this.loadCertificateAuthorityCerts(digiDocCA, caPrefix);
                    this.loadOCSPCertificates(digiDocCA, caPrefix);
                }
            }

        }
    }

    private void logError(String errorMessage) {
        this.log.error(errorMessage);
        this.inputSourceParseErrors.add(errorMessage);
    }

    private void reportFileParseErrors() {
        this.log.debug("");
        if (this.inputSourceParseErrors.size() > 0) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Configuration from ");
            errorMessage.append(this.configurationInputSourceName);
            errorMessage.append(" contains error(s):\n");
            Iterator i$ = this.inputSourceParseErrors.iterator();

            while(i$.hasNext()) {
                String message = (String)i$.next();
                errorMessage.append(message);
            }

            throw new ConfigurationException(errorMessage.toString());
        }
    }

    private void loadYamlTrustedTerritories() {
        List<String> territories = this.getStringListParameterFromFile("TRUSTED_TERRITORIES");
        if (territories != null) {
            this.trustedTerritories = territories;
        }

    }

    private String getParameterFromFile(String key) {
        if (this.configurationFromFile == null) {
            return null;
        } else {
            Object fileValue = this.configurationFromFile.get(key);
            if (fileValue == null) {
                return null;
            } else {
                String value = fileValue.toString();
                return this.valueIsAllowed(key, value) ? value : null;
            }
        }
    }

    private Integer getIntParameterFromFile(String key) {
        String value = this.getParameterFromFile(key);
        return value == null ? null : new Integer(value);
    }

    private List<String> getStringListParameterFromFile(String key) {
        String value = this.getParameterFromFile(key);
        return value == null ? null : Arrays.asList(value.split("\\s*,\\s*"));
    }

    private void setConfigurationValue(String fileKey, ConfigurationParameter parameter) {
        if (this.configurationFromFile != null) {
            Object fileValue = this.configurationFromFile.get(fileKey);
            if (fileValue != null) {
                this.setConfigurationParameter(parameter, fileValue.toString());
            }

        }
    }

    private void setJDigiDocConfigurationValue(String key, String defaultValue) {
        String value = this.defaultIfNull(key, defaultValue);
        if (value != null) {
            this.jDigiDocConfiguration.put(key, value);
        }

    }

    private boolean loadOCSPCertificateEntry(String ocspsEntryName, LinkedHashMap ocsp, String prefix) {
        Object ocspEntry = ocsp.get(ocspsEntryName);
        if (ocspEntry == null) {
            return false;
        } else {
            this.jDigiDocConfiguration.put(prefix + "_" + ocspsEntryName, ocspEntry.toString());
            return true;
        }
    }

    private boolean getOCSPCertificates(String prefix, LinkedHashMap ocsp) {
        ArrayList<String> certificates = (ArrayList)ocsp.get("CERTS");
        if (certificates == null) {
            return false;
        } else {
            for(int j = 0; j < certificates.size(); ++j) {
                if (j == 0) {
                    this.setJDigiDocParameter(String.format("%s_CERT", prefix), (String)certificates.get(0));
                } else {
                    this.setJDigiDocParameter(String.format("%s_CERT_%s", prefix, j), (String)certificates.get(j));
                }
            }

            return true;
        }
    }

    private void loadCertificateAuthorityCerts(LinkedHashMap digiDocCA, String caPrefix) {
        this.log.debug("Loading CA certificates");
        ArrayList<String> certificateAuthorityCerts = this.getCACertsAsArray(digiDocCA);
        this.setJDigiDocParameter(String.format("%s_NAME", caPrefix), digiDocCA.get("NAME").toString());
        this.setJDigiDocParameter(String.format("%s_TRADENAME", caPrefix), digiDocCA.get("TRADENAME").toString());
        int numberOfCACertificates = certificateAuthorityCerts.size();
        this.setJDigiDocParameter(String.format("%s_CERTS", caPrefix), String.valueOf(numberOfCACertificates));

        for(int i = 0; i < numberOfCACertificates; ++i) {
            this.setJDigiDocParameter(String.format("%s_CERT%s", caPrefix, i + 1), (String)certificateAuthorityCerts.get(i));
        }

    }

    private ArrayList<String> getCACertsAsArray(LinkedHashMap digiDocCa) {
        return (ArrayList)digiDocCa.get("CERTS");
    }

    private void setConfigurationParameter(ConfigurationParameter parameter, String value) {
        if (StringUtils.isBlank(value)) {
            this.log.debug("Parameter <{}> has blank value, hence will not be registered", parameter);
        } else {
            this.log.debug("Setting parameter <{}> to <{}>", parameter, value);
            this.registry.put(parameter, value);
        }
    }

    private <T> T getConfigurationParameter(ConfigurationParameter parameter, Class<T> clazz) {
        String value = this.getConfigurationParameter(parameter);
        if (StringUtils.isNotBlank(value)) {
            if (clazz.isAssignableFrom(Integer.class)) {
                return (T) Integer.valueOf(value);
            } else if (clazz.isAssignableFrom(Long.class)) {
                return (T) Long.valueOf(value);
            } else {
                throw new RuntimeException(String.format("Type <%s> not supported", clazz.getSimpleName()));
            }
        } else {
            return null;
        }
    }

    private String getConfigurationParameter(ConfigurationParameter parameter) {
        if (!this.registry.containsKey(parameter)) {
            this.log.debug("Requested parameter <{}> not found", parameter);
            return null;
        } else {
            String value = (String)this.registry.get(parameter);
            this.log.debug("Requesting parameter <{}>. Returned value is <{}>", parameter, value);
            return value;
        }
    }

    private void initOcspAccessCertPasswordForJDigidoc() {
        char[] ocspAccessCertificatePassword = this.getOCSPAccessCertificatePassword();
        if (ocspAccessCertificatePassword != null && ocspAccessCertificatePassword.length > 0) {
            this.setJDigiDocConfigurationValue("DIGIDOC_PKCS12_PASSWD", String.valueOf(ocspAccessCertificatePassword));
        }

    }

    private String getParameter(String systemKey, String fileKey) {
        String valueFromJvm = System.getProperty(systemKey);
        String valueFromFile = this.getParameterFromFile(fileKey);
        this.log(valueFromJvm, valueFromFile, systemKey, fileKey);
        return valueFromJvm != null ? valueFromJvm : valueFromFile;
    }

    private void setJDigiDocParameter(String key, String value) {
        this.log.debug("Setting JDigiDoc parameter <{}> to <{}>", key, value);
        this.jDigiDocConfiguration.put(key, value);
    }

    private void log(Object jvmParam, Object fileParam, String sysParamKey, String fileKey) {
        if (jvmParam != null) {
            this.log.debug(String.format("JVM parameter <%s> detected and applied with value <%s>", sysParamKey, jvmParam));
        }

        if (jvmParam == null && fileParam != null) {
            this.log.debug(String.format("YAML file parameter <%s> detected and applied with value <%s>", fileKey, fileParam));
        }

    }

    public static enum Mode {
        TEST,
        PROD;

        private Mode() {
        }
    }
}
