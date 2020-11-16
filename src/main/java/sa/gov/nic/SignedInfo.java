// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import org.slf4j.Logger;
import java.io.Serializable;

public class SignedInfo implements Serializable
{
    private static final Logger logger;
    private byte[] digestToSign;
    private SignatureParameters signatureParameters;
    
    public SignedInfo() {
    }
    
    public SignedInfo(final byte[] dataToDigest, final SignatureParameters signatureParameters) {
        final sa.gov.nic.DigestAlgorithm digestAlgorithm = signatureParameters.getDigestAlgorithm();
        this.digestToSign = DSSUtils.digest(DigestAlgorithm.forXML(digestAlgorithm.toString()), dataToDigest);
        this.signatureParameters = signatureParameters;
    }
    
    public byte[] getDigest() {
        SignedInfo.logger.debug("");
        return this.getDigestToSign();
    }
    
    public byte[] getDigestToSign() {
        return this.digestToSign;
    }
    
    public sa.gov.nic.DigestAlgorithm getDigestAlgorithm() {
        SignedInfo.logger.debug("");
        return this.signatureParameters.getDigestAlgorithm();
    }
    
    public SignatureParameters getSignatureParameters() {
        return this.signatureParameters;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignedInfo.class);
    }
}
