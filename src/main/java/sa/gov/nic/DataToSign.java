// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.SignatureFinalizer;
import org.slf4j.Logger;
import java.io.Serializable;

public class DataToSign implements Serializable
{
    private static final Logger logger;
    private byte[] dataToSign;
    private SignatureParameters signatureParameters;
    private SignatureFinalizer signatureFinalizer;
    
    public DataToSign(final byte[] data, final SignatureParameters signatureParameters, final SignatureFinalizer signatureFinalizer) {
        this.dataToSign = data;
        this.signatureParameters = signatureParameters;
        this.signatureFinalizer = signatureFinalizer;
    }
    
    public SignatureParameters getSignatureParameters() {
        return this.signatureParameters;
    }
    
    public DigestAlgorithm getDigestAlgorithm() {
        return this.signatureParameters.getDigestAlgorithm();
    }
    
    public byte[] getDataToSign() {
        return this.dataToSign;
    }
    
    public Signature finalize(final byte[] signatureValue) {
        DataToSign.logger.debug("Finalizing signature");
        return this.signatureFinalizer.finalizeSignature(signatureValue);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DataToSign.class);
    }
}
