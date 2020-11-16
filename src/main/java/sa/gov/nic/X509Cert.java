//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509Cert implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(X509Cert.class);
    private X509Certificate originalCert;
    private Map<String, String> issuerPartMap;
    private Map<String, String> subjectNamePartMap;

    public X509Cert(X509Certificate cert) {
        logger.debug("");
        this.originalCert = cert;
    }

    X509Cert(String path) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream inStream = new FileInputStream(new File(path));
            Throwable var4 = null;

            try {
                this.originalCert = (X509Certificate)certificateFactory.generateCertificate(inStream);
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (inStream != null) {
                    if (var4 != null) {
                        try {
                            inStream.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        inStream.close();
                    }
                }

            }

        } catch (Exception var16) {
            throw new RuntimeException(var16);
        }
    }

    public List<String> getCertificatePolicies() throws IOException {
        logger.debug("");
        byte[] extensionValue = this.originalCert.getExtensionValue("2.5.29.32");
        List<String> policies = new ArrayList();
        byte[] octets = ((DEROctetString)DEROctetString.fromByteArray(extensionValue)).getOctets();
        ASN1Sequence sequence = (ASN1Sequence)ASN1Sequence.fromByteArray(octets);
        Enumeration sequenceObjects = sequence.getObjects();

        while(sequenceObjects.hasMoreElements()) {
            DLSequence next = (DLSequence)sequenceObjects.nextElement();
            Object objectAt = next.getObjectAt(0);
            if (objectAt != null) {
                policies.add(objectAt.toString());
            }
        }

        return policies;
    }

    public X509Certificate getX509Certificate() {
        logger.debug("");
        return this.originalCert;
    }

    public String issuerName(X509Cert.Issuer part) {
        logger.debug("Part: " + part);
        if (this.issuerPartMap == null) {
            this.loadIssuerParts();
        }

        String issuerName = (String)this.issuerPartMap.get(part.name());
        logger.debug("Issuer name: " + issuerName);
        return issuerName;
    }

    private void loadIssuerParts() {
        logger.debug("");
        String[] parts = StringUtils.split(this.issuerName(), ',');
        this.issuerPartMap = new HashMap();
        String[] arr$ = parts;
        int len$ = parts.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String part = arr$[i$];
            String[] strings = StringUtils.split(part, "=");
            String key = strings[0].trim();
            String value = strings[1].trim();
            this.issuerPartMap.put(key, value);
            logger.debug("Subject name part key: " + key + " value: " + value);
        }

    }

    public String issuerName() {
        logger.debug("");
        String name = this.originalCert.getIssuerDN().getName();
        logger.debug("Issuer name: " + name);
        return name;
    }

    public boolean isValid(Date date) {
        logger.debug("Date: " + date);

        try {
            this.originalCert.checkValidity(date);
        } catch (CertificateExpiredException var3) {
            logger.debug("Date " + date + " is not valid");
            return false;
        } catch (CertificateNotYetValidException var4) {
            logger.debug("Date " + date + " is not valid");
            return false;
        }

        logger.debug("Date " + date + " is valid");
        return true;
    }

    public boolean isValid() {
        logger.debug("");
        return this.isValid(new Date());
    }

    public List<X509Cert.KeyUsage> getKeyUsages() {
        logger.debug("");
        List<X509Cert.KeyUsage> keyUsages = new ArrayList();
        boolean[] keyUsagesBits = this.originalCert.getKeyUsage();

        for(int i = 0; i < keyUsagesBits.length; ++i) {
            if (keyUsagesBits[i]) {
                keyUsages.add(X509Cert.KeyUsage.values()[i]);
            }
        }

        logger.debug("Returning " + keyUsages.size() + "key usages:");
        Iterator i$ = keyUsages.iterator();

        while(i$.hasNext()) {
            X509Cert.KeyUsage keyUsage = (X509Cert.KeyUsage)i$.next();
            logger.debug("\t" + keyUsage.toString());
        }

        return keyUsages;
    }

    public String getSerial() {
        logger.debug("");
        String serial = Hex.toHexString(this.originalCert.getSerialNumber().toByteArray());
        logger.debug("Serial number: " + serial);
        return serial;
    }

    public String getSubjectName(X509Cert.SubjectName part) {
        logger.debug("Part: " + part);
        if (this.subjectNamePartMap == null) {
            this.loadSubjectNameParts();
        }

        String subjectName = (String)this.subjectNamePartMap.get(part.name());
        logger.debug("Subject name: " + subjectName);
        return subjectName;
    }

    private void loadSubjectNameParts() {
        logger.debug("");
        String[] parts = this.getSubjectName().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        this.subjectNamePartMap = new HashMap();
        String[] arr$ = parts;
        int len$ = parts.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String part = arr$[i$];
            String[] strings = part.split("=(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            String key = strings[0].trim();
            String value = strings[1].trim();
            this.subjectNamePartMap.put(key, value);
            logger.debug("Subject name part key: " + key + " value: " + value);
        }

    }

    public String getSubjectName() {
        logger.debug("");
        String subjectName = this.originalCert.getSubjectX500Principal().toString();
        logger.debug("Subject name: " + subjectName);
        return subjectName;
    }

    public static enum SubjectName {
        SERIALNUMBER,
        GIVENNAME,
        SURNAME,
        CN,
        OU,
        O,
        C;

        private SubjectName() {
        }
    }

    public static enum Issuer {
        EMAILADDRESS,
        C,
        O,
        CN;

        private Issuer() {
        }
    }

    public static enum KeyUsage {
        DIGITAL_SIGNATURE,
        NON_REPUDIATION,
        KEY_ENCIPHERMENT,
        DATA_ENCIPHERMENT,
        KEY_AGREEMENT,
        KEY_CERTIFICATESIGN,
        CRL_SIGN,
        ENCIPHER_ONLY,
        DECIPHER_ONLY;

        private KeyUsage() {
        }
    }
}
