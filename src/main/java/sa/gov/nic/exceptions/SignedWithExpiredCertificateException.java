// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class SignedWithExpiredCertificateException extends DigiDoc4JException
{
    public static final String MESSAGE = "Signature has been created with expired certificate";
    
    public SignedWithExpiredCertificateException() {
        super("Signature has been created with expired certificate");
    }
}
