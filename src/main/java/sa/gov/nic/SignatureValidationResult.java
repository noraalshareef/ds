// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.util.ArrayList;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import java.io.Serializable;

public class SignatureValidationResult implements Serializable
{
    private List<DigiDoc4JException> errors;
    private List<DigiDoc4JException> warnings;
    
    public SignatureValidationResult() {
        this.errors = new ArrayList<DigiDoc4JException>();
        this.warnings = new ArrayList<DigiDoc4JException>();
    }
    
    public boolean isValid() {
        return this.errors.isEmpty();
    }
    
    public List<DigiDoc4JException> getErrors() {
        return this.errors;
    }
    
    public void setErrors(final List<DigiDoc4JException> errors) {
        this.errors = errors;
    }
    
    public List<DigiDoc4JException> getWarnings() {
        return this.warnings;
    }
    
    public void setWarnings(final List<DigiDoc4JException> warnings) {
        this.warnings = warnings;
    }
}
