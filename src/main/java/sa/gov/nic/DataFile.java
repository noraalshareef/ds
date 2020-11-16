// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.exceptions.TechnicalException;
import java.io.OutputStream;
import java.io.IOException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.nio.file.Files;
import java.nio.file.Paths;
import sa.gov.nic.impl.StreamDocument;
import java.io.File;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import java.net.URL;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.MimeType;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import eu.europa.esig.dss.InMemoryDocument;
import java.io.ByteArrayInputStream;
import sa.gov.nic.exceptions.InvalidDataFileException;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.Digest;
import eu.europa.esig.dss.DSSDocument;
import org.slf4j.Logger;
import java.io.Serializable;

public class DataFile implements Serializable
{
    private static final Logger logger;
    private DSSDocument document;
    private Digest digest;
    private String id;
    
    public DataFile(final String path, final String mimeType) {
        this.document = null;
        this.digest = null;
        DataFile.logger.debug("Path: " + path + ", mime type: " + mimeType);
        try {
            (this.document = (DSSDocument)new FileDocument(path)).setMimeType(this.getMimeType(mimeType));
        }
        catch (Exception e) {
            DataFile.logger.error(e.getMessage());
            throw new InvalidDataFileException((Throwable)e);
        }
    }
    
    public DataFile(final byte[] data, final String fileName, final String mimeType) {
        this.document = null;
        this.digest = null;
        DataFile.logger.debug("File name: " + fileName + ", mime type: " + mimeType);
        final ByteArrayInputStream stream = new ByteArrayInputStream(data);
        this.document = (DSSDocument)new InMemoryDocument((InputStream)stream, fileName, this.getMimeType(mimeType));
        IOUtils.closeQuietly((InputStream)stream);
    }
    
    public DataFile(final InputStream stream, final String fileName, final String mimeType) {
        this.document = null;
        this.digest = null;
        DataFile.logger.debug("File name: " + fileName + ", mime type: " + mimeType);
        try {
            this.document = (DSSDocument)new InMemoryDocument(stream, fileName, this.getMimeType(mimeType));
        }
        catch (Exception e) {
            DataFile.logger.error(e.getMessage());
            throw new InvalidDataFileException((Throwable)e);
        }
    }
    
    protected DataFile(final DSSDocument document) {
        this.document = null;
        this.digest = null;
        this.document = document;
    }
    
    public DataFile() {
        this.document = null;
        this.digest = null;
    }
    
    protected MimeType getMimeType(final String mimeType) {
        try {
            final MimeType mimeTypeCode = MimeType.fromMimeTypeString(mimeType);
            DataFile.logger.debug("Mime type: ", (Object)mimeTypeCode);
            return mimeTypeCode;
        }
        catch (DSSException e) {
            DataFile.logger.error(e.getMessage());
            throw new InvalidDataFileException((Throwable)e);
        }
    }
    
    public byte[] calculateDigest() throws Exception {
        DataFile.logger.debug("");
        return this.calculateDigest(new URL("http://www.w3.org/2001/04/xmlenc#sha256"));
    }
    
    public byte[] calculateDigest(final URL method) {
        DataFile.logger.debug("URL method: " + method);
        if (this.digest == null) {
            final DigestAlgorithm digestAlgorithm = DigestAlgorithm.forXML(method.toString());
            this.digest = new Digest(digestAlgorithm, this.calculateDigestInternal(digestAlgorithm));
        }
        else {
            DataFile.logger.debug("Returning existing digest value");
        }
        return this.digest.getValue();
    }
    
    public byte[] calculateDigest(final sa.gov.nic.DigestAlgorithm digestType) {
        DataFile.logger.debug("");
        return this.calculateDigest(digestType.uri());
    }
    
    byte[] calculateDigestInternal(final DigestAlgorithm digestAlgorithm) {
        DataFile.logger.debug("Digest algorithm: " + digestAlgorithm);
        return DSSUtils.digest(digestAlgorithm, this.getBytes());
    }
    
    public String getName() {
        final String documentName = this.document.getName();
        final String name = new File(documentName).getName();
        DataFile.logger.trace("File name: for document " + documentName + " is " + name);
        return name;
    }
    
    public String getId() {
        DataFile.logger.debug("");
        return (this.id == null) ? this.getName() : this.id;
    }
    
    public long getFileSize() {
        DataFile.logger.debug("");
        Label_0101: {
            if (!(this.document instanceof StreamDocument)) {
                if (!(this.document instanceof FileDocument)) {
                    break Label_0101;
                }
            }
            try {
                final long fileSize = Files.size(Paths.get(this.document.getAbsolutePath(), new String[0]));
                DataFile.logger.debug("Document size: " + fileSize);
                return fileSize;
            }
            catch (IOException e) {
                DataFile.logger.error(e.getMessage());
                throw new DigiDoc4JException(e);
            }
        }
        final long fileSize = this.getBytes().length;
        DataFile.logger.debug("File document size: " + fileSize);
        return fileSize;
    }
    
    public String getMediaType() {
        DataFile.logger.debug("");
        final String mediaType = this.document.getMimeType().getMimeTypeString();
        DataFile.logger.debug("Media type is: " + mediaType);
        return mediaType;
    }
    
    public void setMediaType(final String mediaType) {
        final MimeType mimeType = this.getMimeType(mediaType);
        this.document.setMimeType(mimeType);
    }
    
    public void saveAs(final OutputStream out) throws IOException {
        DataFile.logger.debug("");
        out.write(this.getBytes());
        out.close();
    }
    
    public void saveAs(final String path) {
        try {
            DataFile.logger.debug("Path: " + path);
            this.document.save(path);
        }
        catch (IOException e) {
            DataFile.logger.error("Failed to save path " + path);
            throw new TechnicalException("Failed to save path " + path, e);
        }
    }
    
    public byte[] getBytes() {
        DataFile.logger.debug("");
        try {
            return IOUtils.toByteArray(this.document.openStream());
        }
        catch (IOException e) {
            throw new TechnicalException("Error reading document bytes: " + e.getMessage(), e);
        }
    }
    
    public InputStream getStream() {
        DataFile.logger.debug("");
        return this.document.openStream();
    }
    
    public void setId(final String dataFileId) {
        DataFile.logger.debug("");
        this.id = dataFileId;
    }
    
    public DSSDocument getDocument() {
        return this.document;
    }
    
    public void setDocument(final DSSDocument document) {
        this.document = document;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DataFile.class);
    }
}
