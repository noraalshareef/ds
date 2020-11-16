//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic.impl.asic;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import sa.gov.nic.DataFile;
import sa.gov.nic.ValidationResult;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeStampTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(TimeStampTokenValidator.class);
    private AsicParseResult containerParseResult;

    public TimeStampTokenValidator(AsicParseResult containerParseResult) {
        this.containerParseResult = containerParseResult;
    }

    public ValidationResult validate() {
        logger.debug("Validating container");
        this.validateContainer(this.containerParseResult);
        TimeStampToken timeStampToken = this.getTimeStamp(this.containerParseResult);
        List<DigiDoc4JException> errors = this.validateTimeStamp((DataFile)this.containerParseResult.getDataFiles().get(0), timeStampToken);
        Date signedTime = timeStampToken.getTimeStampInfo().getGenTime();
        String signedBy = this.getTimeStampTokenSigner(timeStampToken);
        TimeStampValidationResult timeStampValidationResult = this.generateTimeStampValidationResult(signedTime, signedBy, errors, timeStampToken);
        logger.info("Is container valid: " + timeStampValidationResult.isValid());
        return timeStampValidationResult;
    }

    private TimeStampValidationResult generateTimeStampValidationResult(Date signedTime, String signedBy, List<DigiDoc4JException> errors, TimeStampToken timeStampToken) {
        TimeStampValidationResult timeStampValidationResult = new TimeStampValidationResult();
        timeStampValidationResult.setErrors(errors);
        timeStampValidationResult.setSignedBy(signedBy);
        timeStampValidationResult.setSignedTime(DateUtils.getDateFormatterWithGMTZone().format(signedTime));
        timeStampValidationResult.setTimeStampToken(timeStampToken);
        return timeStampValidationResult;
    }

    private String getTimeStampTokenSigner(TimeStampToken timeStampToken) {
        GeneralName tsa = timeStampToken.getTimeStampInfo().getTsa();
        if (tsa == null) {
            return null;
        } else {
            ASN1Encodable x500Name = tsa.getName();
            return x500Name instanceof X500Name ? IETFUtils.valueToString(((X500Name)x500Name).getRDNs(BCStyle.CN)[0].getFirst().getValue()) : null;
        }
    }

    private List<DigiDoc4JException> validateTimeStamp(DataFile datafile, TimeStampToken timeStampToken) {
        List<DigiDoc4JException> errors = new ArrayList();
        boolean isSignatureValid = this.isSignatureValid(timeStampToken);
        if (!isSignatureValid) {
            errors.add(new DigiDoc4JException("Signature not intact"));
        }

        byte[] dataFileBytes = datafile.getBytes();
        boolean isMessageImprintsValid = this.isMessageImprintsValid(dataFileBytes, timeStampToken);
        if (isSignatureValid && !isMessageImprintsValid) {
            errors.add(new DigiDoc4JException("Signature not intact"));
        }

        boolean isVersionValid = this.isVersionValid(timeStampToken);
        if (!isVersionValid) {
            errors.add(new DigiDoc4JException("TST version not supported"));
        }

        return errors;
    }

    private boolean isMessageImprintsValid(byte[] dataFileBytes, TimeStampToken timeStampToken) {
        byte[] digestValue = DSSUtils.digest(DigestAlgorithm.SHA256, dataFileBytes);
        byte[] messageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
        return Arrays.equals(messageImprintDigest, digestValue);
    }

    private boolean isVersionValid(TimeStampToken timeStampToken) {
        return timeStampToken.getTimeStampInfo().toASN1Structure().getVersion().getValue().longValue() == 1L;
    }

    private boolean isSignatureValid(TimeStampToken timeStampToken) {
        try {
            JcaSimpleSignerInfoVerifierBuilder sigVerifierBuilder = new JcaSimpleSignerInfoVerifierBuilder();
            Collection certCollection = timeStampToken.getCertificates().getMatches(timeStampToken.getSID());
            Iterator certIt = certCollection.iterator();
            X509CertificateHolder cert = (X509CertificateHolder)certIt.next();
            Certificate x509Cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
            SignerInformationVerifier signerInfoVerifier = sigVerifierBuilder.setProvider("BC").build(x509Cert.getPublicKey());
            return timeStampToken.isSignatureValid(signerInfoVerifier);
        } catch (Exception var8) {
            throw new DigiDoc4JException(var8);
        }
    }

    private void validateContainer(AsicParseResult documents) {
        long dataFileCount = documents.getDataFiles() != null ? (long)documents.getDataFiles().size() : 0L;
        long signatureFileCount = documents.getSignatures() != null ? (long)documents.getSignatures().size() : 0L;
        if (dataFileCount != 1L || signatureFileCount > 0L) {
            throw new DigiDoc4JException("Document does not meet the requirements: signatureFileCount = " + signatureFileCount + " (expected 0) , dataFileCount = " + dataFileCount + " (expected 1)");
        }
    }

    private TimeStampToken getTimeStamp(AsicParseResult documents) {
        try {
            CMSSignedData cms = new CMSSignedData(documents.getTimeStampToken().getBytes());
            return new TimeStampToken(cms);
        } catch (TSPException | IOException | CMSException var3) {
            throw new DigiDoc4JException("Document malformed or not matching documentType : " + var3.getMessage());
        }
    }
}
