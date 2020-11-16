// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

import org.apache.commons.lang3.StringUtils;

public class DigiDoc4JException extends RuntimeException
{
    int errorCode;
    private String signatureId;
    
    public DigiDoc4JException(final int error, final String message) {
        super(message);
        this.errorCode = 0;
        this.signatureId = "";
        this.errorCode = error;
    }
    
    public DigiDoc4JException(final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
        this.signatureId = "";
    }
    
    public DigiDoc4JException(final String message) {
        super(message);
        this.errorCode = 0;
        this.signatureId = "";
    }
    
    public DigiDoc4JException(final String message, final String signatureId) {
        super(message);
        this.errorCode = 0;
        this.signatureId = "";
        this.signatureId = signatureId;
    }
    
    public DigiDoc4JException(final Throwable e) {
        super(e);
        this.errorCode = 0;
        this.signatureId = "";
    }
    
    public DigiDoc4JException(final Exception e) {
        super(e);
        this.errorCode = 0;
        this.signatureId = "";
    }
    
    public DigiDoc4JException() {
        this.errorCode = 0;
        this.signatureId = "";
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public String getSignatureId() {
        return this.signatureId;
    }
    
    public void setSignatureId(final String signatureId) {
        this.signatureId = signatureId;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank((CharSequence)this.signatureId)) {
            sb.append("(Signature ID: ").append(this.signatureId).append(") ");
        }
        if (this.errorCode != 0) {
            sb.append("ERROR: ").append(this.errorCode).append(" - ");
        }
        sb.append(this.getMessage());
        return sb.toString();
    }
}
