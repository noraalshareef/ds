// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.manifest;

import org.slf4j.LoggerFactory;
import java.net.URISyntaxException;
import java.net.URI;
import org.w3c.dom.Node;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import eu.europa.esig.dss.DomUtils;
import org.apache.xml.security.signature.Reference;
import sa.gov.nic.impl.asic.asice.AsicESignature;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignature;
import sa.gov.nic.exceptions.DigiDoc4JException;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import sa.gov.nic.Signature;
import java.util.Collection;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import org.slf4j.Logger;

public class ManifestValidator
{
    public static final String MANIFEST_PATH = "META-INF/manifest.xml";
    public static final String MIMETYPE_PATH = "mimetype";
    private static final Logger logger;
    private List<DSSDocument> detachedContents;
    private ManifestParser manifestParser;
    private Collection<Signature> signatures;
    
    public ManifestValidator(final ManifestParser manifestParser, final List<DSSDocument> detachedContents, final Collection<Signature> signatures) {
        this.manifestParser = manifestParser;
        this.detachedContents = detachedContents;
        this.signatures = signatures;
    }
    
    public static List<ManifestErrorMessage> validateEntries(final Map<String, ManifestEntry> manifestEntries, final Set<ManifestEntry> signatureEntries, final String signatureId) {
        ManifestValidator.logger.debug("");
        final ArrayList<ManifestErrorMessage> errorMessages = new ArrayList<ManifestErrorMessage>();
        if (signatureEntries.size() == 0) {
            return errorMessages;
        }
        final Set<ManifestEntry> one = new HashSet<ManifestEntry>(manifestEntries.values());
        final Set<ManifestEntry> onePrim = new HashSet<ManifestEntry>(manifestEntries.values());
        final Set<ManifestEntry> two = new HashSet<ManifestEntry>(signatureEntries);
        final Set<ManifestEntry> twoPrim = new HashSet<ManifestEntry>();
        for (final ManifestEntry manifestEntry : signatureEntries) {
            final String mimeType = manifestEntry.getMimeType();
            final String alterName = manifestEntry.getFileName().replaceAll("\\+", " ");
            twoPrim.add(new ManifestEntry(alterName, mimeType));
        }
        one.removeAll(signatureEntries);
        onePrim.removeAll(twoPrim);
        two.removeAll(manifestEntries.values());
        twoPrim.removeAll(manifestEntries.values());
        if (one.size() > 0 && onePrim.size() > 0) {
            for (final ManifestEntry manifestEntry : one) {
                final String fileName = manifestEntry.getFileName();
                final ManifestEntry signatureEntry = signatureEntryForFile(fileName, signatureEntries);
                if (signatureEntry != null) {
                    errorMessages.add(new ManifestErrorMessage("Manifest file has an entry for file " + fileName + " with mimetype " + manifestEntry.getMimeType() + " but the signature file for signature " + signatureId + " indicates the mimetype is " + signatureEntry.getMimeType(), signatureId));
                    two.remove(signatureEntry);
                }
                else {
                    errorMessages.add(new ManifestErrorMessage("Manifest file has an entry for file " + fileName + " with mimetype " + manifestEntry.getMimeType() + " but the signature file for signature " + signatureId + " does not have an entry for this file", signatureId));
                }
            }
        }
        if (two.size() > 0 && twoPrim.size() > 0) {
            for (final ManifestEntry manifestEntry : two) {
                errorMessages.add(new ManifestErrorMessage("The signature file for signature " + signatureId + " has an entry for file " + manifestEntry.getFileName() + " with mimetype " + manifestEntry.getMimeType() + " but the manifest file does not have an entry for this file", signatureId));
            }
        }
        return errorMessages;
    }
    
    private static ManifestEntry signatureEntryForFile(final String fileName, final Set<ManifestEntry> signatureEntries) {
        ManifestValidator.logger.debug("File name: " + fileName);
        for (final ManifestEntry signatureEntry : signatureEntries) {
            if (fileName.equals(signatureEntry.getFileName())) {
                return signatureEntry;
            }
        }
        return null;
    }
    
    public List<ManifestErrorMessage> validateDocument() {
        ManifestValidator.logger.debug("");
        if (!this.manifestParser.containsManifestFile()) {
            final String errorMessage = "Container does not contain manifest file.";
            ManifestValidator.logger.error(errorMessage);
            throw new DigiDoc4JException(errorMessage);
        }
        final List<ManifestErrorMessage> errorMessages = new ArrayList<ManifestErrorMessage>();
        final Map<String, ManifestEntry> manifestEntries = this.manifestParser.getManifestFileItems();
        Set<ManifestEntry> signatureEntries = new HashSet<ManifestEntry>();
        for (final Signature signature : this.signatures) {
            signatureEntries = this.getSignatureEntries(signature);
            errorMessages.addAll(validateEntries(manifestEntries, signatureEntries, signature.getId()));
        }
        errorMessages.addAll(this.validateFilesInContainer(signatureEntries));
        ManifestValidator.logger.info("Validation of meta data within the manifest file and signature files error count: " + errorMessages.size());
        return errorMessages;
    }
    
    private List<ManifestErrorMessage> validateFilesInContainer(final Set<ManifestEntry> signatureEntries) {
        ManifestValidator.logger.debug("");
        final ArrayList<ManifestErrorMessage> errorMessages = new ArrayList<ManifestErrorMessage>();
        if (signatureEntries.size() == 0) {
            return errorMessages;
        }
        final Set<String> signatureEntriesFileNames = this.getFileNamesFromManifestEntrySet(signatureEntries);
        final List<String> filesInContainer = this.getFilesInContainer();
        for (final String fileInContainer : filesInContainer) {
            final String alterName = fileInContainer.replaceAll("\\ ", "+");
            if (!signatureEntriesFileNames.contains(fileInContainer) && !signatureEntriesFileNames.contains(alterName)) {
                ManifestValidator.logger.error("Container contains unsigned data file '" + fileInContainer + "'");
                errorMessages.add(new ManifestErrorMessage("Container contains a file named " + fileInContainer + " which is not found in the signature file"));
            }
        }
        return errorMessages;
    }
    
    private Set<String> getFileNamesFromManifestEntrySet(final Set<ManifestEntry> signatureEntries) {
        final Set<String> signatureEntriesFileNames = new HashSet<String>(signatureEntries.size());
        for (final ManifestEntry entry : signatureEntries) {
            signatureEntriesFileNames.add(entry.getFileName());
        }
        return signatureEntriesFileNames;
    }
    
    private Set<ManifestEntry> getSignatureEntries(final Signature signature) {
        final Set<ManifestEntry> signatureEntries = new HashSet<ManifestEntry>();
        XadesSignature origin;
        if (signature.getClass() == BDocSignature.class) {
            origin = ((BDocSignature)signature).getOrigin();
        }
        else {
            origin = ((AsicESignature)signature).getOrigin();
        }
        final List<Reference> references = origin.getReferences();
        for (final Reference reference : references) {
            if (reference.getType().equals("")) {
                String mimeTypeString = null;
                final Node signatureNode = origin.getDssSignature().getSignatureElement();
                final Node node = DomUtils.getNode(signatureNode, "./ds:SignedInfo/ds:Reference[@URI=\"" + reference.getURI() + "\"]");
                if (node != null) {
                    final String referenceId = node.getAttributes().getNamedItem("Id").getNodeValue();
                    mimeTypeString = DomUtils.getValue(signatureNode, "./ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedDataObjectProperties/xades:DataObjectFormat[@ObjectReference=\"#" + referenceId + "\"]/xades:MimeType");
                }
                final String uri = this.getFileURI(reference);
                signatureEntries.add(new ManifestEntry(uri, mimeTypeString));
            }
        }
        return signatureEntries;
    }
    
    private String getFileURI(final Reference reference) {
        String uri = reference.getURI();
        try {
            uri = new URI(uri).getPath();
        }
        catch (URISyntaxException e) {
            ManifestValidator.logger.warn("Does not parse as an URI, therefore assuming it's not encoded: '" + uri + "'");
        }
        return uri;
    }
    
    private List<String> getFilesInContainer() {
        final List<String> fileEntries = new ArrayList<String>();
        final List<String> signatureFileNames = this.getSignatureFileNames();
        for (final DSSDocument detachedContent : this.detachedContents) {
            final String name = detachedContent.getName();
            if (!"META-INF/manifest.xml".equals(name) && !"META-INF/".equals(name) && !"mimetype".equals(name) && !signatureFileNames.contains(name)) {
                fileEntries.add(name);
            }
        }
        return fileEntries;
    }
    
    private List<String> getSignatureFileNames() {
        final List<String> signatureFileNames = new ArrayList<String>();
        for (final Signature signature : this.signatures) {
            final String signatureFileName = "META-INF/signature" + signature.getId().toLowerCase() + ".xml";
            if (signatureFileNames.contains(signatureFileName)) {
                final String errorMessage = "Duplicate signature file: " + signatureFileName;
                ManifestValidator.logger.error(errorMessage);
                throw new DigiDoc4JException(errorMessage);
            }
            signatureFileNames.add(signatureFileName);
        }
        return signatureFileNames;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ManifestValidator.class);
    }
}
