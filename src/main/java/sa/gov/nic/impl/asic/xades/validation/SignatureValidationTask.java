// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import org.slf4j.LoggerFactory;
import sa.gov.nic.SignatureValidationResult;
import sa.gov.nic.impl.asic.asics.AsicSSignature;
import sa.gov.nic.impl.asic.asice.AsicESignature;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignature;
import sa.gov.nic.Signature;
import org.slf4j.Logger;
import java.util.concurrent.Callable;

public class SignatureValidationTask implements Callable<SignatureValidationData>
{
    private static final Logger logger;
    private Signature signature;
    
    public SignatureValidationTask(final Signature signature) {
        this.signature = signature;
    }
    
    @Override
    public SignatureValidationData call() throws Exception {
        SignatureValidationTask.logger.debug("Starting to validate signature " + this.signature.getId());
        final SignatureValidationResult validationResult = this.signature.validateSignature();
        final SignatureValidationData validationData = new SignatureValidationData();
        validationData.setValidationResult(validationResult);
        validationData.setSignatureId(this.signature.getId());
        validationData.setSignatureProfile(this.signature.getProfile());
        if (this.signature.getClass() == BDocSignature.class) {
            validationData.setReport(((BDocSignature)this.signature).getDssValidationReport());
        }
        else if (this.signature.getClass() == AsicESignature.class) {
            validationData.setReport(((AsicESignature)this.signature).getDssValidationReport());
        }
        else if (this.signature.getClass() == AsicSSignature.class) {
            validationData.setReport(((AsicSSignature)this.signature).getDssValidationReport());
        }
        return validationData;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureValidationTask.class);
    }
}
