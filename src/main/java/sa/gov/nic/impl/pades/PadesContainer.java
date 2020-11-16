// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.pades;

import sa.gov.nic.SignatureToken;
import java.io.OutputStream;
import sa.gov.nic.DigestAlgorithm;
import sa.gov.nic.SignatureParameters;
import sa.gov.nic.SignedInfo;
import java.security.cert.X509Certificate;
import sa.gov.nic.exceptions.NotSupportedException;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.ArrayList;
import eu.europa.esig.dss.validation.CertificateVerifier;
import sa.gov.nic.impl.asic.SKCommonCertificateVerifier;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.FileDocument;
import sa.gov.nic.ValidationResult;
import sa.gov.nic.SignatureProfile;
import java.util.List;
import sa.gov.nic.Signature;
import java.io.File;
import java.io.InputStream;
import sa.gov.nic.exceptions.NotYetImplementedException;
import sa.gov.nic.DataFile;
import sa.gov.nic.Configuration;
import sa.gov.nic.Container;

public class PadesContainer implements Container
{
    public static final String PADES = "PADES";
    private String containerPath;
    
    public PadesContainer(final String path, final Configuration configuration) {
        this.containerPath = "";
        this.containerPath = path;
    }
    
    @Override
    public DataFile addDataFile(final String path, final String mimeType) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public DataFile addDataFile(final InputStream is, final String fileName, final String mimeType) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public DataFile addDataFile(final File file, final String mimeType) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void addDataFile(final DataFile dataFile) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void addSignature(final Signature signature) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public List<DataFile> getDataFiles() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public String getType() {
        return "PADES";
    }
    
    @Override
    public List<Signature> getSignatures() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void removeDataFile(final DataFile file) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void removeSignature(final Signature signature) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void extendSignatureProfile(final SignatureProfile profile) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public File saveAsFile(final String filePath) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public InputStream saveAsStream() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public ValidationResult validate() {
        final SignedDocumentValidator validator = SignedDocumentValidator.fromDocument((DSSDocument)new FileDocument(new File(this.containerPath)));
        validator.setCertificateVerifier((CertificateVerifier)new SKCommonCertificateVerifier());
        final Reports reports = validator.validateDocument();
        final PadesValidationResult result = new PadesValidationResult(reports.getSimpleReport());
        result.setReport(reports.getXmlSimpleReport());
        final List<String> signatureIdList = (List<String>)reports.getSimpleReport().getSignatureIdList();
        final List<DigiDoc4JException> errors = new ArrayList<DigiDoc4JException>();
        final List<DigiDoc4JException> warnings = new ArrayList<DigiDoc4JException>();
        for (final String id : signatureIdList) {
            final Indication indication = reports.getSimpleReport().getIndication(id);
            if (!Indication.TOTAL_PASSED.equals((Object)indication)) {
                errors.addAll(this.getExceptions(reports.getSimpleReport().getErrors(id)));
                warnings.addAll(this.getExceptions(reports.getSimpleReport().getWarnings(id)));
            }
        }
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }
    
    @Override
    public void setTimeStampToken(final DataFile timeStampToken) {
        throw new NotSupportedException("Not for Pades container");
    }
    
    private List<DigiDoc4JException> getExceptions(final List<String> exceptionString) {
        final List<DigiDoc4JException> exc = new ArrayList<DigiDoc4JException>();
        for (final String s : exceptionString) {
            exc.add(new DigiDoc4JException(s));
        }
        return exc;
    }
    
    @Override
    public SignedInfo prepareSigning(final X509Certificate signerCert) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public String getSignatureProfile() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void setSignatureParameters(final SignatureParameters signatureParameters) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public DigestAlgorithm getDigestAlgorithm() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void addRawSignature(final byte[] signature) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void addRawSignature(final InputStream signatureStream) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public DataFile getDataFile(final int index) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public int countDataFiles() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void removeDataFile(final String fileName) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void removeSignature(final int signatureId) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void save(final String path) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void save(final OutputStream out) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public Signature sign(final SignatureToken signatureToken) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public Signature signRaw(final byte[] rawSignature) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public Signature getSignature(final int index) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public int countSignatures() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public DocumentType getDocumentType() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public String getVersion() {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void extendTo(final SignatureProfile profile) {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void setSignatureProfile(final SignatureProfile profile) {
        throw new NotYetImplementedException();
    }
}
