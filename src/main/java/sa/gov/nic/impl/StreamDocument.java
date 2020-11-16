// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl;

import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import org.apache.commons.codec.binary.Base64;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import eu.europa.esig.dss.DSSException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import eu.europa.esig.dss.MimeType;
import org.slf4j.Logger;
import eu.europa.esig.dss.CommonDocument;

public class StreamDocument extends CommonDocument
{
    private static final Logger logger;
    private static final int MAX_SIZE_IN_MEMORY = 5120;
    String documentName;
    MimeType mimeType;
    File temporaryFile;
    
    public StreamDocument(final InputStream stream, final String documentName, final MimeType mimeType) {
        StreamDocument.logger.debug("Document name: " + documentName + ", mime type: " + mimeType);
        this.createTemporaryFileOfStream(stream);
        this.documentName = documentName;
        this.mimeType = mimeType;
    }
    
    private void createTemporaryFileOfStream(final InputStream stream) {
        StreamDocument.logger.debug("");
        final byte[] bytes = new byte[5120];
        FileOutputStream out = null;
        try {
            this.temporaryFile = File.createTempFile("digidoc4j", ".tmp");
            out = new FileOutputStream(this.temporaryFile);
            int result;
            while ((result = stream.read(bytes)) > 0) {
                out.write(bytes, 0, result);
            }
            out.flush();
            this.temporaryFile.deleteOnExit();
        }
        catch (IOException e) {
            StreamDocument.logger.error(e.getMessage());
            throw new DSSException((Throwable)e);
        }
        finally {
            IOUtils.closeQuietly((OutputStream)out);
        }
    }
    
    public InputStream openStream() throws DSSException {
        StreamDocument.logger.debug("");
        try {
            return this.getTemporaryFileAsStream();
        }
        catch (FileNotFoundException e) {
            StreamDocument.logger.error(e.getMessage());
            throw new DSSException((Throwable)e);
        }
    }
    
    public String getName() {
        StreamDocument.logger.debug("");
        return this.documentName;
    }
    
    public void setName(final String s) {
    }
    
    public String getAbsolutePath() {
        StreamDocument.logger.debug("");
        return this.temporaryFile.getAbsolutePath();
    }
    
    public MimeType getMimeType() {
        StreamDocument.logger.debug("Mime type: " + this.mimeType);
        return this.mimeType;
    }
    
    public void setMimeType(final MimeType mimeType) {
        StreamDocument.logger.debug("Mime type: " + mimeType);
        this.mimeType = mimeType;
    }
    
    public void save(final String filePath) {
        StreamDocument.logger.debug("File Path: " + filePath);
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            try {
                IOUtils.copy((InputStream)this.getTemporaryFileAsStream(), (OutputStream)fileOutputStream);
            }
            finally {
                fileOutputStream.close();
            }
        }
        catch (IOException e) {
            StreamDocument.logger.error(e.getMessage());
            throw new DSSException((Throwable)e);
        }
    }
    
    public String getDigest(final DigestAlgorithm digestAlgorithm) {
        StreamDocument.logger.debug("Digest algorithm: " + digestAlgorithm);
        byte[] digestBytes;
        try {
            digestBytes = DSSUtils.digest(digestAlgorithm, (InputStream)this.getTemporaryFileAsStream());
        }
        catch (FileNotFoundException e) {
            StreamDocument.logger.error(e.getMessage());
            throw new DSSException((Throwable)e);
        }
        return Base64.encodeBase64String(digestBytes);
    }
    
    protected FileInputStream getTemporaryFileAsStream() throws FileNotFoundException {
        return new FileInputStream(this.temporaryFile);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)StreamDocument.class);
    }
}
