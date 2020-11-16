// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.security.cert.X509Certificate;

public interface SignatureToken
{
    X509Certificate getCertificate();
    
    byte[] sign(final DigestAlgorithm p0, final byte[] p1);
}
