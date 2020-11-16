// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class InvalidTimestampException extends DigiDoc4JException
{
    public static final String MESSAGE = "Signature has an invalid timestamp";
    
    public InvalidTimestampException() {
        super("Signature has an invalid timestamp");
    }
}
