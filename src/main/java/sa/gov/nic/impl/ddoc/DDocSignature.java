// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.slf4j.LoggerFactory;
import ee.sk.digidoc.CertValue;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.SignatureValidationResult;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.exceptions.NotYetImplementedException;
import java.util.Date;
import java.security.cert.X509Certificate;
import sa.gov.nic.X509Cert;
import org.slf4j.Logger;
import sa.gov.nic.Signature;

public class DDocSignature implements Signature
{
    private static final Logger logger;
    private X509Cert certificate;
    private final ee.sk.digidoc.Signature origin;
    private int indexInArray;
    
    public DDocSignature(final ee.sk.digidoc.Signature signature) {
        this.indexInArray = 0;
        DDocSignature.logger.debug("");
        this.origin = signature;
    }
    
    public void setCertificate(final X509Cert cert) {
        DDocSignature.logger.debug("");
        this.certificate = cert;
    }
    
    @Override
    public String getCity() {
        DDocSignature.logger.debug("getCity");
        String city = null;
        if (this.origin.getSignedProperties().getSignatureProductionPlace() != null) {
            city = this.origin.getSignedProperties().getSignatureProductionPlace().getCity();
        }
        return (city == null) ? "" : city;
    }
    
    @Override
    public String getCountryName() {
        DDocSignature.logger.debug("getCountryName");
        String countryName = null;
        if (this.origin.getSignedProperties().getSignatureProductionPlace() != null) {
            countryName = this.origin.getSignedProperties().getSignatureProductionPlace().getCountryName();
        }
        return (countryName == null) ? "" : countryName;
    }
    
    @Override
    public String getId() {
        DDocSignature.logger.debug("getId");
        String id = this.origin.getId();
        if (id == null) {
            id = "";
        }
        return id;
    }
    
    @Override
    public byte[] getOCSPNonce() {
        DDocSignature.logger.debug("getOCSPNonce");
        return null;
    }
    
    @Override
    public X509Cert getOCSPCertificate() {
        DDocSignature.logger.debug("getOCSPCertificate");
        final X509Certificate x509Certificate = this.origin.findResponderCert();
        return (x509Certificate != null) ? new X509Cert(x509Certificate) : null;
    }
    
    @Deprecated
    @Override
    public String getPolicy() {
        DDocSignature.logger.debug("getPolicy");
        return "";
    }
    
    @Override
    public String getPostalCode() {
        String postalCode = null;
        if (this.origin.getSignedProperties().getSignatureProductionPlace() != null) {
            postalCode = this.origin.getSignedProperties().getSignatureProductionPlace().getPostalCode();
        }
        DDocSignature.logger.debug("Postal code: " + postalCode);
        return (postalCode == null) ? "" : postalCode;
    }
    
    @Override
    public Date getOCSPResponseCreationTime() {
        final Date date = this.origin.getSignatureProducedAtTime();
        DDocSignature.logger.debug("OCSP response creation time: " + date);
        return date;
    }
    
    @Deprecated
    @Override
    public Date getProducedAt() {
        return this.getOCSPResponseCreationTime();
    }
    
    @Override
    public Date getTimeStampCreationTime() {
        DDocSignature.logger.warn("Not yet implemented");
        throw new NotYetImplementedException();
    }
    
    @Override
    public Date getTrustedSigningTime() {
        return this.getOCSPResponseCreationTime();
    }
    
    @Override
    public SignatureProfile getProfile() {
        DDocSignature.logger.debug("Profile is LT_TM");
        return SignatureProfile.LT_TM;
    }
    
    @Override
    public String getSignatureMethod() {
        DDocSignature.logger.debug("getSignatureMethod");
        final String signatureMethod = this.origin.getSignedInfo().getSignatureMethod();
        DDocSignature.logger.debug("Signature method: " + signatureMethod);
        return (signatureMethod == null) ? "" : signatureMethod;
    }
    
    @Override
    public List<String> getSignerRoles() {
        DDocSignature.logger.debug("getSignerRoles");
        final List<String> roles = new ArrayList<String>();
        for (int numberOfRoles = this.origin.getSignedProperties().countClaimedRoles(), i = 0; i < numberOfRoles; ++i) {
            roles.add(this.origin.getSignedProperties().getClaimedRole(i));
        }
        return roles;
    }
    
    @Override
    public X509Cert getSigningCertificate() {
        DDocSignature.logger.debug("getSigningCertificate");
        return this.certificate;
    }
    
    @Override
    public Date getClaimedSigningTime() {
        DDocSignature.logger.debug("getClaimedSigningTime");
        return this.origin.getSignedProperties().getSigningTime();
    }
    
    @Override
    public Date getSigningTime() {
        return this.getClaimedSigningTime();
    }
    
    @Deprecated
    @Override
    public URI getSignaturePolicyURI() {
        DDocSignature.logger.debug("getSignaturePolicyURI");
        return null;
    }
    
    @Override
    public String getStateOrProvince() {
        DDocSignature.logger.debug("getStateOrProvince");
        String stateOrProvince = null;
        if (this.origin.getSignedProperties().getSignatureProductionPlace() != null) {
            stateOrProvince = this.origin.getSignedProperties().getSignatureProductionPlace().getStateOrProvince();
        }
        return (stateOrProvince == null) ? "" : stateOrProvince;
    }
    
    @Override
    public X509Cert getTimeStampTokenCertificate() {
        DDocSignature.logger.warn("Not yet implemented");
        throw new NotYetImplementedException();
    }
    
    @Override
    public SignatureValidationResult validateSignature() {
        DDocSignature.logger.debug("");
        final List<DigiDoc4JException> validationErrors = new ArrayList<DigiDoc4JException>();
        final ArrayList validationResult = this.origin.verify(this.origin.getSignedDoc(), true, true);
        for (final Object exception : validationResult) {
            final String errorMessage = exception.toString();
            DDocSignature.logger.info(errorMessage);
            validationErrors.add(new DigiDoc4JException(errorMessage));
        }
        DDocSignature.logger.info("Signature has " + validationErrors.size() + " validation errors");
        final SignatureValidationResult result = new SignatureValidationResult();
        result.setErrors(validationErrors);
        return result;
    }
    
    @Deprecated
    @Override
    public List<DigiDoc4JException> validate() {
        return this.validateSignature().getErrors();
    }
    
    public CertValue getCertValueOfType(final int type) {
        DDocSignature.logger.debug("type: " + type);
        return this.origin.getCertValueOfType(type);
    }
    
    @Override
    public byte[] getAdESSignature() {
        DDocSignature.logger.debug("getAdESSignature");
        return this.origin.getOrigContent();
    }
    
    @Deprecated
    @Override
    public byte[] getRawSignature() {
        return this.getAdESSignature();
    }
    
    public int getIndexInArray() {
        return this.indexInArray;
    }
    
    public void setIndexInArray(final int indexInArray) {
        this.indexInArray = indexInArray;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DDocSignature.class);
    }
}
