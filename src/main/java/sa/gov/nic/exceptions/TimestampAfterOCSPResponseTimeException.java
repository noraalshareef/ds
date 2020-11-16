// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class TimestampAfterOCSPResponseTimeException extends DigiDoc4JException
{
    public static final String MESSAGE = "Timestamp time is after OCSP response production time";
    
    public TimestampAfterOCSPResponseTimeException() {
        super("Timestamp time is after OCSP response production time");
    }
}
