// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import sa.gov.nic.exceptions.TechnicalException;
import java.io.InputStream;
import java.util.Enumeration;
import java.io.Closeable;
import org.apache.commons.io.IOUtils;
import java.util.zip.ZipEntry;
import java.io.IOException;
import sa.gov.nic.Configuration;
import java.util.zip.ZipFile;
import org.slf4j.Logger;

public class AsicFileContainerParser extends AsicContainerParser
{
    private static final Logger logger;
    private ZipFile zipFile;
    
    public AsicFileContainerParser(final String containerPath, final Configuration configuration) {
        super(configuration);
        try {
            this.zipFile = new ZipFile(containerPath);
        }
        catch (IOException e) {
            AsicFileContainerParser.logger.error("Error reading container from " + containerPath + " - " + e.getMessage());
            throw new RuntimeException("Error reading container from " + containerPath);
        }
    }
    
    @Override
    protected void parseContainer() {
        AsicFileContainerParser.logger.debug("Parsing zip file");
        try {
            final String zipFileComment = this.zipFile.getComment();
            this.setZipFileComment(zipFileComment);
            this.parseZipFileManifest();
            final Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = (ZipEntry)entries.nextElement();
                this.parseEntry(zipEntry);
            }
        }
        finally {
            IOUtils.closeQuietly((Closeable)this.zipFile);
        }
    }
    
    @Override
    protected void extractManifest(final ZipEntry entry) {
        this.extractAsicEntry(entry);
    }
    
    @Override
    protected InputStream getZipEntryInputStream(final ZipEntry entry) {
        try {
            return this.zipFile.getInputStream(entry);
        }
        catch (IOException e) {
            AsicFileContainerParser.logger.error("Error reading data file '" + entry.getName() + "' from the asic container: " + e.getMessage());
            throw new TechnicalException("Error reading data file '" + entry.getName() + "' from the asic container", e);
        }
    }
    
    private void parseZipFileManifest() {
        final ZipEntry entry = this.zipFile.getEntry("META-INF/manifest.xml");
        if (entry == null) {
            return;
        }
        try {
            final InputStream manifestStream = this.getZipEntryInputStream(entry);
            final InMemoryDocument manifestFile = new InMemoryDocument(IOUtils.toByteArray(manifestStream));
            this.parseManifestEntry((DSSDocument)manifestFile);
        }
        catch (IOException e) {
            AsicFileContainerParser.logger.error("Error parsing manifest file: " + e.getMessage());
            throw new TechnicalException("Error parsing manifest file", e);
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicFileContainerParser.class);
    }
}
