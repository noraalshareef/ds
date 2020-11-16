// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class InvalidOcspNonceException extends DigiDoc4JException
{
    public static final String MESSAGE = "Nonce is invalid";
    
    public InvalidOcspNonceException() {
        super("Nonce is invalid");
    }
}
