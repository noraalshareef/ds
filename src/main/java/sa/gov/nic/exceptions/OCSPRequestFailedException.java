// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.exceptions;

public class OCSPRequestFailedException extends DigiDoc4JException
{
    public static final String MESSAGE = "OCSP request failed. Please check GitHub Wiki for more information: https://github.com/open-eid/digidoc4j/wiki/Questions-&-Answers#if-ocsp-request-has-failed";
    
    public OCSPRequestFailedException(final Throwable e) {
        super("OCSP request failed. Please check GitHub Wiki for more information: https://github.com/open-eid/digidoc4j/wiki/Questions-&-Answers#if-ocsp-request-has-failed", e);
    }
    
    public OCSPRequestFailedException(final String sigId) {
        super("OCSP request failed. Please check GitHub Wiki for more information: https://github.com/open-eid/digidoc4j/wiki/Questions-&-Answers#if-ocsp-request-has-failed", sigId);
    }
}
