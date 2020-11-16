// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.utils;

import org.slf4j.LoggerFactory;
import ee.sk.digidoc.factory.DigiDocGenFactory;
import sa.gov.nic.DigestAlgorithm;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;

public class TokenAlgorithmSupport
{
    private static final Logger logger;
    
    public static DigestAlgorithm determineSignatureDigestAlgorithm(final X509Certificate certificate) {
        if (DigiDocGenFactory.isPre2011IdCard(certificate)) {
            TokenAlgorithmSupport.logger.debug("The certificate belongs to a pre 2011 Estonian ID card supporting SHA-224");
            return DigestAlgorithm.SHA224;
        }
        return DigestAlgorithm.SHA256;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)TokenAlgorithmSupport.class);
    }
}
