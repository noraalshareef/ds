// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import sa.gov.nic.SignatureProfile;

public class EpesSignature extends BesSignature
{
    public EpesSignature(final XadesValidationReportGenerator xadesReportGenerator) {
        super(xadesReportGenerator);
    }
    
    @Override
    public SignatureProfile getProfile() {
        return SignatureProfile.B_EPES;
    }
}
