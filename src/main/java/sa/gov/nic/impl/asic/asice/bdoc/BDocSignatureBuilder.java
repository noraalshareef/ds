// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice.bdoc;

import eu.europa.esig.dss.DigestAlgorithm;
import org.apache.commons.codec.binary.Base64;
import sa.gov.nic.SignatureBuilder;
import eu.europa.esig.dss.Policy;
import sa.gov.nic.impl.asic.asice.AsicESignatureBuilder;

public class BDocSignatureBuilder extends AsicESignatureBuilder
{
    public static Policy createBDocSignaturePolicy() {
        if (BDocSignatureBuilder.policyDefinedByUser != null && SignatureBuilder.isDefinedAllPolicyValues()) {
            return BDocSignatureBuilder.policyDefinedByUser;
        }
        final Policy signaturePolicy = new Policy();
        signaturePolicy.setId("urn:oid:1.3.6.1.4.1.10015.1000.3.2.1");
        signaturePolicy.setDigestValue(Base64.decodeBase64("7pudpH4eXlguSZY2e/pNbKzGsq+fu//woYL1SZFws1A="));
        signaturePolicy.setQualifier("OIDAsURN");
        signaturePolicy.setDigestAlgorithm(DigestAlgorithm.SHA256);
        signaturePolicy.setSpuri("https://www.sk.ee/repository/bdoc-spec21.pdf");
        return signaturePolicy;
    }
    
    @Override
    protected void setSignaturePolicy() {
        if (this.isTimeMarkProfile() || this.isEpesProfile()) {
            final Policy signaturePolicy = createBDocSignaturePolicy();
            this.facade.setSignaturePolicy(signaturePolicy);
        }
    }
}
