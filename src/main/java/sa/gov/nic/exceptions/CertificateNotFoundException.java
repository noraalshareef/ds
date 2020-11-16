// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class CertificateNotFoundException extends DigiDoc4JException
{
    public CertificateNotFoundException(final String message, final String signatureId) {
        super(message, signatureId);
    }
}
