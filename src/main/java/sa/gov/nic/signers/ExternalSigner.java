// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.signers;

import java.security.cert.X509Certificate;
import sa.gov.nic.SignatureToken;

public abstract class ExternalSigner implements SignatureToken
{
    private X509Certificate signingCertificate;
    
    public ExternalSigner(final X509Certificate signingCertificate) {
        this.signingCertificate = signingCertificate;
    }
    
    @Override
    public X509Certificate getCertificate() {
        return this.signingCertificate;
    }
}
