// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class TimestampAndOcspResponseTimeDeltaTooLargeException extends DigiDoc4JException
{
    public static final String MESSAGE = "The difference between the OCSP response time and the signature timestamp is too large";
    
    public TimestampAndOcspResponseTimeDeltaTooLargeException() {
        super("The difference between the OCSP response time and the signature timestamp is too large");
    }
}
