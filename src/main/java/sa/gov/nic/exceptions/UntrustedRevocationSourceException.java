// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class UntrustedRevocationSourceException extends DigiDoc4JException
{
    public static final String MESSAGE = "Signing certificate revocation source is not trusted";
    
    public UntrustedRevocationSourceException() {
        super("Signing certificate revocation source is not trusted");
    }
}
