// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import eu.europa.esig.dss.client.http.proxy.ProxyProperties;
import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import sa.gov.nic.Configuration;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import org.slf4j.Logger;

public class DataLoaderDecorator
{
    private static final Logger logger;
    
    public static void decorateWithProxySettings(final CommonsDataLoader dataLoader, final Configuration configuration) {
        if (configuration.isNetworkProxyEnabled()) {
            final ProxyConfig proxyConfig = create(configuration);
            dataLoader.setProxyConfig(proxyConfig);
        }
    }
    
    private static ProxyConfig create(final Configuration configuration) {
        DataLoaderDecorator.logger.debug("Creating proxy settings");
        final ProxyConfig proxy = new ProxyConfig();
        final ProxyProperties httpProxyProperties = new ProxyProperties();
        if (configuration.getHttpProxyPort() != null && StringUtils.isNotBlank((CharSequence)configuration.getHttpProxyHost())) {
            httpProxyProperties.setHost(configuration.getHttpProxyHost());
            httpProxyProperties.setPort((int)configuration.getHttpProxyPort());
        }
        final ProxyProperties httpsProxyProperties = new ProxyProperties();
        if (configuration.getHttpsProxyPort() != null && StringUtils.isNotBlank((CharSequence)configuration.getHttpsProxyHost())) {
            httpsProxyProperties.setHost(configuration.getHttpsProxyHost());
            httpsProxyProperties.setPort((int)configuration.getHttpsProxyPort());
        }
        if (StringUtils.isNotBlank((CharSequence)configuration.getHttpProxyUser()) && StringUtils.isNotBlank((CharSequence)configuration.getHttpProxyPassword())) {
            httpProxyProperties.setUser(configuration.getHttpProxyUser());
            httpProxyProperties.setPassword(configuration.getHttpProxyPassword());
            httpsProxyProperties.setUser(configuration.getHttpProxyUser());
            httpsProxyProperties.setPassword(configuration.getHttpProxyPassword());
        }
        proxy.setHttpProperties(httpProxyProperties);
        proxy.setHttpsProperties(httpsProxyProperties);
        return proxy;
    }
    
    public static void decorateWithSslSettings(final CommonsDataLoader dataLoader, final Configuration configuration) {
        if (configuration.isSslConfigurationEnabled()) {
            DataLoaderDecorator.logger.debug("Configuring SSL");
            dataLoader.setSslKeystorePath(configuration.getSslKeystorePath());
            dataLoader.setSslTruststorePath(configuration.getSslTruststorePath());
            if (configuration.getSslKeystoreType() != null) {
                dataLoader.setSslKeystoreType(configuration.getSslKeystoreType());
            }
            if (configuration.getSslKeystorePassword() != null) {
                dataLoader.setSslKeystorePassword(configuration.getSslKeystorePassword());
            }
            if (configuration.getSslTruststoreType() != null) {
                dataLoader.setSslTruststoreType(configuration.getSslTruststoreType());
            }
            if (configuration.getSslTruststorePassword() != null) {
                dataLoader.setSslTruststorePassword(configuration.getSslTruststorePassword());
            }
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DataLoaderDecorator.class);
    }
}
