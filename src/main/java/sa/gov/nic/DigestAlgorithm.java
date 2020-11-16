// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.net.MalformedURLException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.net.URL;

public enum DigestAlgorithm
{
    SHA1("http://www.w3.org/2000/09/xmldsig#sha1", new byte[] { 48, 33, 48, 9, 6, 5, 43, 14, 3, 2, 26, 5, 0, 4, 20 }), 
    SHA224("http://www.w3.org/2001/04/xmldsig-more#sha224", new byte[] { 48, 45, 48, 13, 6, 9, 96, -122, 72, 1, 101, 3, 4, 2, 4, 5, 0, 4, 28 }), 
    SHA256("http://www.w3.org/2001/04/xmlenc#sha256", new byte[] { 48, 49, 48, 13, 6, 9, 96, -122, 72, 1, 101, 3, 4, 2, 1, 5, 0, 4, 32 }), 
    SHA384("http://www.w3.org/2001/04/xmldsig-more#sha384", new byte[] { 48, 65, 48, 13, 6, 9, 96, -122, 72, 1, 101, 3, 4, 2, 2, 5, 0, 4, 48 }), 
    SHA512("http://www.w3.org/2001/04/xmlenc#sha512", new byte[] { 48, 81, 48, 13, 6, 9, 96, -122, 72, 1, 101, 3, 4, 2, 3, 5, 0, 4, 64 });
    
    private URL uri;
    private byte[] digestInfoPrefix;
    
    private DigestAlgorithm(final String uri, final byte[] digestInfoPrefix) {
        try {
            this.uri = new URL(uri);
            this.digestInfoPrefix = digestInfoPrefix;
        }
        catch (MalformedURLException e) {
            throw new DigiDoc4JException(e);
        }
    }
    
    public URL uri() {
        return this.uri;
    }
    
    public byte[] digestInfoPrefix() {
        return this.digestInfoPrefix;
    }
    
    public eu.europa.esig.dss.DigestAlgorithm getDssDigestAlgorithm() {
        return eu.europa.esig.dss.DigestAlgorithm.forXML(this.uri.toString());
    }
    
    @Override
    public String toString() {
        return this.uri.toString();
    }
    
    public static DigestAlgorithm findByAlgorithm(final String algorithm) {
        for (final DigestAlgorithm digestAlgorithm : values()) {
            if (digestAlgorithm.name().equals(algorithm)) {
                return digestAlgorithm;
            }
        }
        return null;
    }
}
