// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DSSUtils;
import org.w3c.dom.NodeList;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import org.w3c.dom.Node;
import eu.europa.esig.dss.DomUtils;
import java.util.HashSet;
import org.apache.xml.security.signature.Reference;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.apache.commons.codec.binary.Base64;
import sa.gov.nic.SignatureProfile;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import eu.europa.esig.dss.x509.CertificateToken;
import java.util.Set;
import sa.gov.nic.X509Cert;
import eu.europa.esig.dss.xades.XPathQueryHolder;
import org.w3c.dom.Element;
import eu.europa.esig.dss.validation.SignatureProductionPlace;
import org.slf4j.Logger;

public class BesSignature extends DssXadesSignature
{
    private static final Logger logger;
    private SignatureProductionPlace signerLocation;
    private Element signatureElement;
    private XPathQueryHolder xPathQueryHolder;
    private X509Cert signingCertificate;
    private Set<CertificateToken> encapsulatedCertificates;
    
    public BesSignature(final XadesValidationReportGenerator xadesReportGenerator) {
        super(xadesReportGenerator);
        final XAdESSignature dssSignature = xadesReportGenerator.openDssSignature();
        this.signatureElement = dssSignature.getSignatureElement();
        this.xPathQueryHolder = dssSignature.getXPathQueryHolder();
        BesSignature.logger.debug("Using xpath query holder: " + this.xPathQueryHolder.getClass());
    }
    
    @Override
    public String getId() {
        return this.getDssSignature().getId();
    }
    
    @Override
    public String getSignatureMethod() {
        String xmlId = null;
        final DigestAlgorithm algorithm = this.getDssSignature().getDigestAlgorithm();
        if (algorithm != null) {
            xmlId = algorithm.getXmlId();
        }
        return (xmlId == null) ? "" : xmlId;
    }
    
    @Override
    public Date getSigningTime() {
        return this.getDssSignature().getSigningTime();
    }
    
    @Override
    public String getCity() {
        return (this.getSignerLocation() == null) ? "" : this.getSignerLocation().getCity();
    }
    
    @Override
    public String getStateOrProvince() {
        return (this.getSignerLocation() == null) ? "" : this.getSignerLocation().getStateOrProvince();
    }
    
    @Override
    public String getPostalCode() {
        return (this.getSignerLocation() == null) ? "" : this.getSignerLocation().getPostalCode();
    }
    
    @Override
    public String getCountryName() {
        return (this.getSignerLocation() == null) ? "" : this.getSignerLocation().getCountryName();
    }
    
    @Override
    public List<String> getSignerRoles() {
        final String[] claimedSignerRoles = this.getDssSignature().getClaimedSignerRoles();
        return (claimedSignerRoles == null) ? Collections.emptyList() : Arrays.asList(claimedSignerRoles);
    }
    
    @Override
    public X509Cert getSigningCertificate() {
        if (this.signingCertificate != null) {
            return this.signingCertificate;
        }
        final CertificateToken keyInfoCertificate = this.findKeyInfoCertificate();
        if (keyInfoCertificate == null) {
            BesSignature.logger.warn("Signing certificate not found");
            return null;
        }
        final X509Certificate certificate = keyInfoCertificate.getCertificate();
        return this.signingCertificate = new X509Cert(certificate);
    }
    
    @Override
    public SignatureProfile getProfile() {
        return SignatureProfile.B_BES;
    }
    
    @Override
    public byte[] getSignatureValue() {
        BesSignature.logger.debug("Getting signature value");
        final Element signatureValueElement = this.getDssSignature().getSignatureValue();
        final String textContent = signatureValueElement.getTextContent();
        return Base64.decodeBase64(textContent);
    }
    
    @Override
    public Date getTrustedSigningTime() {
        BesSignature.logger.info("B_BES signature does not contain OCSP response time or Timestamp to provide trusted signing time");
        return null;
    }
    
    @Override
    public Date getOCSPResponseCreationTime() {
        BesSignature.logger.info("The signature does not contain OCSP response");
        return null;
    }
    
    @Override
    public X509Cert getOCSPCertificate() {
        BesSignature.logger.info("The signature does not contain OCSP response");
        return null;
    }
    
    @Override
    public List<BasicOCSPResp> getOcspResponses() {
        BesSignature.logger.info("The signature does not contain OCSP response");
        return Collections.emptyList();
    }
    
    @Override
    public Date getTimeStampCreationTime() {
        BesSignature.logger.info("The signature does not contain Timestamp");
        return null;
    }
    
    @Override
    public X509Cert getTimeStampTokenCertificate() {
        BesSignature.logger.info("The signature does not contain Timestamp");
        return null;
    }
    
    @Override
    public List<Reference> getReferences() {
        return (List<Reference>)this.getDssSignature().getReferences();
    }
    
    protected Element getSignatureElement() {
        return this.signatureElement;
    }
    
    protected XPathQueryHolder getxPathQueryHolder() {
        return this.xPathQueryHolder;
    }
    
    protected Set<CertificateToken> getEncapsulatedCertificates() {
        if (this.encapsulatedCertificates == null) {
            BesSignature.logger.debug("Finding encapsulated certificates");
            this.encapsulatedCertificates = this.findCertificates(this.xPathQueryHolder.XPATH_ENCAPSULATED_X509_CERTIFICATE);
            BesSignature.logger.debug("Found " + this.encapsulatedCertificates.size() + " encapsulated certificates");
        }
        return this.encapsulatedCertificates;
    }
    
    private CertificateToken findKeyInfoCertificate() {
        BesSignature.logger.debug("Finding key info certificate");
        this.xPathQueryHolder.getClass();
        final Set<CertificateToken> keyInfoCertificates = this.findCertificates("./ds:KeyInfo/ds:X509Data/ds:X509Certificate");
        if (keyInfoCertificates.isEmpty()) {
            BesSignature.logger.debug("Signing certificate not found");
            return null;
        }
        if (keyInfoCertificates.size() > 1) {
            BesSignature.logger.warn("Found more than one signing certificate in the key info block: " + keyInfoCertificates.size());
        }
        return keyInfoCertificates.iterator().next();
    }
    
    protected Set<CertificateToken> findCertificates(final String xPath) {
        final Set<CertificateToken> certificates = new HashSet<CertificateToken>();
        final NodeList nodeList = DomUtils.getNodeList((Node)this.signatureElement, xPath);
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Element certificateElement = (Element)nodeList.item(i);
            final CertificateToken certToken = this.createCertificateToken(certificateElement);
            if (!certificates.contains(certToken)) {
                final String idIdentifier = DSSXMLUtils.getIDIdentifier(certificateElement);
                certToken.setXmlId(idIdentifier);
                certificates.add(certToken);
            }
        }
        return certificates;
    }
    
    private CertificateToken createCertificateToken(final Element certificateElement) {
        final byte[] derEncoded = Base64.decodeBase64(certificateElement.getTextContent());
        return DSSUtils.loadCertificate(derEncoded);
    }
    
    private SignatureProductionPlace getSignerLocation() {
        if (this.signerLocation == null) {
            BesSignature.logger.debug("Getting signature production place");
            this.signerLocation = this.getDssSignature().getSignatureProductionPlace();
        }
        return this.signerLocation;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BesSignature.class);
    }
}
