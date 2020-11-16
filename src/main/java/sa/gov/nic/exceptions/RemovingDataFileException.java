// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class RemovingDataFileException extends DigiDoc4JException
{
    public static final String MESSAGE = "Datafiles cannot be removed from an already signed container";
    
    public RemovingDataFileException() {
        super("Datafiles cannot be removed from an already signed container");
    }
}
