// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.manifest;

public class ManifestErrorMessage
{
    private String errorMessage;
    private String signatureId;
    
    public ManifestErrorMessage(final String errorMessage, final String signatureId) {
        this.errorMessage = "";
        this.signatureId = "";
        this.errorMessage = errorMessage;
        this.signatureId = signatureId;
    }
    
    public ManifestErrorMessage(final String errorMessage) {
        this.errorMessage = "";
        this.signatureId = "";
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getSignatureId() {
        return this.signatureId;
    }
    
    public void setSignatureId(final String signatureId) {
        this.signatureId = signatureId;
    }
}
