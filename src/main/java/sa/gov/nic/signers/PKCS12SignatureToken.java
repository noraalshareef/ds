// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.signers;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.EncryptionAlgorithm;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import eu.europa.esig.dss.SignatureAlgorithm;

import javax.crypto.Cipher;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.DERObjectIdentifier;
import java.security.MessageDigest;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import sa.gov.nic.DigestAlgorithm;
import java.security.cert.X509Certificate;
import sun.security.x509.KeyUsageExtension;

import java.util.List;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import java.security.cert.X509CertSelector;
import java.io.IOException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import sa.gov.nic.X509Cert;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import org.slf4j.Logger;
import sa.gov.nic.SignatureToken;

public class PKCS12SignatureToken implements SignatureToken
{
    private static final Logger logger;
    protected KeyStoreSignatureTokenConnection signatureTokenConnection;
    protected KSPrivateKeyEntry keyEntry;
    
    public PKCS12SignatureToken(final String fileName, final char[] password) {
        this.signatureTokenConnection = null;
        this.keyEntry = null;
        this.init(fileName, String.valueOf(password), X509Cert.KeyUsage.NON_REPUDIATION, null);
    }
    
    public PKCS12SignatureToken(final String fileName, final String password) {
        this.signatureTokenConnection = null;
        this.keyEntry = null;
        this.init(fileName, password, X509Cert.KeyUsage.NON_REPUDIATION, null);
    }
    
    public PKCS12SignatureToken(final String fileName, final String password, final String alias) {
        this.signatureTokenConnection = null;
        this.keyEntry = null;
        this.init(fileName, password, X509Cert.KeyUsage.NON_REPUDIATION, alias);
    }
    
    public PKCS12SignatureToken(final String fileName, final String password, final X509Cert.KeyUsage keyUsage) {
        this.signatureTokenConnection = null;
        this.keyEntry = null;
        this.init(fileName, password, keyUsage, null);
    }
    
    private void init(final String fileName, final String password, final X509Cert.KeyUsage keyUsage, final String alias) {
        PKCS12SignatureToken.logger.info("Using PKCS#12 signature token from file: " + fileName);
        try {
            this.signatureTokenConnection = (KeyStoreSignatureTokenConnection)new Pkcs12SignatureToken(fileName, password);
        }
        catch (IOException e) {
            throw new DigiDoc4JException(e.getMessage());
        }
        if (alias != null) {
            PKCS12SignatureToken.logger.debug("Searching key with alias: " + alias);
            this.keyEntry = this.signatureTokenConnection.getKey(alias, password);
        }
        else {
            PKCS12SignatureToken.logger.debug("Searching key by usage: " + keyUsage.name());
            final List<DSSPrivateKeyEntry> keys = (List<DSSPrivateKeyEntry>)this.signatureTokenConnection.getKeys();
            final X509CertSelector selector = new X509CertSelector();
            selector.setKeyUsage(this.getUsageBitArray(keyUsage));
            for (final DSSPrivateKeyEntry key : keys) {
                if (selector.match(key.getCertificate().getCertificate())) {
                    this.keyEntry = (KSPrivateKeyEntry)key;
                    break;
                }
            }
        }
        if (this.keyEntry == null && this.signatureTokenConnection.getKeys().size() > 0) {
            this.keyEntry = (KSPrivateKeyEntry) this.signatureTokenConnection.getKeys().get(0);
        }
    }
    
    private boolean[] getUsageBitArray(final X509Cert.KeyUsage keyUsage) {
        final KeyUsageExtension usage = new KeyUsageExtension();
        try {
            usage.set(keyUsage.name(), Boolean.TRUE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return usage.getBits();
    }
    
    public KeyStoreSignatureTokenConnection getSignatureTokenConnection() {
        return this.signatureTokenConnection;
    }
    
    @Override
    public X509Certificate getCertificate() {
        PKCS12SignatureToken.logger.debug("Using key with alias: ", (Object)this.getAlias());
        return this.keyEntry.getCertificate().getCertificate();
    }
    
    @Override
    public byte[] sign(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        PKCS12SignatureToken.logger.info("Signing with PKCS#12 signature token, using digest algorithm: " + digestAlgorithm.name());
        final ToBeSigned toBeSigned = new ToBeSigned(dataToSign);
        final eu.europa.esig.dss.DigestAlgorithm dssDigestAlgorithm = eu.europa.esig.dss.DigestAlgorithm.forXML(digestAlgorithm.toString());
        final SignatureValue signature = this.signatureTokenConnection.sign(toBeSigned, dssDigestAlgorithm, (DSSPrivateKeyEntry)this.keyEntry);
        return signature.getValue();
    }
    
    public byte[] sign2(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) throws Exception {
        final MessageDigest sha = MessageDigest.getInstance(digestAlgorithm.name(), "BC");
        final byte[] digest = sha.digest(dataToSign);
        final DERObjectIdentifier shaoid = new DERObjectIdentifier(digestAlgorithm.getDssDigestAlgorithm().getOid());
        final AlgorithmIdentifier shaaid = new AlgorithmIdentifier((ASN1ObjectIdentifier)shaoid, (ASN1Encodable)null);
        final DigestInfo di = new DigestInfo(shaaid, digest);
        final byte[] plainSig = di.getEncoded("DER");
        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(1, this.keyEntry.getPrivateKey());
        final byte[] signature = cipher.doFinal(plainSig);
        return signature;
    }
    
    public byte[] sign3(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        byte[] result = new byte[512];
        try {
            final EncryptionAlgorithm encryptionAlgorithm = this.keyEntry.getEncryptionAlgorithm();
            final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getAlgorithm(encryptionAlgorithm, digestAlgorithm.getDssDigestAlgorithm());
            final String javaSignatureAlgorithm = signatureAlgorithm.getJCEId();
            PKCS12SignatureToken.logger.debug("  ... Signing with PKCS#11 and " + javaSignatureAlgorithm);
            final Signature signature = Signature.getInstance(javaSignatureAlgorithm);
            signature.initSign(this.keyEntry.getPrivateKey());
            signature.update(dataToSign);
            result = signature.sign();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e2) {
            e2.printStackTrace();
        }
        catch (SignatureException e3) {
            e3.printStackTrace();
        }
        return result;
    }
    
    public String getAlias() {
        return this.keyEntry.getAlias();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)PKCS12SignatureToken.class);
    }
}
