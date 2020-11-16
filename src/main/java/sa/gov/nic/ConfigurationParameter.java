// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

public enum ConfigurationParameter
{
    ConnectionTimeoutInMillis, 
    SocketTimeoutInMillis, 
    TslCacheExpirationTimeInMillis, 
    TslKeyStorePassword, 
    RevocationAndTimestampDeltaInMinutes, 
    AllowedTimestampAndOCSPResponseDeltaInMinutes, 
    SignatureProfile, 
    SignatureDigestAlgorithm, 
    TspSource, 
    TslLocation, 
    TslKeyStoreLocation, 
    ValidationPolicy, 
    OcspSource, 
    OcspAccessCertificateFile, 
    OcspAccessCertificatePassword, 
    HttpProxyHost, 
    HttpProxyPort, 
    HttpsProxyHost, 
    HttpsProxyPort, 
    HttpProxyUser, 
    HttpProxyPassword, 
    SslKeystoreType, 
    SslTruststoreType, 
    SslKeystorePath, 
    SslKeystorePassword, 
    SslTruststorePath, 
    SslTruststorePassword, 
    SignOcspRequests, 
    TspsCount, 
    TspCountrySource, 
    TspCountryKeystorePath, 
    TspCountryKeystoreType, 
    TspCountryKeystorePassword;
}
