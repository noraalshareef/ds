// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.signers;

import java.nio.file.Path;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bouncycastle.tsp.TimeStampToken;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.DSSASN1Utils;
import eu.europa.esig.dss.client.http.DataLoader;
import sa.gov.nic.impl.asic.SkDataLoader;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.DSSUtils;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.DataFile;
import sa.gov.nic.Configuration;
import sa.gov.nic.ContainerBuilder;
import java.util.List;
import eu.europa.esig.dss.DigestAlgorithm;

public final class TimestampToken
{
    private TimestampToken() {
    }
    
    public static DataFile generateTimestampToken(final DigestAlgorithm digestAlgorithm, final List<ContainerBuilder.ContainerDataFile> dataFiles, final Configuration configuration) {
        if (dataFiles.isEmpty()) {
            throw new DigiDoc4JException("Add data file first");
        }
        if (dataFiles.size() > 1) {
            throw new DigiDoc4JException("Supports only asics with only one datafile");
        }
        final ContainerBuilder.ContainerDataFile containerDataFile = dataFiles.get(0);
        final OnlineTSPSource onlineTSPSource = defineOnlineTSPSource(configuration);
        final byte[] dataFileDigest = getDigest(containerDataFile);
        final byte[] digest = DSSUtils.digest(digestAlgorithm, dataFileDigest);
        final DataFile timeStampToken = getTimestampToken(onlineTSPSource, digestAlgorithm, digest);
        return timeStampToken;
    }
    
    public static DataFile generateTimestampToken(final DigestAlgorithm digestAlgorithm, final DataFile containerDataFile) {
        final OnlineTSPSource onlineTSPSource = defineOnlineTSPSource(null);
        final byte[] dataFileDigest = containerDataFile.getBytes();
        final byte[] digest = DSSUtils.digest(digestAlgorithm, dataFileDigest);
        final DataFile timeStampToken = getTimestampToken(onlineTSPSource, digestAlgorithm, digest);
        return timeStampToken;
    }
    
    private static OnlineTSPSource defineOnlineTSPSource(Configuration configuration) {
        final OnlineTSPSource onlineTSPSource = new OnlineTSPSource();
        if (configuration == null) {
            configuration = Configuration.getInstance();
        }
        onlineTSPSource.setTspServer(configuration.getTspSource());
        final SkDataLoader dataLoader = SkDataLoader.createTimestampDataLoader(configuration);
        dataLoader.setAsicSUserAgentSignatureProfile();
        onlineTSPSource.setDataLoader((DataLoader)dataLoader);
        return onlineTSPSource;
    }
    
    private static DataFile getTimestampToken(final OnlineTSPSource onlineTSPSource, final DigestAlgorithm digestAlgorithm, final byte[] digest) {
        final DataFile timeStampToken = new DataFile();
        final TimeStampToken timeStampResponse = onlineTSPSource.getTimeStampResponse(digestAlgorithm, digest);
        final String timestampFilename = "timestamp";
        timeStampToken.setDocument((DSSDocument)new InMemoryDocument(DSSASN1Utils.getEncoded(timeStampResponse), timestampFilename, MimeType.TST));
        timeStampToken.setMediaType(MimeType.TST.getMimeTypeString());
        return timeStampToken;
    }
    
    private static byte[] getDigest(final ContainerBuilder.ContainerDataFile dataFile) {
        try {
            byte[] dataFileDigest;
            if (!dataFile.isStream) {
                final Path path = Paths.get(dataFile.filePath, new String[0]);
                dataFileDigest = Files.readAllBytes(path);
            }
            else {
                dataFileDigest = IOUtils.toByteArray(dataFile.inputStream);
            }
            return dataFileDigest;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new DigiDoc4JException("Cannot get file digest");
        }
    }
}
