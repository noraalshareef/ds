// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.utils.Helper;
import sa.gov.nic.SignatureProfile;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import java.io.Closeable;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import eu.europa.esig.dss.DSSException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import java.io.InputStream;
import org.apache.http.entity.InputStreamEntity;
import java.io.ByteArrayInputStream;
import org.apache.http.client.methods.HttpPost;
import java.net.URI;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;

public class SkDataLoader extends CommonsDataLoader
{
    private static final Logger logger;
    public static final String TIMESTAMP_CONTENT_TYPE = "application/timestamp-query";
    private String userAgent;
    
    public static SkDataLoader createOcspDataLoader(final Configuration configuration) {
        final SkDataLoader dataLoader = new SkDataLoader(configuration);
        dataLoader.setContentType("application/ocsp-request");
        return dataLoader;
    }
    
    public static SkDataLoader createTimestampDataLoader(final Configuration configuration) {
        final SkDataLoader dataLoader = new SkDataLoader(configuration);
        dataLoader.setContentType("application/timestamp-query");
        return dataLoader;
    }
    
    protected SkDataLoader(final Configuration configuration) {
        DataLoaderDecorator.decorateWithProxySettings(this, configuration);
        DataLoaderDecorator.decorateWithSslSettings(this, configuration);
    }
    
    public byte[] post(final String url, final byte[] content) throws DSSException {
        SkDataLoader.logger.info("Getting OCSP response from " + url);
        if (this.userAgent == null) {
            throw new TechnicalException("User Agent must be set for OCSP requests");
        }
        HttpPost httpRequest = null;
        HttpResponse httpResponse = null;
        CloseableHttpClient client = null;
        try {
            final URI uri = URI.create(url.trim());
            httpRequest = new HttpPost(uri);
            httpRequest.setHeader("User-Agent", this.userAgent);
            final ByteArrayInputStream bis = new ByteArrayInputStream(content);
            final HttpEntity httpEntity = (HttpEntity)new InputStreamEntity((InputStream)bis, (long)content.length);
            final HttpEntity requestEntity = (HttpEntity)new BufferedHttpEntity(httpEntity);
            httpRequest.setEntity(requestEntity);
            if (this.contentType != null) {
                httpRequest.setHeader("Content-Type", this.contentType);
            }
            client = this.getHttpClient(url);
            httpResponse = this.getHttpResponse(client, (HttpUriRequest)httpRequest, url);
            final byte[] returnedBytes = this.readHttpResponse(url, httpResponse);
            return returnedBytes;
        }
        catch (IOException e) {
            throw new DSSException((Throwable)e);
        }
        finally {
            try {
                if (httpRequest != null) {
                    httpRequest.releaseConnection();
                }
                if (httpResponse != null) {
                    EntityUtils.consumeQuietly(httpResponse.getEntity());
                }
            }
            finally {
                IOUtils.closeQuietly((Closeable)client);
            }
        }
    }
    
    public void setUserAgentSignatureProfile(final SignatureProfile signatureProfile) {
        this.userAgent = Helper.createBDocUserAgent(signatureProfile);
    }
    
    public void setAsicSUserAgentSignatureProfile() {
        this.userAgent = Helper.createBDocAsicSUserAgent();
    }
    
    public String getUserAgent() {
        return this.userAgent;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SkDataLoader.class);
    }
}
