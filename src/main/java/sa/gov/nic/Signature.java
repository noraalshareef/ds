// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import sa.gov.nic.exceptions.DigiDoc4JException;
import java.net.URI;
import java.util.List;
import java.util.Date;
import java.io.Serializable;

public interface Signature extends Serializable
{
    String getCity();
    
    String getCountryName();
    
    String getId();
    
    byte[] getOCSPNonce();
    
    X509Cert getOCSPCertificate();
    
    @Deprecated
    String getPolicy();
    
    String getPostalCode();
    
    @Deprecated
    Date getProducedAt();
    
    Date getOCSPResponseCreationTime();
    
    Date getTimeStampCreationTime();
    
    Date getTrustedSigningTime();
    
    SignatureProfile getProfile();
    
    String getSignatureMethod();
    
    List<String> getSignerRoles();
    
    X509Cert getSigningCertificate();
    
    Date getClaimedSigningTime();
    
    @Deprecated
    Date getSigningTime();
    
    @Deprecated
    URI getSignaturePolicyURI();
    
    String getStateOrProvince();
    
    X509Cert getTimeStampTokenCertificate();
    
    SignatureValidationResult validateSignature();
    
    @Deprecated
    List<DigiDoc4JException> validate();
    
    byte[] getAdESSignature();
    
    @Deprecated
    byte[] getRawSignature();
}
