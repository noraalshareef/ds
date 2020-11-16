// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import sa.gov.nic.SignatureProfile;
import sa.gov.nic.SignatureValidationResult;
import java.io.Serializable;

public class SignatureValidationData implements Serializable
{
    private SignatureValidationResult validationResult;
    private String signatureId;
    private XadesValidationResult report;
    private SignatureProfile signatureProfile;
    
    public void setValidationResult(final SignatureValidationResult validationResult) {
        this.validationResult = validationResult;
    }
    
    public SignatureValidationResult getValidationResult() {
        return this.validationResult;
    }
    
    public void setSignatureId(final String signatureId) {
        this.signatureId = signatureId;
    }
    
    public String getSignatureId() {
        return this.signatureId;
    }
    
    public void setReport(final XadesValidationResult report) {
        this.report = report;
    }
    
    public XadesValidationResult getReport() {
        return this.report;
    }
    
    public void setSignatureProfile(final SignatureProfile signatureProfile) {
        this.signatureProfile = signatureProfile;
    }
    
    public SignatureProfile getSignatureProfile() {
        return this.signatureProfile;
    }
}
