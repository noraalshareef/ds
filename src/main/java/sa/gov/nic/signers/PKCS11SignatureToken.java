// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.signers;

import org.slf4j.LoggerFactory;
import java.security.PrivateKey;
import org.apache.commons.lang3.ArrayUtils;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import sa.gov.nic.exceptions.TechnicalException;
import java.io.IOException;
import sun.security.x509.KeyUsageExtension;

import java.security.cert.X509CertSelector;
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
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import java.security.MessageDigest;
import sa.gov.nic.DigestAlgorithm;
import java.security.cert.X509Certificate;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import java.util.List;
import eu.europa.esig.dss.token.PasswordInputCallback;
import sa.gov.nic.X509Cert;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.AbstractSignatureTokenConnection;
import org.slf4j.Logger;
import sa.gov.nic.SignatureToken;

public class PKCS11SignatureToken implements SignatureToken
{
    private static final Logger logger;
    private AbstractSignatureTokenConnection signatureTokenConnection;
    private KSPrivateKeyEntry privateKeyEntry;
    
    public PKCS11SignatureToken(final String pkcs11ModulePath, final char[] password, final int slotIndex) {
        PKCS11SignatureToken.logger.debug("Initializing PKCS#11 signature token from " + pkcs11ModulePath + " and slot " + slotIndex);
        this.signatureTokenConnection = (AbstractSignatureTokenConnection)new Pkcs11SignatureToken(pkcs11ModulePath, password, slotIndex);
        this.privateKeyEntry = this.findPrivateKey(X509Cert.KeyUsage.NON_REPUDIATION);
    }
    
    public PKCS11SignatureToken(final String pkcs11ModulePath, final PasswordInputCallback passwordCallback, final int slotIndex) {
        PKCS11SignatureToken.logger.debug("Initializing PKCS#11 signature token with password callback from " + pkcs11ModulePath + " and slot " + slotIndex);
        this.signatureTokenConnection = (AbstractSignatureTokenConnection)new Pkcs11SignatureToken(pkcs11ModulePath, passwordCallback, slotIndex);
        this.privateKeyEntry = this.findPrivateKey(X509Cert.KeyUsage.NON_REPUDIATION);
    }
    
    public List<DSSPrivateKeyEntry> getPrivateKeyEntries() {
        return (List<DSSPrivateKeyEntry>)this.signatureTokenConnection.getKeys();
    }
    
    public void usePrivateKeyEntry(final DSSPrivateKeyEntry keyEntry) {
        this.privateKeyEntry = (KSPrivateKeyEntry)keyEntry;
    }
    
    @Override
    public X509Certificate getCertificate() {
        PKCS11SignatureToken.logger.debug("Fetching certificate");
        return this.getPrivateKeyEntry().getCertificate().getCertificate();
    }
    
    public byte[] sign2(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) throws Exception {
        final MessageDigest sha = MessageDigest.getInstance(digestAlgorithm.name(), "BC");
        final byte[] digest = sha.digest(dataToSign);
        final DERObjectIdentifier shaoid = new DERObjectIdentifier(digestAlgorithm.getDssDigestAlgorithm().getOid());
        final AlgorithmIdentifier shaaid = new AlgorithmIdentifier((ASN1ObjectIdentifier)shaoid, (ASN1Encodable)DERNull.INSTANCE);
        final DigestInfo di = new DigestInfo(shaaid, digest);
        final byte[] plainSig = di.getEncoded("DER");
        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(1, this.privateKeyEntry.getPrivateKey());
        final byte[] signature = cipher.doFinal(plainSig);
        return signature;
    }
    
    public byte[] sign3(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        byte[] result = new byte[512];
        try {
            final EncryptionAlgorithm encryptionAlgorithm = this.privateKeyEntry.getEncryptionAlgorithm();
            final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getAlgorithm(encryptionAlgorithm, digestAlgorithm.getDssDigestAlgorithm());
            final String javaSignatureAlgorithm = signatureAlgorithm.getJCEId();
            PKCS11SignatureToken.logger.debug("  ... Signing with PKCS#11 and " + javaSignatureAlgorithm);
            final Signature signature = Signature.getInstance(javaSignatureAlgorithm);
            signature.initSign(this.privateKeyEntry.getPrivateKey());
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
    
    private KSPrivateKeyEntry findPrivateKey(final X509Cert.KeyUsage keyUsage) {
        PKCS11SignatureToken.logger.debug("Searching key by usage: " + keyUsage.name());
        final List<DSSPrivateKeyEntry> keys = this.getPrivateKeyEntries();
        final X509CertSelector selector = new X509CertSelector();
        selector.setKeyUsage(this.getUsageBitArray(keyUsage));
        for (final DSSPrivateKeyEntry key : keys) {
            if (selector.match(key.getCertificate().getCertificate())) {
                this.privateKeyEntry = (KSPrivateKeyEntry)key;
                PKCS11SignatureToken.logger.debug("... Found key by keyUsage. Key encryption algorithm:" + this.privateKeyEntry.getEncryptionAlgorithm().getName());
                break;
            }
        }
        return this.getPrivateKeyEntry();
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
    
    private KSPrivateKeyEntry getPrivateKeyEntry() {
        if (this.privateKeyEntry == null) {
            this.privateKeyEntry = (KSPrivateKeyEntry)this.getPrivateKeyEntries().get(0);
            PKCS11SignatureToken.logger.debug("... Getting first available key");
        }
        return this.privateKeyEntry;
    }
    
    @Override
    public byte[] sign(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        if (this.privateKeyEntry == null) {
            throw new TechnicalException("privateKeyEntry is null");
        }
        final String encryptionAlg = this.privateKeyEntry.getEncryptionAlgorithm().getName();
        if ("ECDSA".equals(encryptionAlg)) {
            PKCS11SignatureToken.logger.debug("Sign ECDSA");
            return this.signECDSA(digestAlgorithm, dataToSign);
        }
        if ("RSA".equals(encryptionAlg)) {
            PKCS11SignatureToken.logger.debug("Sign RSA");
            return this.signRSA(digestAlgorithm, dataToSign);
        }
        throw new TechnicalException("Failed to sign with PKCS#11. Encryption Algorithm should be ECDSA or RSA but actually is : " + encryptionAlg);
    }
    
    private byte[] signECDSA(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        try {
            PKCS11SignatureToken.logger.debug("Signing with PKCS#11 and " + digestAlgorithm.name());
            final ToBeSigned toBeSigned = new ToBeSigned(dataToSign);
            final eu.europa.esig.dss.DigestAlgorithm dssDigestAlgorithm = eu.europa.esig.dss.DigestAlgorithm.forXML(digestAlgorithm.toString());
            final SignatureValue signature = this.signatureTokenConnection.sign(toBeSigned, dssDigestAlgorithm, (DSSPrivateKeyEntry)this.privateKeyEntry);
            return signature.getValue();
        }
        catch (Exception e) {
            PKCS11SignatureToken.logger.error("Failed to sign with PKCS#11: " + e.getMessage());
            throw new TechnicalException("Failed to sign with PKCS#11: " + e.getMessage(), e);
        }
    }
    
    private byte[] signRSA(final DigestAlgorithm digestAlgorithm, final byte[] dataToSign) {
        try {
            PKCS11SignatureToken.logger.debug("Signing with PKCS#11 and " + digestAlgorithm.name());
            final byte[] digestToSign = DSSUtils.digest(digestAlgorithm.getDssDigestAlgorithm(), dataToSign);
            final byte[] digestWithPadding = addPadding(digestToSign, digestAlgorithm);
            return this.signDigest(digestWithPadding);
        }
        catch (Exception e) {
            PKCS11SignatureToken.logger.error("Failed to sign with PKCS#11: " + e.getMessage());
            throw new TechnicalException("Failed to sign with PKCS#11: " + e.getMessage(), e);
        }
    }
    
    private static byte[] addPadding(final byte[] digest, final DigestAlgorithm digestAlgorithm) {
        return ArrayUtils.addAll(digestAlgorithm.digestInfoPrefix(), digest);
    }
    
    private byte[] signDigest(final byte[] digestToSign) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        PKCS11SignatureToken.logger.debug("Signing digest");
        final DSSPrivateKeyEntry privateKeyEntry = (DSSPrivateKeyEntry)this.getPrivateKeyEntry();
        final PrivateKey privateKey = ((KSPrivateKeyEntry)privateKeyEntry).getPrivateKey();
        final EncryptionAlgorithm encryptionAlgorithm = privateKeyEntry.getEncryptionAlgorithm();
        final String signatureAlgorithm = "NONEwith" + encryptionAlgorithm.getName();
        return this.invokeSigning(digestToSign, privateKey, signatureAlgorithm);
    }
    
    private byte[] invokeSigning(final byte[] digestToSign, final PrivateKey privateKey, final String signatureAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PKCS11SignatureToken.logger.debug("Signing with signature algorithm " + signatureAlgorithm);
        final Signature signer = Signature.getInstance(signatureAlgorithm);
        signer.initSign(privateKey);
        signer.update(digestToSign);
        final byte[] signatureValue = signer.sign();
        return signatureValue;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)PKCS11SignatureToken.class);
    }
}
