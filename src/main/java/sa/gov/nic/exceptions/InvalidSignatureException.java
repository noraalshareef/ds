// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class InvalidSignatureException extends DigiDoc4JException
{
    public static String MESSAGE;
    
    public InvalidSignatureException() {
        super(InvalidSignatureException.MESSAGE);
    }
    
    static {
        InvalidSignatureException.MESSAGE = "Invalid signature document";
    }
}
