// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DigestAlgorithm;
import sa.gov.nic.impl.asic.xades.validation.XadesValidationResult;
import java.io.IOException;
import sa.gov.nic.exceptions.TechnicalException;
import org.apache.commons.io.IOUtils;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.net.URI;
import java.util.List;
import sa.gov.nic.SignatureProfile;
import java.util.Date;
import sa.gov.nic.X509Cert;
import sa.gov.nic.exceptions.NotYetImplementedException;
import eu.europa.esig.dss.DSSDocument;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidator;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.SignatureValidationResult;
import org.slf4j.Logger;
import sa.gov.nic.Signature;

public class AsicSignature implements Signature
{
    private static final Logger logger;
    private SignatureValidationResult validationResult;
    private XadesSignature xadesSignature;
    private SignatureValidator validator;
    private DSSDocument signatureDocument;
    
    public AsicSignature(final XadesSignature xadesSignature, final SignatureValidator validator) {
        this.xadesSignature = xadesSignature;
        this.validator = validator;
    }
    
    @Override
    public String getCity() {
        return this.xadesSignature.getCity();
    }
    
    @Override
    public String getCountryName() {
        return this.xadesSignature.getCountryName();
    }
    
    @Override
    public String getId() {
        return this.xadesSignature.getId();
    }
    
    @Override
    public byte[] getOCSPNonce() {
        AsicSignature.logger.warn("Not yet implemented");
        throw new NotYetImplementedException();
    }
    
    @Override
    public X509Cert getOCSPCertificate() {
        return this.xadesSignature.getOCSPCertificate();
    }
    
    @Deprecated
    @Override
    public String getPolicy() {
        AsicSignature.logger.warn("Not yet implemented");
        throw new NotYetImplementedException();
    }
    
    @Override
    public String getPostalCode() {
        return this.xadesSignature.getPostalCode();
    }
    
    @Override
    public Date getOCSPResponseCreationTime() {
        return this.xadesSignature.getOCSPResponseCreationTime();
    }
    
    @Deprecated
    @Override
    public Date getProducedAt() {
        return this.getOCSPResponseCreationTime();
    }
    
    @Override
    public Date getTimeStampCreationTime() {
        return this.xadesSignature.getTimeStampCreationTime();
    }
    
    @Override
    public Date getTrustedSigningTime() {
        return this.xadesSignature.getTrustedSigningTime();
    }
    
    @Override
    public SignatureProfile getProfile() {
        return this.xadesSignature.getProfile();
    }
    
    @Override
    public String getSignatureMethod() {
        return this.xadesSignature.getSignatureMethod();
    }
    
    @Override
    public List<String> getSignerRoles() {
        return this.xadesSignature.getSignerRoles();
    }
    
    @Override
    public X509Cert getSigningCertificate() {
        return this.xadesSignature.getSigningCertificate();
    }
    
    @Override
    public Date getClaimedSigningTime() {
        return this.xadesSignature.getSigningTime();
    }
    
    @Override
    public Date getSigningTime() {
        AsicSignature.logger.debug("get signing time by profile: " + this.getProfile());
        switch (this.getProfile()) {
            case B_BES: {
                return this.getClaimedSigningTime();
            }
            case B_EPES: {
                return this.getClaimedSigningTime();
            }
            default: {
                return this.getTrustedSigningTime();
            }
        }
    }
    
    @Deprecated
    @Override
    public URI getSignaturePolicyURI() {
        AsicSignature.logger.warn("Not yet implemented");
        throw new NotYetImplementedException();
    }
    
    @Override
    public String getStateOrProvince() {
        return this.xadesSignature.getStateOrProvince();
    }
    
    @Override
    public X509Cert getTimeStampTokenCertificate() {
        return this.xadesSignature.getTimeStampTokenCertificate();
    }
    
    @Override
    public SignatureValidationResult validateSignature() {
        AsicSignature.logger.debug("Validating signature");
        if (this.validationResult == null) {
            this.validationResult = this.validator.extractValidationErrors();
            AsicSignature.logger.info("Signature has " + this.validationResult.getErrors().size() + " validation errors and " + this.validationResult.getWarnings().size() + " warnings");
        }
        else {
            AsicSignature.logger.debug("Using existing validation errors with " + this.validationResult.getErrors().size() + " validation errors and " + this.validationResult.getWarnings().size() + " warnings");
        }
        return this.validationResult;
    }
    
    @Deprecated
    @Override
    public List<DigiDoc4JException> validate() {
        return this.validateSignature().getErrors();
    }
    
    @Override
    public byte[] getAdESSignature() {
        AsicSignature.logger.debug("Getting full XAdES signature byte array");
        try {
            return IOUtils.toByteArray(this.signatureDocument.openStream());
        }
        catch (IOException e) {
            throw new TechnicalException("Error parsing xades signature: " + e.getMessage(), e);
        }
    }
    
    @Deprecated
    @Override
    public byte[] getRawSignature() {
        return this.getAdESSignature();
    }
    
    public XadesSignature getOrigin() {
        return this.xadesSignature;
    }
    
    public void setSignatureDocument(final DSSDocument signatureDocument) {
        this.signatureDocument = signatureDocument;
    }
    
    public XadesValidationResult getDssValidationReport() {
        return this.xadesSignature.validate();
    }
    
    public DSSDocument getSignatureDocument() {
        return this.signatureDocument;
    }
    
    public DigestAlgorithm getSignatureDigestAlgorithm() {
        return this.xadesSignature.getDssSignature().getDigestAlgorithm();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicSignature.class);
    }
}
