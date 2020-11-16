// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cert.ocsp.RespID;
import sa.gov.nic.exceptions.CertificateNotFoundException;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import java.util.Arrays;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.asn1.x509.Extension;
import eu.europa.esig.dss.x509.CertificateToken;
import java.util.List;
import sa.gov.nic.SignatureProfile;
import java.util.Date;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import sa.gov.nic.X509Cert;
import org.slf4j.Logger;

public class TimemarkSignature extends BesSignature
{
    private static final Logger logger;
    private X509Cert ocspCertificate;
    private BasicOCSPResp ocspResponse;
    private Date ocspResponseTime;
    
    public TimemarkSignature(final XadesValidationReportGenerator xadesReportGenerator) {
        super(xadesReportGenerator);
    }
    
    @Override
    public SignatureProfile getProfile() {
        return SignatureProfile.LT_TM;
    }
    
    @Override
    public X509Cert getOCSPCertificate() {
        if (this.ocspCertificate != null) {
            return this.ocspCertificate;
        }
        this.initOcspResponse();
        if (this.ocspResponse == null) {
            return null;
        }
        return this.ocspCertificate = this.findOcspCertificate();
    }
    
    @Override
    public List<BasicOCSPResp> getOcspResponses() {
        return (List<BasicOCSPResp>)this.getDssSignature().getOCSPSource().getContainedOCSPResponses();
    }
    
    @Override
    public Date getOCSPResponseCreationTime() {
        if (this.ocspResponseTime != null) {
            return this.ocspResponseTime;
        }
        this.initOcspResponse();
        if (this.ocspResponse == null) {
            return null;
        }
        return this.ocspResponseTime = this.ocspResponse.getProducedAt();
    }
    
    @Override
    public Date getTrustedSigningTime() {
        return this.getOCSPResponseCreationTime();
    }
    
    private void initOcspResponse() {
        if (this.ocspResponse == null) {
            this.ocspResponse = this.findOcspResponse();
            if (this.ocspResponse == null) {
                TimemarkSignature.logger.warn("Signature is missing OCSP response");
            }
        }
    }
    
    private BasicOCSPResp findOcspResponse() {
        TimemarkSignature.logger.debug("Finding OCSP response");
        final List<BasicOCSPResp> containedOCSPResponses = this.getOcspResponses();
        if (containedOCSPResponses.isEmpty()) {
            TimemarkSignature.logger.debug("Contained OCSP responses is empty");
            return null;
        }
        if (containedOCSPResponses.size() > 1) {
            TimemarkSignature.logger.warn("Signature contains more than one OCSP response: " + containedOCSPResponses.size() + ". Using the first one.");
        }
        return containedOCSPResponses.get(0);
    }
    
    private X509Cert findOcspCertificate() {
        String rId = "";
        final String signatureId = this.getDssSignature().getId();
        try {
            final RespID responderId = this.ocspResponse.getResponderId();
            rId = responderId.toString();
            final String primitiveName = this.getCN(responderId.toASN1Primitive().getName());
            final byte[] keyHash = responderId.toASN1Primitive().getKeyHash();
            final boolean isKeyHash = this.useKeyHashForOCSP(primitiveName, keyHash);
            if (isKeyHash) {
                TimemarkSignature.logger.debug("Using keyHash {} for OCSP certificate match", (Object)keyHash);
            }
            else {
                TimemarkSignature.logger.debug("Using ASN1Primitive {} for OCSP certificate match", (Object)primitiveName);
            }
            for (final CertificateToken cert : this.getDssSignature().getCertificates()) {
                if (isKeyHash) {
                    final ASN1Primitive skiPrimitive = JcaX509ExtensionUtils.parseExtensionValue(cert.getCertificate().getExtensionValue(Extension.subjectKeyIdentifier.getId()));
                    final byte[] keyIdentifier = ASN1OctetString.getInstance((Object)skiPrimitive.getEncoded()).getOctets();
                    if (Arrays.equals(keyHash, keyIdentifier)) {
                        return new X509Cert(cert.getCertificate());
                    }
                    continue;
                }
                else {
                    final String certCn = this.getCN(new X500Name(cert.getSubjectX500Principal().getName()));
                    if (StringUtils.equals((CharSequence)certCn, (CharSequence)primitiveName)) {
                        return new X509Cert(cert.getCertificate());
                    }
                    continue;
                }
            }
        }
        catch (IOException e) {
            TimemarkSignature.logger.error("Unable to wrap and extract SubjectKeyIdentifier from certificate - technical error. {}", (Throwable)e);
        }
        TimemarkSignature.logger.error("OCSP certificate for " + rId + " was not found in TSL");
        throw new CertificateNotFoundException("OCSP certificate for " + rId + " was not found in TSL", signatureId);
    }
    
    private boolean useKeyHashForOCSP(final String primitiveName, final byte[] keyHash) {
        return keyHash != null && keyHash.length > 0 && (primitiveName == null || primitiveName.trim().length() == 0);
    }
    
    private String getCN(final X500Name x500Name) {
        if (x500Name == null) {
            return null;
        }
        final RDN[] rdNs = x500Name.getRDNs(new ASN1ObjectIdentifier("2.5.4.3"));
        if (rdNs == null || rdNs.length == 0) {
            return null;
        }
        final AttributeTypeAndValue[] typesAndValues = rdNs[0].getTypesAndValues();
        if (typesAndValues == null || typesAndValues.length == 0) {
            return null;
        }
        return typesAndValues[0].getValue().toString();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TimemarkSignature.class);
    }
}
