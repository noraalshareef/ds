// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.slf4j.LoggerFactory;
import sa.gov.nic.Signature;
import sa.gov.nic.exceptions.ContainerWithoutFilesException;
import sa.gov.nic.exceptions.SignerCertificateRequiredException;
import sa.gov.nic.SignedInfo;
import java.security.cert.X509Certificate;

import sa.gov.nic.DataToSign;
import org.slf4j.Logger;
import sa.gov.nic.SignatureBuilder;

public class DDocSignatureBuilder extends SignatureBuilder
{
    private static final Logger logger;
    
    @Override
    public DataToSign buildDataToSign() throws SignerCertificateRequiredException, ContainerWithoutFilesException {
        DDocSignatureBuilder.logger.debug("Building data to sign");
        final DDocFacade ddocFacade = this.getJDigiDocFacade();
        ddocFacade.setSignatureParameters(this.signatureParameters);
        final X509Certificate signingCertificate = this.signatureParameters.getSigningCertificate();
        final SignedInfo signedInfo = ddocFacade.prepareSigning(signingCertificate);
        return new DataToSign(signedInfo.getDigestToSign(), this.signatureParameters, ddocFacade);
    }
    
    @Override
    protected Signature invokeSigningProcess() {
        final DDocFacade ddocFacade = this.getJDigiDocFacade();
        ddocFacade.setSignatureParameters(this.signatureParameters);
        return ddocFacade.sign(this.signatureToken);
    }
    
    @Override
    public Signature openAdESSignature(final byte[] signatureDocument) {
        final DDocFacade ddocFacade = this.getJDigiDocFacade();
        ddocFacade.setSignatureParameters(this.signatureParameters);
        ddocFacade.addRawSignature(signatureDocument);
        final int signatureIndex = ddocFacade.getSignatures().size() - 1;
        final Signature signature = ddocFacade.getSignatures().get(signatureIndex);
        return signature;
    }
    
    private DDocFacade getJDigiDocFacade() {
        return ((DDocContainer)this.container).getJDigiDocFacade();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DDocSignatureBuilder.class);
    }
}
