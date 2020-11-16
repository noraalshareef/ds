// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.ocsp;

import org.slf4j.LoggerFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DEROctetString;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DERSequence;
import java.io.IOException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;

public class BDocTMOcspSource extends SKOnlineOCSPSource
{
    private static final Logger logger;
    private final byte[] signature;
    
    public BDocTMOcspSource(final Configuration configuration, final byte[] signature) {
        super(configuration);
        this.signature = signature;
        BDocTMOcspSource.logger.debug("Using TM OCSP source");
    }
    
    @Override
    Extension createNonce() {
        try {
            final boolean critical = false;
            return new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, critical, this.createNonceAsn1Sequence().getEncoded());
        }
        catch (IOException e) {
            BDocTMOcspSource.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
    }
    
    private DERSequence createNonceAsn1Sequence() {
        final ASN1Object[] nonceComponents = { (ASN1Object)new DefaultDigestAlgorithmIdentifierFinder().find("SHA-256"), (ASN1Object)new DEROctetString(DSSUtils.digest(DigestAlgorithm.SHA256, this.signature)) };
        return new DERSequence((ASN1Encodable[])nonceComponents);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SKOnlineOCSPSource.class);
    }
}
