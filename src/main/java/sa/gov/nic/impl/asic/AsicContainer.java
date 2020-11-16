// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import sa.gov.nic.SignatureToken;
import sa.gov.nic.DigestAlgorithm;
import sa.gov.nic.SignatureParameters;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.SignedInfo;
import java.security.cert.X509Certificate;
import sa.gov.nic.exceptions.InvalidSignatureException;
import org.apache.commons.io.IOUtils;
import sa.gov.nic.SignatureBuilder;
import sa.gov.nic.exceptions.DataFileNotFoundException;
import java.util.Arrays;

import sa.gov.nic.exceptions.DuplicateDataFileException;
import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.exceptions.RemovingDataFileException;
import sa.gov.nic.impl.asic.xades.SignatureExtender;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.impl.asic.asice.AsicESignature;
import sa.gov.nic.impl.asic.asics.AsicSSignature;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import eu.europa.esig.dss.DSSDocument;
import java.io.OutputStream;
import java.io.IOException;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.utils.Helper;
import java.io.File;
import sa.gov.nic.impl.asic.asice.AsicEContainerValidator;
import sa.gov.nic.impl.asic.asice.bdoc.BDocContainerValidator;
import java.io.InputStream;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import sa.gov.nic.ValidationResult;
import sa.gov.nic.Signature;
import sa.gov.nic.DataFile;
import java.util.List;
import org.slf4j.Logger;
import sa.gov.nic.Configuration;
import sa.gov.nic.Container;

public abstract class AsicContainer implements Container
{
    protected Configuration configuration;
    private final Logger log;
    private List<DataFile> dataFiles;
    private List<Signature> newSignatures;
    private List<Signature> signatures;
    private List<DataFile> newDataFiles;
    private AsicParseResult containerParseResult;
    private ValidationResult validationResult;
    private boolean dataFilesHaveChanged;
    private String containerType;
    private DataFile timeStampToken;
    
    protected abstract String createUserAgent();
    
    public AsicContainer() {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = Configuration.getInstance();
    }
    
    public AsicContainer(final Configuration configuration) {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = configuration;
    }
    
    public AsicContainer(final String containerPath, final String containerType) {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = Configuration.getInstance();
        this.containerType = containerType;
        this.openContainer(containerPath);
    }
    
    public AsicContainer(final String containerPath, final Configuration configuration, final String containerType) {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = configuration;
        this.containerType = containerType;
        this.openContainer(containerPath);
    }
    
    public AsicContainer(final InputStream stream, final String containerType) {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = Configuration.getInstance();
        this.containerType = containerType;
        this.openContainer(stream);
    }
    
    public AsicContainer(final InputStream stream, final Configuration configuration, final String containerType) {
        this.log = LoggerFactory.getLogger((Class)AsicContainer.class);
        this.dataFiles = new ArrayList<DataFile>();
        this.newSignatures = new ArrayList<Signature>();
        this.signatures = new ArrayList<Signature>();
        this.newDataFiles = new ArrayList<DataFile>();
        this.containerType = "";
        this.configuration = configuration;
        this.containerType = containerType;
        this.openContainer(stream);
    }
    
    @Override
    public ValidationResult validate() {
        if (this.validationResult == null) {
            this.validationResult = this.validateContainer();
        }
        return this.validationResult;
    }
    
    protected ValidationResult validateContainer() {
        if (this.timeStampToken != null) {
            return this.validateTimestampToken();
        }
        if (!this.isNewContainer()) {
            if (this.containerType.equals(DocumentType.BDOC.toString())) {
                final BDocContainerValidator validator = new BDocContainerValidator(this.containerParseResult, this.getConfiguration());
                validator.setValidateManifest(!this.dataFilesHaveChanged);
                return validator.validate(this.getSignatures());
            }
            final AsicEContainerValidator validator2 = new AsicEContainerValidator(this.containerParseResult, this.getConfiguration());
            validator2.setValidateManifest(!this.dataFilesHaveChanged);
            return validator2.validate(this.getSignatures());
        }
        else {
            if (this.containerType.equals(DocumentType.BDOC.toString())) {
                return new BDocContainerValidator(this.getConfiguration()).validate(this.getSignatures());
            }
            return new AsicEContainerValidator(this.getConfiguration()).validate(this.getSignatures());
        }
    }
    
    private ValidationResult validateTimestampToken() {
        if (this.containerParseResult == null) {
            this.containerParseResult = new AsicStreamContainerParser(this.saveAsStream(), this.getConfiguration()).read();
        }
        final TimeStampTokenValidator timeStampTokenValidator = new TimeStampTokenValidator(this.containerParseResult);
        return timeStampTokenValidator.validate();
    }
    
    @Override
    public File saveAsFile(final String filePath) {
        this.log.debug("Saving container to file: " + filePath);
        final File file = new File(filePath);
        try (final OutputStream stream = Helper.bufferedOutputStream(file)) {
            this.save(stream);
            this.log.info("Container was saved to file " + filePath);
            return file;
        }
        catch (IOException e) {
            this.log.error("Unable to close stream: " + e.getMessage());
            throw new TechnicalException("Unable to close stream", e);
        }
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    protected abstract List<Signature> parseSignatureFiles(final List<DSSDocument> p0, final List<DSSDocument> p1);
    
    @Override
    public InputStream saveAsStream() {
        this.log.debug("Saving container as stream");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.save(outputStream);
        final InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        this.log.info("Container was saved to stream");
        return inputStream;
    }
    
    protected void validateIncomingSignature(final Signature signature) {
        if (signature == null) {
            throw new TechnicalException("ValidateIncomingSignature is null");
        }
        if (!(signature instanceof BDocSignature) && !(signature instanceof AsicSSignature) && !(signature instanceof AsicESignature) && !(signature instanceof AsicSignature)) {
            throw new TechnicalException("BDoc signature must be an instance of AsicSignature");
        }
    }
    
    protected List<Signature> extendAllSignatureProfile(final SignatureProfile profile, final List<Signature> signatures, final List<DataFile> dataFiles) {
        this.log.info("Extending all signatures' profile to " + profile.name());
        DetachedContentCreator detachedContentCreator = null;
        try {
            detachedContentCreator = new DetachedContentCreator().populate(dataFiles);
        }
        catch (Exception e) {
            this.log.error("Error in datafiles processing: " + e.getMessage());
            throw new DigiDoc4JException(e);
        }
        final List<DSSDocument> detachedContentList = detachedContentCreator.getDetachedContentList();
        final SignatureExtender signatureExtender = new SignatureExtender(this.getConfiguration(), detachedContentList);
        final List<DSSDocument> extendedSignatureDocuments = signatureExtender.extend(signatures, profile);
        final List<Signature> extendedSignatures = this.parseSignatureFiles(extendedSignatureDocuments, detachedContentList);
        this.log.debug("Finished extending all signatures");
        return extendedSignatures;
    }
    
    protected void validateDataFilesRemoval() {
        if (!this.getSignatures().isEmpty()) {
            this.log.error("Datafiles cannot be removed from an already signed container");
            throw new RemovingDataFileException();
        }
    }
    
    protected void verifyIfAllowedToAddDataFile(final String fileName) {
        if (this.getSignatures().size() > 0) {
            final String errorMessage = "Datafiles cannot be added to an already signed container";
            this.log.error(errorMessage);
            throw new DigiDoc4JException(errorMessage);
        }
        this.checkForDuplicateDataFile(fileName);
    }
    
    private void checkForDuplicateDataFile(final String fileName) {
        this.log.debug("");
        for (final DataFile dataFile : this.getDataFiles()) {
            final String dataFileName = dataFile.getName();
            if (StringUtils.equals((CharSequence)dataFileName, (CharSequence)fileName)) {
                final String errorMessage = "Data file " + fileName + " already exists";
                this.log.error(errorMessage);
                throw new DuplicateDataFileException(errorMessage);
            }
        }
    }
    
    public void setType(final String containerType) {
        this.containerType = containerType;
    }
    
    @Override
    public String getType() {
        return this.containerType;
    }
    
    private void openContainer(final String containerPath) {
        this.log.debug("Opening container from <{}>", (Object)containerPath);
        this.populateContainerWithParseResult(new AsicFileContainerParser(containerPath, this.getConfiguration()).read());
    }
    
    private void openContainer(final InputStream inputStream) {
        this.log.debug("Opening container from stream");
        this.populateContainerWithParseResult(new AsicStreamContainerParser(inputStream, this.getConfiguration()).read());
    }
    
    private void populateContainerWithParseResult(final AsicParseResult parseResult) {
        this.containerParseResult = parseResult;
        this.dataFiles.addAll(parseResult.getDataFiles());
        this.timeStampToken = parseResult.getTimeStampToken();
        this.signatures.addAll(this.parseSignatureFiles(parseResult.getSignatures(), parseResult.getDetachedContents()));
    }
    
    private void removeExistingSignature(final BDocSignature signature) {
        final DSSDocument signatureDocument = signature.getSignatureDocument();
        if (signatureDocument == null) {
            return;
        }
        final String signatureFileName = signatureDocument.getName();
        this.removeExistingFileFromContainer(signatureFileName);
    }
    
    private void removeExistingFileFromContainer(final String filePath) {
        this.log.debug("Removing file from the container: " + filePath);
        if (this.containerParseResult != null) {
            final List<AsicEntry> asicEntries = this.containerParseResult.getAsicEntries();
            for (final AsicEntry entry : asicEntries) {
                final String entryFileName = entry.getZipEntry().getName();
                if (StringUtils.equalsIgnoreCase((CharSequence)filePath, (CharSequence)entryFileName)) {
                    asicEntries.remove(entry);
                    this.log.debug("File was successfully removed");
                    break;
                }
            }
        }
    }
    
    private void removeAllExistingSignaturesFromContainer() {
        this.log.debug("Removing all existing signatures");
        for (final Signature signature : this.signatures) {
            this.removeExistingSignature((BDocSignature)signature);
        }
    }
    
    private int determineNextSignatureFileIndex() {
        final Integer currentUsedSignatureFileIndex = this.containerParseResult.getCurrentUsedSignatureFileIndex();
        if (currentUsedSignatureFileIndex == null) {
            return 0;
        }
        return currentUsedSignatureFileIndex + 1;
    }
    
    @Override
    public List<DataFile> getDataFiles() {
        return this.dataFiles;
    }
    
    @Override
    public DataFile addDataFile(final String path, final String mimeType) {
        final DataFile dataFile = new DataFile(path, mimeType);
        this.addDataFile(dataFile);
        return dataFile;
    }
    
    @Override
    public DataFile addDataFile(final InputStream inputStream, final String fileName, final String mimeType) {
        final DataFile dataFile = new DataFile(inputStream, fileName, mimeType);
        this.addDataFile(dataFile);
        return dataFile;
    }
    
    @Override
    public DataFile addDataFile(final File file, final String mimeType) {
        final DataFile dataFile = new DataFile(file.getPath(), mimeType);
        this.addDataFile(dataFile);
        return dataFile;
    }
    
    @Override
    public void addDataFile(final DataFile dataFile) {
        final String fileName = dataFile.getName();
        this.verifyIfAllowedToAddDataFile(fileName);
        if ("ASICS".equals(this.getType())) {
            if (this.dataFiles.size() > 1) {
                throw new DigiDoc4JException("DataFile is already exists");
            }
            if (this.newDataFiles.size() > 1) {
                throw new DigiDoc4JException("Not possible to add more than one datafile");
            }
        }
        this.dataFiles.add(dataFile);
        this.newDataFiles.add(dataFile);
        this.dataFilesHaveChanged = true;
        this.removeExistingFileFromContainer("META-INF/manifest.xml");
    }
    
    @Override
    public void addSignature(final Signature signature) {
        this.validateIncomingSignature(signature);
        this.newSignatures.add(signature);
        this.signatures.add(signature);
    }
    
    @Override
    public void setTimeStampToken(final DataFile timeStampToken) {
        this.timeStampToken = timeStampToken;
    }
    
    private byte[] getDigest() {
        final DataFile dataFile = this.getDataFiles().get(0);
        return dataFile.getBytes();
    }
    
    public boolean isTimestampTokenDefined() {
        return this.timeStampToken != null;
    }
    
    @Override
    public void extendSignatureProfile(final SignatureProfile profile) {
        if (!this.isNewContainer()) {
            this.removeAllExistingSignaturesFromContainer();
            final List<Signature> signatures = this.extendAllSignaturesProfile(profile, this.signatures, this.dataFiles);
            this.signatures = signatures;
            this.newSignatures = new ArrayList<Signature>(signatures);
        }
        else {
            this.signatures = this.extendAllSignaturesProfile(profile, this.signatures, this.dataFiles);
        }
    }
    
    private List<Signature> extendAllSignaturesProfile(final SignatureProfile profile, final List<Signature> signatures, final List<DataFile> dataFiles) {
        List<Signature> extendedSignatures;
        if ("ASICS".equals(this.getType())) {
            extendedSignatures = this.extendAllSignatureProfile(profile, signatures, Arrays.asList(dataFiles.get(0)));
        }
        else {
            extendedSignatures = this.extendAllSignatureProfile(profile, signatures, dataFiles);
        }
        return extendedSignatures;
    }
    
    @Override
    public void removeSignature(final Signature signature) {
        this.log.info("Removing signature " + signature.getId());
        if (!this.isNewContainer()) {
            this.validateIncomingSignature(signature);
            final boolean wasNewlyAddedSignature = this.newSignatures.remove(signature);
            final boolean wasIncludedInContainer = this.signatures.remove(signature);
            if (wasIncludedInContainer && !wasNewlyAddedSignature) {
                this.log.debug("This signature was included in the container before the container was opened");
                this.removeExistingSignature((BDocSignature)signature);
            }
        }
        else {
            this.signatures.remove(signature);
        }
    }
    
    @Deprecated
    @Override
    public void removeSignature(final int signatureId) {
        this.log.debug("Removing signature from index " + signatureId);
        if (!this.isNewContainer()) {
            final Signature signature = this.signatures.get(signatureId);
            if (signature != null) {
                this.removeSignature(signature);
            }
        }
        else {
            this.signatures.remove(signatureId);
        }
    }
    
    @Override
    public void removeDataFile(final String fileName) {
        if (!this.isNewContainer()) {
            this.log.error("Datafiles cannot be removed from an already signed container");
            throw new RemovingDataFileException();
        }
        this.log.info("Removing data file: " + fileName);
        this.validateDataFilesRemoval();
        for (final DataFile dataFile : this.dataFiles) {
            final String name = dataFile.getName();
            if (StringUtils.equals((CharSequence)fileName, (CharSequence)name)) {
                this.dataFiles.remove(dataFile);
                this.log.debug("Data file has been removed");
                return;
            }
        }
        throw new DataFileNotFoundException(fileName);
    }
    
    @Override
    public void removeDataFile(final DataFile file) {
        if (!this.isNewContainer()) {
            this.log.error("Datafiles cannot be removed from an already signed container");
            throw new RemovingDataFileException();
        }
        this.log.info("Removing data file: " + file.getName());
        this.validateDataFilesRemoval();
        final boolean wasRemovalSuccessful = this.dataFiles.remove(file);
        if (!wasRemovalSuccessful) {
            throw new DataFileNotFoundException(file.getName());
        }
    }
    
    private boolean isNewContainer() {
        return this.containerParseResult == null;
    }
    
    @Override
    public List<Signature> getSignatures() {
        return this.signatures;
    }
    
    protected void writeAsicContainer(final AsicContainerCreator zipCreator) {
        final String userAgent = this.createUserAgent();
        zipCreator.setZipComment(userAgent);
        if (!this.isNewContainer()) {
            final int nextSignatureFileIndex = this.determineNextSignatureFileIndex();
            zipCreator.writeExistingEntries(this.containerParseResult.getAsicEntries());
            if (this.dataFilesHaveChanged) {
                zipCreator.writeManifest(this.dataFiles, this.getType());
            }
            zipCreator.writeSignatures(this.newSignatures, nextSignatureFileIndex);
            zipCreator.writeDataFiles(this.newDataFiles);
            if (StringUtils.isNotBlank((CharSequence)this.containerParseResult.getZipFileComment())) {
                zipCreator.writeContainerComment(this.containerParseResult.getZipFileComment());
            }
        }
        else {
            final int startingSignatureFileIndex = 0;
            zipCreator.writeAsiceMimeType(this.getType());
            zipCreator.writeManifest(this.dataFiles, this.getType());
            zipCreator.writeDataFiles(this.dataFiles);
            if (this.timeStampToken != null && "ASICS".equals(this.getType())) {
                zipCreator.writeTimestampToken(this.timeStampToken);
            }
            else {
                zipCreator.writeSignatures(this.signatures, startingSignatureFileIndex);
            }
            zipCreator.writeContainerComment(userAgent);
        }
        zipCreator.finalizeZipFile();
    }
    
    @Deprecated
    @Override
    public void addRawSignature(final byte[] signatureDocument) {
        this.log.info("Adding raw signature");
        final Signature signature = SignatureBuilder.aSignature(this).openAdESSignature(signatureDocument);
        this.addSignature(signature);
    }
    
    @Deprecated
    @Override
    public void addRawSignature(final InputStream signatureStream) {
        try {
            final byte[] bytes = IOUtils.toByteArray(signatureStream);
            this.addRawSignature(bytes);
        }
        catch (IOException e) {
            this.log.error("Failed to read signature stream: " + e.getMessage());
            throw new InvalidSignatureException();
        }
    }
    
    @Deprecated
    @Override
    public int countDataFiles() {
        return this.getDataFiles().size();
    }
    
    @Deprecated
    @Override
    public int countSignatures() {
        return this.getSignatures().size();
    }
    
    @Deprecated
    @Override
    public DocumentType getDocumentType() {
        return DocumentType.BDOC;
    }
    
    @Deprecated
    @Override
    public String getVersion() {
        return "";
    }
    
    @Deprecated
    @Override
    public void extendTo(final SignatureProfile profile) {
        this.extendSignatureProfile(profile);
    }
    
    @Deprecated
    @Override
    public void save(final String path) {
        this.saveAsFile(path);
    }
    
    @Deprecated
    @Override
    public DataFile getDataFile(final int index) {
        return this.getDataFiles().get(index);
    }
    
    @Deprecated
    @Override
    public Signature getSignature(final int index) {
        return this.getSignatures().get(index);
    }
    
    @Deprecated
    @Override
    public SignedInfo prepareSigning(final X509Certificate signerCert) {
        throw new NotSupportedException("Prepare signing method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public String getSignatureProfile() {
        throw new NotSupportedException("Getting signature profile method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public void setSignatureParameters(final SignatureParameters signatureParameters) {
        throw new NotSupportedException("Setting signature parameters method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public DigestAlgorithm getDigestAlgorithm() {
        throw new NotSupportedException("Getting digest algorithm method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public Signature sign(final SignatureToken signatureToken) {
        throw new NotSupportedException("Sign method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public Signature signRaw(final byte[] rawSignature) {
        throw new NotSupportedException("Sign raw method is not supported by Asic container");
    }
    
    @Deprecated
    @Override
    public void setSignatureProfile(final SignatureProfile profile) {
        throw new NotSupportedException("Setting signature profile method is not supported by Asic container");
    }
}
