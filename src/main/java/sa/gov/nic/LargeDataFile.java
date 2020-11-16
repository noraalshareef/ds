// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.DSSDocument;
import sa.gov.nic.exceptions.InvalidDataFileException;
import sa.gov.nic.impl.StreamDocument;
import java.io.InputStream;
import org.slf4j.Logger;

public class LargeDataFile extends DataFile
{
    private static final Logger logger;
    
    public LargeDataFile(final InputStream stream, final String fileName, final String mimeType) {
        LargeDataFile.logger.debug("Large file name: " + fileName + ", mime type: " + mimeType);
        try {
            final DSSDocument document = (DSSDocument)new StreamDocument(stream, fileName, this.getMimeType(mimeType));
            this.setDocument(document);
        }
        catch (Exception e) {
            LargeDataFile.logger.error(e.getMessage());
            throw new InvalidDataFileException((Throwable)e);
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)LargeDataFile.class);
    }
}
