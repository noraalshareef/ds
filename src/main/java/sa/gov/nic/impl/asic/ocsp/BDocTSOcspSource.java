// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.ocsp;

import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Extension;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;

public class BDocTSOcspSource extends SKOnlineOCSPSource
{
    private static final Logger logger;
    
    public BDocTSOcspSource(final Configuration configuration) {
        super(configuration);
        BDocTSOcspSource.logger.debug("Using TS OCSP source");
    }
    
    public Extension createNonce() {
        final byte[] bytes = this.generateRandomNonce();
        final DEROctetString nonce = new DEROctetString(bytes);
        final boolean critical = false;
        return new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, critical, (ASN1OctetString)nonce);
    }
    
    private byte[] generateRandomNonce() {
        final SecureRandom random = new SecureRandom();
        final byte[] nonceBytes = new byte[20];
        random.nextBytes(nonceBytes);
        return nonceBytes;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BDocTSOcspSource.class);
    }
}
