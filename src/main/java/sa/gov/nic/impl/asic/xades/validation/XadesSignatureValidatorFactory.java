// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import sa.gov.nic.SignatureProfile;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.Configuration;

public class XadesSignatureValidatorFactory
{
    private Configuration configuration;
    private XadesSignature signature;
    
    public XadesSignatureValidator create() {
        final SignatureProfile profile = this.signature.getProfile();
        XadesSignatureValidator xadesValidator;
        if (profile == SignatureProfile.B_BES) {
            xadesValidator = new XadesSignatureValidator(this.signature);
        }
        else if (profile == SignatureProfile.LT_TM) {
            xadesValidator = new TimemarkSignatureValidator(this.signature);
        }
        else if (profile == SignatureProfile.LT) {
            xadesValidator = new TimestampSignatureValidator(this.signature, this.configuration);
        }
        else {
            xadesValidator = new TimestampSignatureValidator(this.signature, this.configuration);
        }
        return xadesValidator;
    }
    
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
    
    public void setSignature(final XadesSignature signature) {
        this.signature = signature;
    }
}
