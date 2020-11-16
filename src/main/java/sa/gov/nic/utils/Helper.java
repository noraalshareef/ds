// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.utils;

import org.slf4j.LoggerFactory;
import sa.gov.nic.exceptions.TechnicalException;
import eu.europa.esig.dss.InMemoryDocument;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import org.apache.commons.io.FilenameUtils;
import java.io.FilenameFilter;
import eu.europa.esig.dss.DSSUtils;
import sa.gov.nic.ContainerBuilder;
import sa.gov.nic.DataFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.MimeType;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.Version;
import sa.gov.nic.Container;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.io.IOUtils;
import java.util.zip.ZipFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import org.slf4j.Logger;

public final class Helper
{
    private static final Logger logger;
    private static final int ZIP_VERIFICATION_CODE = 1347093252;
    private static final int INT_LENGTH = 4;
    private static final String ASIC_E_TM_SIGNATURE_LEVEL = "ASiC_E_BASELINE_LT_TM";
    private static final String ASIC_S_TM_SIGNATURE_LEVEL = "ASiC_S_BASELINE_LT_TM";
    private static final String EMPTY_CONTAINER_SIGNATURE_LEVEL_ASIC_E = "ASiC_E";
    private static final String EMPTY_CONTAINER_SIGNATURE_LEVEL_ASIC_S = "ASiC_S";
    public static final String SPECIAL_CHARACTERS = "[\\\\<>:\"/|?*]";
    private static final char[] hexArray;
    
    private Helper() {
    }
    
    public static boolean isZipFile(final InputStream stream) throws IOException {
        final DataInputStream in = new DataInputStream(stream);
        if (stream.markSupported()) {
            stream.mark(4);
        }
        final int test = in.readInt();
        if (stream.markSupported()) {
            stream.reset();
        }
        final int zipVerificationCode = 1347093252;
        return test == 1347093252;
    }
    
    public static boolean isZipFile(final File file) throws IOException {
        try (final FileInputStream stream = new FileInputStream(file)) {
            return isZipFile(stream);
        }
    }
    
    public static boolean isXMLFile(final File file) throws ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try {
            builder.parse(file);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public static void deleteFile(final String file) throws IOException {
        Files.deleteIfExists(Paths.get(file, new String[0]));
    }
    
    public static String extractSignature(final String file, final int index) throws IOException {
        final ZipFile zipFile = new ZipFile(file);
        final String signatureFileName = "META-INF/signatures" + index + ".xml";
        final ZipEntry entry = zipFile.getEntry(signatureFileName);
        if (entry == null) {
            throw new IOException(signatureFileName + " does not exists in archive: " + file);
        }
        final InputStream inputStream = zipFile.getInputStream(entry);
        final String signatureContent = IOUtils.toString(inputStream, "UTF-8");
        zipFile.close();
        inputStream.close();
        return signatureContent;
    }
    
    public static <T> void serialize(final T object, final File file) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.flush();
        }
        catch (Exception e) {
            throw new DigiDoc4JException(e);
        }
        finally {
            IOUtils.closeQuietly((OutputStream)out);
            IOUtils.closeQuietly((OutputStream)fileOut);
        }
    }
    
    public static <T> void serialize(final T object, final String filename) {
        serialize(object, new File(filename));
    }
    
    public static <T> T deserializer(final File file) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = new FileInputStream(file);
            in = new ObjectInputStream(fileIn);
            final T object = (T)in.readObject();
            return object;
        }
        catch (Exception e) {
            throw new DigiDoc4JException(e);
        }
        finally {
            IOUtils.closeQuietly((InputStream)in);
            IOUtils.closeQuietly((InputStream)fileIn);
        }
    }
    
    public static <T> T deserializer(final String filename) {
        return deserializer(new File(filename));
    }
    
    public static OutputStream bufferedOutputStream(final File file) {
        try {
            return new BufferedOutputStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new DigiDoc4JException(e);
        }
    }
    
    public static String createUserAgent(final Container container) {
        final String documentType = container.getDocumentType().toString();
        final String version = container.getVersion();
        final String signatureProfile = container.getSignatureProfile();
        return createUserAgent(documentType, version, signatureProfile);
    }
    
    public static String createUserAgent(final String documentType, final String version, final String signatureProfile) {
        final StringBuilder ua = new StringBuilder("LIB DigiDoc4j/").append((Version.VERSION == null) ? "DEV" : Version.VERSION);
        ua.append(" format: ").append(documentType);
        if (version != null) {
            ua.append("/").append(version);
        }
        if (signatureProfile != null) {
            ua.append(" signatureProfile: ").append(signatureProfile);
        }
        ua.append(" Java: ").append(System.getProperty("java.version"));
        ua.append("/").append(System.getProperty("java.vendor"));
        ua.append(" OS: ").append(System.getProperty("os.name"));
        ua.append("/").append(System.getProperty("os.arch"));
        ua.append("/").append(System.getProperty("os.version"));
        ua.append(" JVM: ").append(System.getProperty("java.vm.name"));
        ua.append("/").append(System.getProperty("java.vm.vendor"));
        ua.append("/").append(System.getProperty("java.vm.version"));
        final String userAgent = ua.toString();
        Helper.logger.debug("User-Agent: " + userAgent);
        return userAgent;
    }
    
    public static String createBDocAsicSUserAgent(final SignatureProfile signatureProfile) {
        if (signatureProfile == SignatureProfile.LT_TM) {
            return createUserAgent(MimeType.ASICS.getMimeTypeString(), null, "ASiC_S_BASELINE_LT_TM");
        }
        final SignatureLevel signatureLevel = determineSignatureLevel(signatureProfile);
        return createBDocUserAgent(signatureLevel);
    }
    
    public static String createBDocAsicSUserAgent() {
        return createUserAgent(MimeType.ASICS.getMimeTypeString(), null, "ASiC_S");
    }
    
    public static String createBDocUserAgent() {
        return createUserAgent(MimeType.ASICE.getMimeTypeString(), null, "ASiC_E");
    }
    
    public static String createBDocUserAgent(final SignatureProfile signatureProfile) {
        if (signatureProfile == SignatureProfile.LT_TM) {
            return createUserAgent(MimeType.ASICE.getMimeTypeString(), null, "ASiC_E_BASELINE_LT_TM");
        }
        final SignatureLevel signatureLevel = determineSignatureLevel(signatureProfile);
        return createBDocUserAgent(signatureLevel);
    }
    
    private static String createBDocUserAgent(final SignatureLevel signatureLevel) {
        return createUserAgent(MimeType.ASICE.getMimeTypeString(), null, signatureLevel.name());
    }
    
    private static SignatureLevel determineSignatureLevel(final SignatureProfile signatureProfile) {
        if (signatureProfile == SignatureProfile.B_BES) {
            return SignatureLevel.XAdES_BASELINE_B;
        }
        if (signatureProfile == SignatureProfile.LTA) {
            return SignatureLevel.XAdES_BASELINE_LTA;
        }
        return SignatureLevel.XAdES_BASELINE_LT;
    }
    
    public static boolean hasSpecialCharacters(final String fileName) {
        final Pattern special = Pattern.compile("[\\\\<>:\"/|?*]");
        final Matcher hasSpecial = special.matcher(fileName);
        return hasSpecial.find();
    }
    
    public static String getIdentifier(final String identifier) {
        String id = identifier.trim();
        if (DSSXMLUtils.isOid(id)) {
            id = id.substring(id.lastIndexOf(58) + 1);
            return id;
        }
        return id;
    }
    
    public static SignaturePolicyProvider getBdocSignaturePolicyProvider(final DSSDocument signature) {
        final SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
        final Map<String, DSSDocument> signaturePoliciesById = new HashMap<String, DSSDocument>();
        signaturePoliciesById.put("1.3.6.1.4.1.10015.1000.3.2.1", signature);
        final Map<String, DSSDocument> signaturePoliciesByUrl = new HashMap<String, DSSDocument>();
        signaturePoliciesByUrl.put("https://www.sk.ee/repository/bdoc-spec21.pdf", signature);
        signaturePolicyProvider.setSignaturePoliciesById((Map)signaturePoliciesById);
        signaturePolicyProvider.setSignaturePoliciesByUrl((Map)signaturePoliciesByUrl);
        return signaturePolicyProvider;
    }
    
    public static List<byte[]> getAllFilesFromContainerAsBytes(final Container container) {
        final List<byte[]> files = new ArrayList<byte[]>();
        for (final DataFile dataFile : container.getDataFiles()) {
            files.add(dataFile.getBytes());
        }
        return files;
    }
    
    public static List<byte[]> getAllFilesFromContainerPathAsBytes(final String pathFrom) {
        final Container container = ContainerBuilder.aContainer().fromExistingFile(pathFrom).build();
        final List<byte[]> files = new ArrayList<byte[]>();
        for (final DataFile dataFile : container.getDataFiles()) {
            files.add(dataFile.getBytes());
        }
        return files;
    }
    
    public static void saveAllFilesFromContainerToFolder(final Container container, final String path) {
        for (final DataFile dataFile : container.getDataFiles()) {
            final File file = new File(path + File.separator + dataFile.getName());
            DSSUtils.saveToFile(dataFile.getBytes(), file);
        }
    }
    
    public static void saveAllFilesFromContainerPathToFolder(final String pathFrom, final String pathTo) {
        final Container container = ContainerBuilder.aContainer().fromExistingFile(pathFrom).build();
        for (final DataFile dataFile : container.getDataFiles()) {
            final File file = new File(pathTo + File.separator + dataFile.getName());
            DSSUtils.saveToFile(dataFile.getBytes(), file);
        }
    }
    
    public static void deleteTmpFiles() {
        final File dir = new File(System.getProperty("java.io.tmpdir"));
        final FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().startsWith("digidoc4j") && name.toLowerCase().endsWith(".tmp");
            }
        };
        for (final File f : dir.listFiles(filenameFilter)) {
            if (!f.delete()) {
                f.deleteOnExit();
                System.gc();
            }
        }
    }
    
    public static boolean isAsicEContainer(final String path) {
        final String extension = FilenameUtils.getExtension(path);
        if ("sce".equals(extension) || "asice".equals(extension)) {
            return true;
        }
        if ("zip".equals(extension)) {
            try {
                return parseAsicContainer(new BufferedInputStream(new FileInputStream(path)), MimeType.ASICE);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }
    
    public static boolean isAsicEContainer(final BufferedInputStream stream) {
        boolean isAsic = false;
        try {
            isAsic = parseAsicContainer(stream, MimeType.ASICE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return isAsic;
    }
    
    public static boolean isAsicSContainer(final String path) {
        final String extension = FilenameUtils.getExtension(path);
        if ("scs".equals(extension) || "asics".equals(extension)) {
            return true;
        }
        if ("zip".equals(extension)) {
            try {
                return parseAsicContainer(new BufferedInputStream(new FileInputStream(path)), MimeType.ASICS);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }
    
    public static boolean isAsicSContainer(final BufferedInputStream stream) {
        boolean isAsic = false;
        try {
            isAsic = parseAsicContainer(stream, MimeType.ASICS);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return isAsic;
    }
    
    private static boolean parseAsicContainer(final BufferedInputStream stream, final MimeType mtype) throws IOException {
        stream.mark(stream.available() + 1);
        final ZipInputStream zipInputStream = new ZipInputStream(stream);
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (StringUtils.equalsIgnoreCase((CharSequence)"mimetype", (CharSequence)entry.getName())) {
                    final InputStream zipFileInputStream = zipInputStream;
                    final BOMInputStream bomInputStream = new BOMInputStream(zipFileInputStream);
                    final DSSDocument document = (DSSDocument)new InMemoryDocument((InputStream)bomInputStream);
                    final String mimeType = StringUtils.trim(IOUtils.toString(IOUtils.toByteArray(document.openStream()), "UTF-8"));
                    if (StringUtils.equalsIgnoreCase((CharSequence)mimeType, (CharSequence)mtype.getMimeTypeString())) {
                        return true;
                    }
                    continue;
                }
            }
        }
        catch (IOException e) {
            Helper.logger.error("Error reading asic container stream: " + e.getMessage());
            throw new TechnicalException("Error reading asic container stream: ", e);
        }
        finally {
            stream.reset();
        }
        return false;
    }
    
    public static boolean isPdfFile(final String file) {
        return FilenameUtils.getExtension(file).equals("pdf");
    }
    
    public static String bytesToHex(final byte[] bytes, final int maxLen) {
        final char[] hexChars = new char[Math.min(bytes.length, maxLen) * 2];
        for (int j = 0; j < Math.min(bytes.length, maxLen); ++j) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = Helper.hexArray[v >>> 4];
            hexChars[j * 2 + 1] = Helper.hexArray[v & 0xF];
        }
        return new String(hexChars);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)Helper.class);
        hexArray = "0123456789ABCDEF".toCharArray();
    }
}
