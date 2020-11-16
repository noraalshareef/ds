// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.manifest;

import org.slf4j.LoggerFactory;
import sa.gov.nic.exceptions.DuplicateDataFileException;
import org.w3c.dom.NamedNodeMap;
import eu.europa.esig.dss.DomUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import eu.europa.esig.dss.DSSDocument;
import org.slf4j.Logger;
import java.io.Serializable;

public class ManifestParser implements Serializable
{
    private static final Logger logger;
    private static final String NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0";
    private DSSDocument manifestFile;
    private Map<String, ManifestEntry> entries;
    
    public ManifestParser(final DSSDocument manifestFile) {
        this.manifestFile = manifestFile;
    }
    
    public boolean containsManifestFile() {
        return this.manifestFile != null;
    }
    
    public Map<String, ManifestEntry> getManifestFileItems() {
        if (!this.containsManifestFile()) {
            return Collections.emptyMap();
        }
        this.entries = new HashMap<String, ManifestEntry>();
        this.loadFileEntriesFromManifest();
        return this.entries;
    }
    
    private void loadFileEntriesFromManifest() {
        final Element root = this.loadManifestXml();
        for (Node firstChild = root.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            final String nodeName = firstChild.getLocalName();
            if ("file-entry".equals(nodeName)) {
                this.addFileEntry(firstChild);
            }
        }
    }
    
    private Element loadManifestXml() {
        return DomUtils.buildDOM(this.manifestFile).getDocumentElement();
    }
    
    private void addFileEntry(final Node firstChild) {
        final NamedNodeMap attributes = firstChild.getAttributes();
        final String filePath = attributes.getNamedItemNS("urn:oasis:names:tc:opendocument:xmlns:manifest:1.0", "full-path").getTextContent();
        final String mimeType = attributes.getNamedItemNS("urn:oasis:names:tc:opendocument:xmlns:manifest:1.0", "media-type").getTextContent();
        if (!"/".equals(filePath)) {
            this.validateNotDuplicateFile(filePath);
            this.entries.put(filePath, new ManifestEntry(filePath, mimeType));
        }
    }
    
    private void validateNotDuplicateFile(final String filePath) {
        if (this.entries.containsKey(filePath)) {
            final DuplicateDataFileException digiDoc4JException = new DuplicateDataFileException("duplicate entry in manifest file: " + filePath);
            ManifestParser.logger.error(digiDoc4JException.getMessage());
            throw digiDoc4JException;
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ManifestParser.class);
    }
}
