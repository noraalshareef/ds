// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.ocsp;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import sa.gov.nic.exceptions.DigiDoc4JException;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.cert.ocsp.SingleResp;
import java.util.Date;
import java.io.IOException;
import org.bouncycastle.cert.ocsp.OCSPException;
import eu.europa.esig.dss.x509.RevocationToken;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;
import eu.europa.esig.dss.x509.ocsp.OCSPToken;
import org.bouncycastle.operator.ContentSigner;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import org.bouncycastle.cert.ocsp.CertificateID;
import eu.europa.esig.dss.DSSException;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import sa.gov.nic.exceptions.ConfigurationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import eu.europa.esig.dss.DSSRevocationUtils;
import org.bouncycastle.asn1.x509.Extension;
import eu.europa.esig.dss.x509.CertificateToken;
import sa.gov.nic.Configuration;
import sa.gov.nic.impl.asic.SkDataLoader;
import org.slf4j.Logger;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;

public abstract class SKOnlineOCSPSource implements OCSPSource
{
    private static final Logger logger;
    private SkDataLoader dataLoader;
    private Configuration configuration;
    
    public SKOnlineOCSPSource(final Configuration configuration) {
        this.configuration = configuration;
        SKOnlineOCSPSource.logger.debug("Initialized SK Online OCSP source");
    }
    
    public String getAccessLocation() {
        SKOnlineOCSPSource.logger.debug("");
        String location = "http://demo.sk.ee/ocsp";
        if (this.configuration != null) {
            location = this.configuration.getOcspSource();
        }
        SKOnlineOCSPSource.logger.debug("OCSP Access location: " + location);
        return location;
    }
    
    private byte[] buildOCSPRequest(final CertificateToken signCert, final CertificateToken issuerCert, final Extension nonceExtension) throws DSSException {
        try {
            SKOnlineOCSPSource.logger.debug("Building OCSP request");
            final CertificateID certId = DSSRevocationUtils.getOCSPCertificateID(signCert, issuerCert);
            final OCSPReqBuilder ocspReqBuilder = new OCSPReqBuilder();
            ocspReqBuilder.addRequest(certId);
            ocspReqBuilder.setRequestExtensions(new Extensions(nonceExtension));
            if (!this.configuration.hasToBeOCSPRequestSigned()) {
                return ocspReqBuilder.build().getEncoded();
            }
            SKOnlineOCSPSource.logger.info("Using signed OCSP request");
            final JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA1withRSA");
            if (!this.configuration.isOCSPSigningConfigurationAvailable()) {
                throw new ConfigurationException("Configuration needed for OCSP request signing is not complete.");
            }
            final DSSPrivateKeyEntry keyEntry = this.getOCSPAccessCertificatePrivateKey();
            final PrivateKey privateKey = ((KSPrivateKeyEntry)keyEntry).getPrivateKey();
            final X509Certificate ocspSignerCert = keyEntry.getCertificate().getCertificate();
            final ContentSigner contentSigner = signerBuilder.build(privateKey);
            final X509CertificateHolder[] chain = { new X509CertificateHolder(ocspSignerCert.getEncoded()) };
            final GeneralName generalName = new GeneralName(new JcaX509CertificateHolder(ocspSignerCert).getSubject());
            ocspReqBuilder.setRequestorName(generalName);
            return ocspReqBuilder.build(contentSigner, chain).getEncoded();
        }
        catch (Exception e) {
            throw new DSSException((Throwable)e);
        }
    }
    
    public OCSPToken getOCSPToken(final CertificateToken certificateToken, final CertificateToken issuerCertificateToken) {
        SKOnlineOCSPSource.logger.debug("Getting OCSP token");
        if (this.dataLoader == null) {
            throw new RuntimeException("Data loader is null");
        }
        try {
            final String dssIdAsString = certificateToken.getDSSIdAsString();
            if (SKOnlineOCSPSource.logger.isTraceEnabled()) {
                SKOnlineOCSPSource.logger.trace("--> OnlineOCSPSource queried for " + dssIdAsString);
            }
            final String ocspUri = this.getAccessLocation();
            SKOnlineOCSPSource.logger.debug("Getting OCSP token from URI: " + ocspUri);
            if (ocspUri == null) {
                return null;
            }
            final Extension nonceExtension = this.createNonce();
            final byte[] content = this.buildOCSPRequest(certificateToken, issuerCertificateToken, nonceExtension);
            final byte[] ocspRespBytes = this.dataLoader.post(ocspUri, content);
            final OCSPResp ocspResp = new OCSPResp(ocspRespBytes);
            final BasicOCSPResp basicOCSPResp = (BasicOCSPResp)ocspResp.getResponseObject();
            if (basicOCSPResp == null) {
                SKOnlineOCSPSource.logger.error("OCSP response is empty");
                return null;
            }
            this.checkNonce(basicOCSPResp, nonceExtension);
            Date bestUpdate = null;
            SingleResp bestSingleResp = null;
            final CertificateID certId = DSSRevocationUtils.getOCSPCertificateID(certificateToken, issuerCertificateToken);
            for (final SingleResp singleResp : basicOCSPResp.getResponses()) {
                if (DSSRevocationUtils.matches(certId, singleResp)) {
                    final Date thisUpdate = singleResp.getThisUpdate();
                    if (bestUpdate == null || thisUpdate.after(bestUpdate)) {
                        bestSingleResp = singleResp;
                        bestUpdate = thisUpdate;
                    }
                }
            }
            if (bestSingleResp != null) {
                final OCSPToken ocspToken = new OCSPToken();
                ocspToken.setBasicOCSPResp(basicOCSPResp);
                ocspToken.setCertId(certId);
                ocspToken.setSourceURL(ocspUri);
                certificateToken.addRevocationToken((RevocationToken)ocspToken);
                return ocspToken;
            }
        }
        catch (OCSPException e) {
            SKOnlineOCSPSource.logger.error("OCSP error: " + e.getMessage(), (Throwable)e);
        }
        catch (IOException e2) {
            throw new DSSException((Throwable)e2);
        }
        return null;
    }
    
    protected void checkNonce(final BasicOCSPResp basicOCSPResp, final Extension expectedNonceExtension) {
        final Extension extension = basicOCSPResp.getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);
        final DEROctetString expectedNonce = (DEROctetString)expectedNonceExtension.getExtnValue();
        final DEROctetString receivedNonce = (DEROctetString)extension.getExtnValue();
        if (!receivedNonce.equals((Object)expectedNonce)) {
            throw new DigiDoc4JException("The OCSP request was the victim of replay attack: nonce[sent:" + expectedNonce + "," + " received:" + receivedNonce);
        }
    }
    
    abstract Extension createNonce();
    
    private DSSPrivateKeyEntry getOCSPAccessCertificatePrivateKey() throws IOException {
        final Pkcs12SignatureToken signatureTokenConnection = new Pkcs12SignatureToken(this.configuration.getOCSPAccessCertificateFileName(), this.configuration.getOCSPAccessCertificatePasswordAsString());
        return signatureTokenConnection.getKeys().get(0);
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    public SkDataLoader getDataLoader() {
        return this.dataLoader;
    }
    
    public void setDataLoader(final SkDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SKOnlineOCSPSource.class);
    }
}
