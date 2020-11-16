//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic.impl.asic;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.MimeType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.DataFile;
import sa.gov.nic.Signature;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.impl.asic.manifest.AsicManifest;
import sa.gov.nic.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsicContainerCreator {
    private static final Logger logger = LoggerFactory.getLogger(AsicContainerCreator.class);
    private static final String ZIP_ENTRY_MIMETYPE = "mimetype";
    private static final Charset CHARSET;
    private final ZipOutputStream zipOutputStream;
    private final OutputStream outputStream;
    private String zipComment;

    /** @deprecated */
    @Deprecated
    public AsicContainerCreator(File containerPathToSave) {
        this(Helper.bufferedOutputStream(containerPathToSave));
    }

    public AsicContainerCreator(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.zipOutputStream = new ZipOutputStream(outputStream, CHARSET);
    }

    /** @deprecated */
    @Deprecated
    public AsicContainerCreator() {
        this((OutputStream)(new ByteArrayOutputStream()));
    }

    public void finalizeZipFile() {
        logger.debug("Finalizing asic zip file");

        try {
            this.zipOutputStream.finish();
        } catch (IOException var5) {
            handleIOException("Unable to finish creating asic ZIP container", var5);
        } finally {
            Helper.deleteTmpFiles();
        }

    }

    /** @deprecated */
    @Deprecated
    public InputStream fetchInputStreamOfFinalizedContainer() {
        if (this.outputStream instanceof ByteArrayOutputStream) {
            logger.debug("Fetching input stream of the finalized container");
            return new ByteArrayInputStream(((ByteArrayOutputStream)this.outputStream).toByteArray());
        } else {
            throw new NotSupportedException("instance not backed by an in-memory stream");
        }
    }

    public void writeAsiceMimeType(String containerType) {
        logger.debug("Writing asic mime type to asic zip file");
        String mimeTypeString;
        if ("ASICS".equals(containerType)) {
            mimeTypeString = MimeType.ASICS.getMimeTypeString();
        } else {
            mimeTypeString = MimeType.ASICE.getMimeTypeString();
        }

        byte[] mimeTypeBytes = mimeTypeString.getBytes(CHARSET);
        (new AsicContainerCreator.BytesEntryCallback(getAsicMimeTypeZipEntry(mimeTypeBytes), mimeTypeBytes)).write();
    }

    public void writeManifest(Collection<DataFile> dataFiles, String containerType) {
        logger.debug("Writing asic manifest");
        final AsicManifest manifest = new AsicManifest(containerType);
        manifest.addFileEntry(dataFiles);
        (new AsicContainerCreator.EntryCallback(new ZipEntry("META-INF/manifest.xml")) {
            void doWithEntryStream(OutputStream stream) throws IOException {
                manifest.writeTo(stream);
            }
        }).write();
    }

    public void writeDataFiles(Collection<DataFile> dataFiles) {
        logger.debug("Adding data files to the asic zip container");
        Iterator i$ = dataFiles.iterator();

        while(i$.hasNext()) {
            DataFile dataFile = (DataFile)i$.next();
            String name = dataFile.getName();
            logger.debug("Adding data file {}", name);
            this.zipOutputStream.setLevel(8);
            (new AsicContainerCreator.StreamEntryCallback(new ZipEntry(name), dataFile.getStream())).write();
        }

    }

    public void writeSignatures(Collection<Signature> signatures, int nextSignatureFileNameIndex) {
        logger.debug("Adding signatures to the asic zip container");
        int index = nextSignatureFileNameIndex;

        for(Iterator i$ = signatures.iterator(); i$.hasNext(); ++index) {
            Signature signature = (Signature)i$.next();
            String signatureFileName = "META-INF/signatures" + index + ".xml";
            (new AsicContainerCreator.BytesEntryCallback(new ZipEntry(signatureFileName), signature.getAdESSignature())).write();
        }

    }

    public void writeTimestampToken(DataFile dataFile) {
        logger.debug("Adding signatures to the asic zip container");
        String signatureFileName = "META-INF/timestamp.tst";
        (new AsicContainerCreator.BytesEntryCallback(new ZipEntry(signatureFileName), dataFile.getBytes())).write();
    }

    public void writeExistingEntries(Collection<AsicEntry> asicEntries) {
        logger.debug("Writing existing zip container entries");

        DSSDocument content;
        ZipEntry zipEntry;
        for(Iterator i$ = asicEntries.iterator(); i$.hasNext(); (new AsicContainerCreator.StreamEntryCallback(zipEntry, content.openStream(), false)).write()) {
            AsicEntry asicEntry = (AsicEntry)i$.next();
            content = asicEntry.getContent();
            zipEntry = asicEntry.getZipEntry();
            if (!StringUtils.equalsIgnoreCase("mimetype", zipEntry.getName())) {
                this.zipOutputStream.setLevel(8);
            }
        }

    }

    public void writeContainerComment(String comment) {
        logger.debug("Writing container comment: " + comment);
        this.zipOutputStream.setComment(comment);
    }

    public void setZipComment(String zipComment) {
        this.zipComment = zipComment;
    }

    private static ZipEntry getAsicMimeTypeZipEntry(byte[] mimeTypeBytes) {
        ZipEntry entryMimetype = new ZipEntry("mimetype");
        entryMimetype.setMethod(0);
        entryMimetype.setSize((long)mimeTypeBytes.length);
        entryMimetype.setCompressedSize((long)mimeTypeBytes.length);
        CRC32 crc = new CRC32();
        crc.update(mimeTypeBytes);
        entryMimetype.setCrc(crc.getValue());
        return entryMimetype;
    }

    private static void handleIOException(String message, IOException e) {
        logger.error(message + ": " + e.getMessage());
        throw new TechnicalException(message, e);
    }

    static {
        CHARSET = StandardCharsets.UTF_8;
    }

    private abstract class EntryCallback {
        private final ZipEntry entry;
        private final boolean addComment;

        EntryCallback(ZipEntry entry) {
            this(entry, true);
        }

        EntryCallback(ZipEntry entry, boolean addComment) {
            this.entry = entry;
            this.addComment = addComment;
        }

        void write() {
            if (this.addComment) {
                this.entry.setComment(AsicContainerCreator.this.zipComment);
            }

            try {
                AsicContainerCreator.this.zipOutputStream.putNextEntry(this.entry);
                this.doWithEntryStream(AsicContainerCreator.this.zipOutputStream);
                AsicContainerCreator.this.zipOutputStream.closeEntry();
            } catch (IOException var2) {
                AsicContainerCreator.handleIOException("Unable to write Zip entry to asic container", var2);
            }

        }

        abstract void doWithEntryStream(OutputStream var1) throws IOException;
    }

    private class BytesEntryCallback extends AsicContainerCreator.EntryCallback {
        private final byte[] data;

        BytesEntryCallback(ZipEntry entry, byte[] data) {
            super(entry);
            this.data = data;
        }

        void doWithEntryStream(OutputStream stream) throws IOException {
            stream.write(this.data);
        }
    }

    private class StreamEntryCallback extends AsicContainerCreator.EntryCallback {
        private final InputStream inputStream;

        StreamEntryCallback(ZipEntry entry, InputStream inputStream) {
            this(entry, inputStream, true);
        }

        StreamEntryCallback(ZipEntry entry, InputStream inputStream, boolean addComment) {
            super(entry, addComment);
            this.inputStream = inputStream;
        }

        void doWithEntryStream(OutputStream stream) throws IOException {
            IOUtils.copy(this.inputStream, stream);
        }
    }
}
