// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.DataFile;
import java.util.zip.ZipEntry;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.Configuration;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;

public class AsicStreamContainerParser extends AsicContainerParser
{
    private static final Logger logger;
    private ZipInputStream zipInputStream;
    
    public AsicStreamContainerParser(final InputStream inputStream, final Configuration configuration) {
        super(configuration);
        this.zipInputStream = new ZipInputStream(inputStream);
    }
    
    @Override
    protected void parseContainer() {
        this.parseZipStream();
        this.updateDataFilesMimeType();
    }
    
    private void parseZipStream() {
        AsicStreamContainerParser.logger.debug("Parsing zip stream");
        try {
            ZipEntry entry;
            while ((entry = this.zipInputStream.getNextEntry()) != null) {
                this.parseEntry(entry);
            }
        }
        catch (IOException e) {
            AsicStreamContainerParser.logger.error("Error reading asic container stream: " + e.getMessage());
            throw new TechnicalException("Error reading asic container stream: ", e);
        }
        finally {
            IOUtils.closeQuietly((InputStream)this.zipInputStream);
        }
    }
    
    private void updateDataFilesMimeType() {
        for (final DataFile dataFile : this.getDataFiles().values()) {
            final String fileName = dataFile.getName();
            final String mimeType = this.getDataFileMimeType(fileName);
            dataFile.setMediaType(mimeType);
        }
    }
    
    @Override
    protected void extractManifest(final ZipEntry entry) {
        final AsicEntry asicEntry = this.extractAsicEntry(entry);
        this.parseManifestEntry(asicEntry.getContent());
    }
    
    @Override
    protected InputStream getZipEntryInputStream(final ZipEntry entry) {
        return this.zipInputStream;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicStreamContainerParser.class);
    }
}
