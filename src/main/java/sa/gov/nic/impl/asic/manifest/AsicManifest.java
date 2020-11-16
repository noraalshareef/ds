// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.manifest;

import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.DOMImplementationLS;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import sa.gov.nic.DataFile;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import sa.gov.nic.exceptions.TechnicalException;
import eu.europa.esig.dss.MimeType;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.slf4j.Logger;

public class AsicManifest
{
    private static final Logger logger;
    public static final String XML_PATH = "META-INF/manifest.xml";
    private Document dom;
    private Element rootElement;
    
    public AsicManifest() {
        this.generateAsicManifest(null);
    }
    
    public AsicManifest(final String containerType) {
        this.generateAsicManifest(containerType);
    }
    
    private void generateAsicManifest(final String containerType) {
        AsicManifest.logger.debug("Creating new manifest");
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            this.dom = db.newDocument();
            (this.rootElement = this.dom.createElement("manifest:manifest")).setAttribute("xmlns:manifest", "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0");
            final Element firstChild = this.dom.createElement("manifest:file-entry");
            firstChild.setAttribute("manifest:full-path", "/");
            if ("ASICS".equals(containerType)) {
                firstChild.setAttribute("manifest:media-type", MimeType.ASICS.getMimeTypeString());
            }
            else {
                firstChild.setAttribute("manifest:media-type", MimeType.ASICE.getMimeTypeString());
            }
            this.rootElement.appendChild(firstChild);
            this.dom.appendChild(this.rootElement);
        }
        catch (ParserConfigurationException e) {
            AsicManifest.logger.error(e.getMessage());
            throw new TechnicalException("Error creating manifest", e);
        }
    }
    
    public void addFileEntry(final Collection<DataFile> dataFiles) {
        for (final DataFile dataFile : dataFiles) {
            AsicManifest.logger.debug("Adding " + dataFile.getName() + " to manifest");
            final Element childElement = this.dom.createElement("manifest:file-entry");
            childElement.setAttribute("manifest:media-type", dataFile.getMediaType());
            childElement.setAttribute("manifest:full-path", dataFile.getName());
            this.rootElement.appendChild(childElement);
        }
    }
    
    public byte[] getBytes() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.writeTo(outputStream);
        return outputStream.toByteArray();
    }
    
    public void writeTo(final OutputStream outputStream) {
        final DOMImplementationLS implementation = (DOMImplementationLS)this.dom.getImplementation();
        final LSOutput lsOutput = implementation.createLSOutput();
        lsOutput.setByteStream(outputStream);
        final LSSerializer writer = implementation.createLSSerializer();
        writer.write(this.dom, lsOutput);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicManifest.class);
    }
}
