// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class ContainerWithoutFilesException extends DigiDoc4JException
{
    public static final String MESSAGE = "Container does not contain any data files";
    
    public ContainerWithoutFilesException() {
        super("Container does not contain any data files");
    }
    
    public ContainerWithoutFilesException(final String message) {
        super(message);
    }
}
