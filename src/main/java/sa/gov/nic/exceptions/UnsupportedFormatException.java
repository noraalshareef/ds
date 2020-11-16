// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class UnsupportedFormatException extends DigiDoc4JException
{
    public UnsupportedFormatException(final String type) {
        super("Unsupported format: " + type);
    }
}
