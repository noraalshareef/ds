// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class DataFileNotFoundException extends DigiDoc4JException
{
    public DataFileNotFoundException(final String fileName) {
        super("File not found: " + fileName);
    }
}
