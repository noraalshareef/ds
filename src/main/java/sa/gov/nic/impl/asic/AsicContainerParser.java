// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.Collection;
import sa.gov.nic.exceptions.DuplicateDataFileException;
import sa.gov.nic.exceptions.ContainerWithoutFilesException;
import sa.gov.nic.exceptions.UnsupportedFormatException;
import sa.gov.nic.impl.StreamDocument;
import sa.gov.nic.utils.MimeTypeUtil;
import eu.europa.esig.dss.MimeType;
import java.io.IOException;
import sa.gov.nic.exceptions.TechnicalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import eu.europa.esig.dss.InMemoryDocument;
import org.apache.commons.io.input.BOMInputStream;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.Collections;
import java.util.ArrayList;
import sa.gov.nic.Configuration;
import sa.gov.nic.impl.asic.manifest.ManifestParser;
import sa.gov.nic.impl.asic.manifest.ManifestEntry;
import java.util.Map;
import sa.gov.nic.DataFile;
import java.util.LinkedHashMap;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public abstract class AsicContainerParser
{
    public static final String MANIFEST = "META-INF/manifest.xml";
    public static final String TIMESTAMP_TOKEN = "META-INF/timestamp.tst";
    private static final Logger logger;
    private static final String SIGNATURES_FILE_REGEX = "META-INF/(.*)signatures(.*).xml";
    private static final Pattern SIGNATURE_FILE_ENDING_PATTERN;
    private AsicParseResult parseResult;
    private List<DSSDocument> signatures;
    private LinkedHashMap<String, DataFile> dataFiles;
    private List<DSSDocument> detachedContents;
    private Integer currentSignatureFileIndex;
    private String mimeType;
    private String zipFileComment;
    private List<AsicEntry> asicEntries;
    private Map<String, ManifestEntry> manifestFileItems;
    private ManifestParser manifestParser;
    private boolean storeDataFilesOnlyInMemory;
    private boolean manifestFound;
    private long maxDataFileCachedInBytes;
    private DataFile timestampToken;
    
    protected AsicContainerParser(final Configuration configuration) {
        this.parseResult = new AsicParseResult();
        this.signatures = new ArrayList<DSSDocument>();
        this.dataFiles = new LinkedHashMap<String, DataFile>();
        this.detachedContents = new ArrayList<DSSDocument>();
        this.asicEntries = new ArrayList<AsicEntry>();
        this.manifestFileItems = Collections.emptyMap();
        this.manifestFound = false;
        this.storeDataFilesOnlyInMemory = configuration.storeDataFilesOnlyInMemory();
        this.maxDataFileCachedInBytes = configuration.getMaxDataFileCachedInBytes();
    }
    
    public AsicParseResult read() {
        this.parseContainer();
        this.validateParseResult();
        this.populateParseResult();
        return this.parseResult;
    }
    
    protected abstract void parseContainer();
    
    protected abstract void extractManifest(final ZipEntry p0);
    
    protected abstract InputStream getZipEntryInputStream(final ZipEntry p0);
    
    protected void parseManifestEntry(final DSSDocument manifestFile) {
        AsicContainerParser.logger.debug("Parsing manifest");
        this.manifestParser = new ManifestParser(manifestFile);
        this.manifestFileItems = this.manifestParser.getManifestFileItems();
    }
    
    protected void parseEntry(final ZipEntry entry) {
        final String entryName = entry.getName();
        AsicContainerParser.logger.debug("Paring zip entry " + entryName + " with comment: " + entry.getComment());
        if (this.isMimeType(entryName)) {
            this.extractMimeType(entry);
        }
        else if (this.isManifest(entryName)) {
            if (this.manifestFound) {
                throw new DigiDoc4JException("Multiple manifest.xml files disallowed");
            }
            this.manifestFound = true;
            this.extractManifest(entry);
        }
        else if (this.isSignaturesFile(entryName)) {
            this.determineCurrentSignatureFileIndex(entryName);
            this.extractSignature(entry);
        }
        else if (this.isDataFile(entryName)) {
            this.extractDataFile(entry);
        }
        else if (this.isTimeStampToken(entryName)) {
            this.extractTimeStamp(entry);
        }
        else {
            this.extractAsicEntry(entry);
        }
    }
    
    private void extractMimeType(final ZipEntry entry) {
        try {
            final InputStream zipFileInputStream = this.getZipEntryInputStream(entry);
            final BOMInputStream bomInputStream = new BOMInputStream(zipFileInputStream);
            final DSSDocument document = (DSSDocument)new InMemoryDocument((InputStream)bomInputStream);
            this.mimeType = StringUtils.trim(IOUtils.toString(this.getDocumentBytes(document), "UTF-8"));
            this.extractAsicEntry(entry, document);
        }
        catch (IOException e) {
            AsicContainerParser.logger.error("Error parsing container mime type: " + e.getMessage());
            throw new TechnicalException("Error parsing container mime type: " + e.getMessage(), e);
        }
    }
    
    private void extractSignature(final ZipEntry entry) {
        AsicContainerParser.logger.debug("Extracting signature");
        final InputStream zipFileInputStream = this.getZipEntryInputStream(entry);
        final String fileName = entry.getName();
        final InMemoryDocument document = new InMemoryDocument(zipFileInputStream, fileName);
        this.signatures.add((DSSDocument)document);
        this.extractSignatureAsicEntry(entry, (DSSDocument)document);
    }
    
    private void extractTimeStamp(final ZipEntry entry) {
        AsicContainerParser.logger.debug("Extracting timestamp file");
        final InputStream zipFileInputStream = this.getZipEntryInputStream(entry);
        final String fileName = entry.getName();
        this.timestampToken = new DataFile(zipFileInputStream, fileName, MimeType.TST.getMimeTypeString());
    }
    
    private void extractDataFile(final ZipEntry entry) {
        AsicContainerParser.logger.debug("Extracting data file");
        final String fileName = entry.getName();
        this.validateDataFile(fileName);
        final DSSDocument document = this.extractStreamDocument(entry);
        final DataFile dataFile = new AsicDataFile(document);
        this.dataFiles.put(fileName, dataFile);
        this.detachedContents.add(document);
        this.extractAsicEntry(entry, document);
    }
    
    private DSSDocument extractStreamDocument(final ZipEntry entry) {
        AsicContainerParser.logger.debug("Zip entry size is <{}> bytes", (Object)entry.getSize());
        final MimeType mimeTypeCode = MimeTypeUtil.mimeTypeOf(this.getDataFileMimeType(entry.getName()));
        if (this.storeDataFilesOnlyInMemory || entry.getSize() <= this.maxDataFileCachedInBytes) {
            return (DSSDocument)new InMemoryDocument(this.getZipEntryInputStream(entry), entry.getName(), mimeTypeCode);
        }
        return (DSSDocument)new StreamDocument(this.getZipEntryInputStream(entry), entry.getName(), mimeTypeCode);
    }
    
    protected AsicEntry extractAsicEntry(final ZipEntry entry) {
        AsicContainerParser.logger.debug("Extracting asic entry");
        final DSSDocument document = this.extractStreamDocument(entry);
        return this.extractAsicEntry(entry, document);
    }
    
    private AsicEntry extractAsicEntry(final ZipEntry zipEntry, final DSSDocument document) {
        final AsicEntry asicEntry = new AsicEntry(zipEntry);
        asicEntry.setContent(document);
        this.asicEntries.add(asicEntry);
        return asicEntry;
    }
    
    private void extractSignatureAsicEntry(final ZipEntry entry, final DSSDocument document) {
        final AsicEntry asicEntry = this.extractAsicEntry(entry, document);
        asicEntry.setSignature(true);
    }
    
    protected String getDataFileMimeType(final String fileName) {
        if (this.manifestFileItems.containsKey(fileName)) {
            final ManifestEntry manifestEntry = this.manifestFileItems.get(fileName);
            return manifestEntry.getMimeType();
        }
        final MimeType mimetype = MimeType.fromFileName(fileName);
        return mimetype.getMimeTypeString();
    }
    
    private void validateParseResult() {
        if (!StringUtils.equalsIgnoreCase((CharSequence)MimeType.ASICE.getMimeTypeString(), (CharSequence)this.mimeType) && !StringUtils.equalsIgnoreCase((CharSequence)MimeType.ASICS.getMimeTypeString(), (CharSequence)this.mimeType)) {
            AsicContainerParser.logger.error("Container mime type is not " + MimeType.ASICE.getMimeTypeString() + " but is " + this.mimeType);
            throw new UnsupportedFormatException("Container mime type is not " + MimeType.ASICE.getMimeTypeString() + " OR " + MimeType.ASICS.getMimeTypeString() + " but is " + this.mimeType);
        }
        if (!this.signatures.isEmpty() && this.dataFiles.isEmpty()) {
            throw new ContainerWithoutFilesException("The reference data object(s) is not found!");
        }
    }
    
    private void validateDataFile(final String fileName) {
        if (this.dataFiles.containsKey(fileName)) {
            AsicContainerParser.logger.error("Container contains duplicate data file: " + fileName);
            throw new DuplicateDataFileException("Container contains duplicate data file: " + fileName);
        }
    }
    
    private void populateParseResult() {
        final Collection<DataFile> files = this.dataFiles.values();
        this.parseResult.setDataFiles(new ArrayList<DataFile>(files));
        this.parseResult.setSignatures(this.signatures);
        this.parseResult.setCurrentUsedSignatureFileIndex(this.currentSignatureFileIndex);
        this.parseResult.setDetachedContents(this.detachedContents);
        this.parseResult.setManifestParser(this.manifestParser);
        this.parseResult.setZipFileComment(this.zipFileComment);
        this.parseResult.setAsicEntries(this.asicEntries);
        this.parseResult.setTimeStampToken(this.timestampToken);
        this.parseResult.setMimeType(this.mimeType);
    }
    
    private boolean isMimeType(final String entryName) {
        return StringUtils.equalsIgnoreCase((CharSequence)"mimetype", (CharSequence)entryName);
    }
    
    private boolean isDataFile(final String entryName) {
        return !entryName.startsWith("META-INF/") && !this.isMimeType(entryName);
    }
    
    private boolean isTimeStampToken(final String entryName) {
        return StringUtils.equalsIgnoreCase((CharSequence)"META-INF/timestamp.tst", (CharSequence)entryName);
    }
    
    private boolean isManifest(final String entryName) {
        return StringUtils.equalsIgnoreCase((CharSequence)"META-INF/manifest.xml", (CharSequence)entryName);
    }
    
    private boolean isSignaturesFile(final String entryName) {
        return entryName.matches("META-INF/(.*)signatures(.*).xml");
    }
    
    private void determineCurrentSignatureFileIndex(final String entryName) {
        final Matcher fileEndingMatcher = AsicContainerParser.SIGNATURE_FILE_ENDING_PATTERN.matcher(entryName);
        final boolean fileEndingFound = fileEndingMatcher.find();
        if (fileEndingFound) {
            final String fileEnding = fileEndingMatcher.group();
            final String indexNumber = fileEnding.replace(".xml", "");
            final int fileIndex = Integer.parseInt(indexNumber);
            if (this.currentSignatureFileIndex == null || this.currentSignatureFileIndex <= fileIndex) {
                this.currentSignatureFileIndex = fileIndex;
            }
        }
    }
    
    private byte[] getDocumentBytes(final DSSDocument document) {
        try {
            return IOUtils.toByteArray(document.openStream());
        }
        catch (IOException e) {
            AsicContainerParser.logger.error("Error getting document content: " + e.getMessage());
            throw new TechnicalException("Error getting document content: " + e.getMessage(), e);
        }
    }
    
    void setZipFileComment(final String zipFileComment) {
        this.zipFileComment = zipFileComment;
    }
    
    LinkedHashMap<String, DataFile> getDataFiles() {
        return this.dataFiles;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicContainerParser.class);
        SIGNATURE_FILE_ENDING_PATTERN = Pattern.compile("(\\d+).xml");
    }
}
