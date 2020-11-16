// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import sa.gov.nic.impl.asic.xades.validation.XadesValidationResult;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import org.apache.xml.security.signature.Reference;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import java.util.Date;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.X509Cert;
import java.util.List;
import java.io.Serializable;

public interface XadesSignature extends Serializable
{
    String getId();
    
    String getCity();
    
    String getStateOrProvince();
    
    String getPostalCode();
    
    String getCountryName();
    
    List<String> getSignerRoles();
    
    X509Cert getSigningCertificate();
    
    SignatureProfile getProfile();
    
    String getSignatureMethod();
    
    Date getSigningTime();
    
    Date getTrustedSigningTime();
    
    @Deprecated
    Date getOCSPResponseCreationTime();
    
    X509Cert getOCSPCertificate();
    
    List<BasicOCSPResp> getOcspResponses();
    
    Date getTimeStampCreationTime();
    
    X509Cert getTimeStampTokenCertificate();
    
    List<Reference> getReferences();
    
    byte[] getSignatureValue();
    
    XAdESSignature getDssSignature();
    
    XadesValidationResult validate();
}
