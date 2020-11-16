// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DLSequence;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.DSSUtils;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1OctetString;
import java.util.Arrays;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import java.util.Date;
import java.util.List;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import org.slf4j.Logger;
import java.io.Serializable;

public class OcspNonceValidator implements Serializable
{
    private static final Logger logger;
    private XAdESSignature signature;
    private BasicOCSPResp ocspResponse;
    
    public OcspNonceValidator(final XAdESSignature signature) {
        this.signature = signature;
        this.ocspResponse = this.getLatestOcspResponse(signature.getOCSPSource().getContainedOCSPResponses());
    }
    
    public boolean isValid() {
        if (this.signature.getPolicyId() == null) {
            return true;
        }
        if (this.ocspResponse == null) {
            OcspNonceValidator.logger.debug("OCSP response was not found in signature: " + this.signature.getId());
            return true;
        }
        return this.isOcspResponseValid(this.ocspResponse);
    }
    
    private BasicOCSPResp getLatestOcspResponse(final List<BasicOCSPResp> ocspResponses) {
        if (ocspResponses.size() == 0) {
            return null;
        }
        BasicOCSPResp basicOCSPResp = ocspResponses.get(0);
        Date latestDate = basicOCSPResp.getProducedAt();
        for (int i = 1; i < ocspResponses.size(); ++i) {
            final BasicOCSPResp ocspResp = ocspResponses.get(i);
            if (ocspResp.getProducedAt().after(latestDate)) {
                latestDate = ocspResp.getProducedAt();
                basicOCSPResp = ocspResp;
            }
        }
        return basicOCSPResp;
    }
    
    private boolean isOcspResponseValid(final BasicOCSPResp latestOcspResponse) {
        final Extension extension = latestOcspResponse.getExtension(new ASN1ObjectIdentifier(OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId()));
        if (extension == null) {
            OcspNonceValidator.logger.error("No valid OCSP extension found in signature: " + this.signature.getId());
            return false;
        }
        return this.isOcspExtensionValid(extension);
    }
    
    private boolean isOcspExtensionValid(final Extension extension) {
        try {
            final ASN1OctetString ev = extension.getExtnValue();
            final byte[] octets = ev.getOctets();
            final byte[] signatureDigestValue = this.getSignatureDigestValue(octets);
            final ASN1Sequence seq = ASN1Sequence.getInstance((Object)octets);
            final byte[] foundHash = ((DEROctetString)seq.getObjectAt(1)).getOctets();
            final boolean extensionHashMatchesSignatureHash = Arrays.equals(foundHash, signatureDigestValue);
            OcspNonceValidator.logger.debug("OCSP extension contains valid signature digest: " + extensionHashMatchesSignatureHash);
            return extensionHashMatchesSignatureHash;
        }
        catch (Exception e) {
            OcspNonceValidator.logger.error("Invalid nonce format: " + e.getMessage());
            return false;
        }
    }
    
    private byte[] getSignatureDigestValue(final byte[] octets) {
        final DigestAlgorithm usedDigestAlgorithm = this.getExtensionDigestAlgorithm(octets);
        final String signatureValueInBase64 = this.signature.getSignatureValue().getFirstChild().getNodeValue();
        final byte[] signatureValue = Base64.decodeBase64(signatureValueInBase64.getBytes());
        return DSSUtils.digest(usedDigestAlgorithm, signatureValue);
    }
    
    private DigestAlgorithm getExtensionDigestAlgorithm(final byte[] octets) {
        final ASN1Encodable oid = ASN1Sequence.getInstance((Object)octets).getObjectAt(0);
        final String oidString = ((DLSequence)oid).getObjects().nextElement().toString();
        return DigestAlgorithm.forOID(oidString);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)OcspNonceValidator.class);
    }
}
