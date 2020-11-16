// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class NotSupportedException extends DigiDoc4JException
{
    public NotSupportedException(final String message) {
        super("Not supported: " + message);
    }
}
