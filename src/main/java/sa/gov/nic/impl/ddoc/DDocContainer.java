// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.slf4j.LoggerFactory;
import sa.gov.nic.SignatureToken;
import sa.gov.nic.DigestAlgorithm;
import sa.gov.nic.SignatureParameters;
import sa.gov.nic.SignedInfo;
import java.security.cert.X509Certificate;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.ValidationResult;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import sa.gov.nic.SignatureProfile;
import java.util.List;
import sa.gov.nic.Signature;
import java.io.File;
import java.io.InputStream;
import sa.gov.nic.DataFile;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import sa.gov.nic.Container;

public class DDocContainer implements Container
{
    private static final Logger logger;
    private DDocFacade jDigiDocFacade;
    
    public DDocContainer(final DDocFacade jDigiDocFacade) {
        this.jDigiDocFacade = jDigiDocFacade;
    }
    
    public DDocContainer() {
        this.jDigiDocFacade = new DDocFacade();
    }
    
    public DDocContainer(final Configuration configuration) {
        this.jDigiDocFacade = new DDocFacade(configuration);
    }
    
    @Override
    public DataFile addDataFile(final String path, final String mimeType) {
        this.jDigiDocFacade.addDataFile(path, mimeType);
        return new DataFile(path, mimeType);
    }
    
    @Override
    public DataFile addDataFile(final InputStream is, final String fileName, final String mimeType) {
        return this.jDigiDocFacade.addDataFile(is, fileName, mimeType);
    }
    
    @Override
    public DataFile addDataFile(final File file, final String mimeType) {
        return this.jDigiDocFacade.addDataFile(file.getPath(), mimeType);
    }
    
    @Override
    public void addDataFile(final DataFile dataFile) {
        this.jDigiDocFacade.addDataFile(dataFile);
    }
    
    @Override
    public void addSignature(final Signature signature) {
        DDocContainer.logger.debug("Ignoring separate add signature call for DDoc containers, because signatures are added to container during signing process");
    }
    
    @Override
    public List<DataFile> getDataFiles() {
        return this.jDigiDocFacade.getDataFiles();
    }
    
    @Override
    public String getType() {
        return "DDOC";
    }
    
    @Override
    public List<Signature> getSignatures() {
        return this.jDigiDocFacade.getSignatures();
    }
    
    @Override
    public void removeDataFile(final DataFile file) {
        this.jDigiDocFacade.removeDataFile(file.getName());
    }
    
    @Override
    public void removeSignature(final Signature signature) {
        final DDocSignature dDocSignature = (DDocSignature)signature;
        this.jDigiDocFacade.removeSignature(dDocSignature.getIndexInArray());
    }
    
    @Override
    public void extendSignatureProfile(final SignatureProfile profile) {
        this.jDigiDocFacade.extendTo(profile);
    }
    
    @Override
    public File saveAsFile(final String fileName) {
        this.jDigiDocFacade.save(fileName);
        return new File(fileName);
    }
    
    @Override
    public InputStream saveAsStream() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.save(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    @Override
    public ValidationResult validate() {
        return this.jDigiDocFacade.validate();
    }
    
    @Override
    public void setTimeStampToken(final DataFile timeStampToken) {
        throw new NotSupportedException("Not for DDOC container");
    }
    
    @Deprecated
    @Override
    public SignedInfo prepareSigning(final X509Certificate signerCert) {
        return this.jDigiDocFacade.prepareSigning(signerCert);
    }
    
    @Deprecated
    @Override
    public String getSignatureProfile() {
        return this.jDigiDocFacade.getSignatureProfile();
    }
    
    @Deprecated
    @Override
    public void setSignatureParameters(final SignatureParameters signatureParameters) {
        this.jDigiDocFacade.setSignatureParameters(signatureParameters);
    }
    
    @Deprecated
    @Override
    public DigestAlgorithm getDigestAlgorithm() {
        return this.jDigiDocFacade.getDigestAlgorithm();
    }
    
    @Deprecated
    @Override
    public void addRawSignature(final byte[] signature) {
        this.jDigiDocFacade.addRawSignature(signature);
    }
    
    @Deprecated
    @Override
    public void addRawSignature(final InputStream signatureStream) {
        this.jDigiDocFacade.addRawSignature(signatureStream);
    }
    
    @Deprecated
    @Override
    public DataFile getDataFile(final int index) {
        return this.jDigiDocFacade.getDataFile(index);
    }
    
    @Deprecated
    @Override
    public int countDataFiles() {
        return this.jDigiDocFacade.countDataFiles();
    }
    
    @Deprecated
    @Override
    public void removeDataFile(final String fileName) {
        this.jDigiDocFacade.removeDataFile(fileName);
    }
    
    @Deprecated
    @Override
    public void removeSignature(final int signatureId) {
        this.jDigiDocFacade.removeSignature(signatureId);
    }
    
    @Deprecated
    @Override
    public void save(final String path) {
        this.jDigiDocFacade.save(path);
    }
    
    @Override
    public void save(final OutputStream out) {
        this.jDigiDocFacade.save(out);
    }
    
    @Deprecated
    @Override
    public Signature sign(final SignatureToken signatureToken) {
        return this.jDigiDocFacade.sign(signatureToken);
    }
    
    @Deprecated
    @Override
    public Signature signRaw(final byte[] rawSignature) {
        return this.jDigiDocFacade.signRaw(rawSignature);
    }
    
    @Deprecated
    @Override
    public Signature getSignature(final int index) {
        return this.jDigiDocFacade.getSignature(index);
    }
    
    @Deprecated
    @Override
    public int countSignatures() {
        return this.jDigiDocFacade.countSignatures();
    }
    
    @Deprecated
    @Override
    public DocumentType getDocumentType() {
        return this.jDigiDocFacade.getDocumentType();
    }
    
    @Deprecated
    @Override
    public String getVersion() {
        return this.jDigiDocFacade.getVersion();
    }
    
    @Deprecated
    @Override
    public void extendTo(final SignatureProfile profile) {
        this.jDigiDocFacade.extendTo(profile);
    }
    
    @Deprecated
    @Override
    public void setSignatureProfile(final SignatureProfile profile) {
        this.jDigiDocFacade.setSignatureProfile(profile);
    }
    
    public DDocFacade getJDigiDocFacade() {
        return this.jDigiDocFacade;
    }
    
    public String getFormat() {
        return this.jDigiDocFacade.getFormat();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DDocContainer.class);
    }
}
