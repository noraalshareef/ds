// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import sa.gov.nic.exceptions.CertificateNotFoundException;
import eu.europa.esig.dss.DSSException;
import sa.gov.nic.exceptions.TechnicalException;
import org.bouncycastle.cms.CMSSignedData;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import eu.europa.esig.dss.DomUtils;
import java.util.Date;
import java.security.cert.X509Certificate;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import eu.europa.esig.dss.validation.TimestampToken;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.X509Cert;
import org.bouncycastle.tsp.TimeStampToken;
import eu.europa.esig.dss.xades.XPathQueryHolder;
import org.w3c.dom.Element;
import org.slf4j.Logger;

public class TimestampSignature extends TimemarkSignature
{
    private static final Logger logger;
    private Element signatureElement;
    private XPathQueryHolder xPathQueryHolder;
    private TimeStampToken timeStampToken;
    private X509Cert timestampTokenCertificate;
    
    public TimestampSignature(final XadesValidationReportGenerator xadesReportGenerator) {
        super(xadesReportGenerator);
        this.xPathQueryHolder = this.getxPathQueryHolder();
        this.signatureElement = this.getSignatureElement();
    }
    
    @Override
    public SignatureProfile getProfile() {
        return SignatureProfile.LT;
    }
    
    @Override
    public X509Cert getTimeStampTokenCertificate() {
        if (this.timestampTokenCertificate != null) {
            return this.timestampTokenCertificate;
        }
        final XAdESSignature origin = this.getDssSignature();
        if (origin.getSignatureTimestamps() == null || origin.getSignatureTimestamps().isEmpty()) {
            this.throwTimestampNotFoundException(origin.getId());
        }
        final TimestampToken timestampToken = origin.getSignatureTimestamps().get(0);
        final CertificateToken issuerToken = timestampToken.getIssuerToken();
        if (issuerToken == null) {
            return this.throwTimestampNotFoundException(origin.getId());
        }
        final X509Certificate certificate = issuerToken.getCertificate();
        return this.timestampTokenCertificate = new X509Cert(certificate);
    }
    
    @Override
    public Date getTimeStampCreationTime() {
        if (this.timeStampToken == null) {
            this.timeStampToken = this.findTimestampToken();
        }
        if (this.timeStampToken == null || this.timeStampToken.getTimeStampInfo() == null) {
            TimestampSignature.logger.warn("Timestamp token was not found");
            return null;
        }
        return this.timeStampToken.getTimeStampInfo().getGenTime();
    }
    
    @Override
    public Date getTrustedSigningTime() {
        return this.getTimeStampCreationTime();
    }
    
    private TimeStampToken findTimestampToken() {
        TimestampSignature.logger.debug("Finding timestamp token");
        final NodeList timestampNodes = DomUtils.getNodeList((Node)this.signatureElement, this.xPathQueryHolder.XPATH_SIGNATURE_TIMESTAMP);
        if (timestampNodes.getLength() == 0) {
            TimestampSignature.logger.warn("Signature timestamp element was not found");
            return null;
        }
        if (timestampNodes.getLength() > 1) {
            TimestampSignature.logger.warn("Signature contains more than one timestamp: " + timestampNodes.getLength() + ". Using only the first one");
        }
        final Node timestampNode = timestampNodes.item(0);
        final Element timestampTokenNode = DomUtils.getElement(timestampNode, this.xPathQueryHolder.XPATH__ENCAPSULATED_TIMESTAMP);
        if (timestampTokenNode == null) {
            TimestampSignature.logger.warn("The timestamp cannot be extracted from the signature");
            return null;
        }
        final String base64EncodedTimestamp = timestampTokenNode.getTextContent();
        return this.createTimeStampToken(base64EncodedTimestamp);
    }
    
    private TimeStampToken createTimeStampToken(final String base64EncodedTimestamp) throws DSSException {
        TimestampSignature.logger.debug("Creating timestamp token");
        try {
            final byte[] tokenBytes = Base64.decodeBase64(base64EncodedTimestamp);
            final CMSSignedData signedData = new CMSSignedData(tokenBytes);
            return new TimeStampToken(signedData);
        }
        catch (Exception e) {
            TimestampSignature.logger.error("Error parsing timestamp token: " + e.getMessage());
            throw new TechnicalException("Error parsing timestamp token", e);
        }
    }
    
    private X509Cert throwTimestampNotFoundException(final String sigId) {
        TimestampSignature.logger.error("TimeStamp certificate not found, Signature id: " + sigId);
        throw new CertificateNotFoundException("TimeStamp certificate not found", sigId);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TimestampSignature.class);
    }
}
