//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic.impl.ddoc;

import ee.sk.digidoc.DigiDocException;
import ee.sk.digidoc.KeyInfo;
import ee.sk.digidoc.Signature;
import ee.sk.digidoc.SignedDoc;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import sa.gov.nic.Configuration;
import sa.gov.nic.DataFile;
import sa.gov.nic.DigestAlgorithm;
import sa.gov.nic.SignatureParameters;
import sa.gov.nic.SignatureProductionPlace;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.SignatureToken;
import sa.gov.nic.SignedInfo;
import sa.gov.nic.ValidationResult;
import sa.gov.nic.X509Cert;
import sa.gov.nic.Container.DocumentType;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.impl.SignatureFinalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDocFacade implements SignatureFinalizer, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DDocFacade.class);
    protected SignedDoc ddoc;
    private ArrayList<DigiDocException> openContainerExceptions = new ArrayList();
    private SignatureProfile signatureProfile;
    private SignatureParameters signatureParameters;
    protected Signature ddocSignature;
    private Configuration configuration;
    static ConfigManagerInitializer configManagerInitializer = new ConfigManagerInitializer();

    public DDocFacade() {
        this.signatureProfile = SignatureProfile.LT_TM;
        this.signatureParameters = new SignatureParameters();
        logger.debug("");
        this.intConfiguration();
        this.createDDOCContainer();
    }

    public DDocFacade(Configuration configuration) {
        this.signatureProfile = SignatureProfile.LT_TM;
        this.signatureParameters = new SignatureParameters();
        logger.debug("");
        this.configuration = configuration;
        this.initConfigManager(configuration);
        this.createDDOCContainer();
    }

    DDocFacade(SignedDoc ddoc) {
        this.signatureProfile = SignatureProfile.LT_TM;
        this.signatureParameters = new SignatureParameters();
        logger.debug("");
        this.intConfiguration();
        this.ddoc = ddoc;
    }

    public SignedInfo prepareSigning(X509Certificate signerCert) {
        logger.info("Preparing signing");
        List<String> signerRoles = this.signatureParameters.getRoles();
        SignatureProductionPlace signatureProductionPlace = this.signatureParameters.getProductionPlace();
        ee.sk.digidoc.SignatureProductionPlace productionPlace = new ee.sk.digidoc.SignatureProductionPlace(signatureProductionPlace.getCity(), signatureProductionPlace.getStateOrProvince(), signatureProductionPlace.getCountry(), signatureProductionPlace.getPostalCode());
        if (this.signatureParameters.getDigestAlgorithm() == null) {
            this.signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA1);
        }

        try {
            this.ddocSignature = this.ddoc.prepareSignature(signerCert, (String[])signerRoles.toArray(new String[signerRoles.size()]), productionPlace);
            String signatureId = this.signatureParameters.getSignatureId();
            if (signatureId != null) {
                this.ddocSignature.setId(signatureId);
            }

            return new SignedInfo(this.ddocSignature.calculateSignedInfoXML(), this.signatureParameters);
        } catch (DigiDocException var6) {
            logger.error(var6.getMessage());
            throw new DigiDoc4JException(var6);
        }
    }

    public String getSignatureProfile() {
        String name = this.signatureProfile.name();
        logger.debug("Signature profile: " + name);
        return name;
    }

    public void setSignatureParameters(SignatureParameters signatureParameters) {
        logger.debug("");
        DigestAlgorithm algorithm = signatureParameters.getDigestAlgorithm();
        if (algorithm == null) {
            signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA1);
        } else if (algorithm != DigestAlgorithm.SHA1) {
            NotSupportedException exception = new NotSupportedException("DDOC 1.3 supports only SHA1 as digest algorithm. Specified algorithm is " + algorithm);
            logger.error(exception.toString());
            throw exception;
        }

        this.addSignatureProfile(signatureParameters);
        this.signatureParameters = signatureParameters.copy();
    }

    private void addSignatureProfile(SignatureParameters signatureParameters) {
        if (signatureParameters.getSignatureProfile() != null) {
            this.setSignatureProfile(signatureParameters.getSignatureProfile());
        }

    }

    public DigestAlgorithm getDigestAlgorithm() {
        DigestAlgorithm digestAlgorithm = this.signatureParameters.getDigestAlgorithm();
        logger.debug("Digest algorithm: " + digestAlgorithm);
        return digestAlgorithm;
    }

    private void intConfiguration() {
        logger.debug("");
        this.configuration = Configuration.getInstance();
        this.initConfigManager(this.configuration);
    }

    private void createDDOCContainer() {
        logger.debug("");

        try {
            this.ddoc = new SignedDoc("DIGIDOC-XML", "1.3");
            this.signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA1);
            logger.info("DDoc container created");
        } catch (DigiDocException var2) {
            logger.error(var2.getMessage());
            throw new DigiDoc4JException(var2.getNestedException());
        }
    }

    public DataFile addDataFile(String path, String mimeType) {
        logger.info("Adding data file: " + path + ", mime type " + mimeType);

        try {
            this.ddoc.addDataFile(new File(path), mimeType, "EMBEDDED_BASE64");
            return new DataFile(path, mimeType);
        } catch (DigiDocException var4) {
            logger.error(var4.getMessage());
            throw new DigiDoc4JException(var4.getNestedException());
        }
    }

    public DataFile addDataFile(InputStream is, String fileName, String mimeType) {
        logger.info("Adding data file: " + fileName + ", mime type: " + mimeType);

        try {
            ee.sk.digidoc.DataFile dataFile = new ee.sk.digidoc.DataFile(this.ddoc.getNewDataFileId(), "EMBEDDED_BASE64", fileName, mimeType, this.ddoc);
            byte[] data = IOUtils.toByteArray(is);
            dataFile.setBody(data);
            this.ddoc.addDataFile(dataFile);
            return new DataFile(is, fileName, mimeType);
        } catch (IOException | DigiDocException var6) {
            logger.error(var6.getMessage());
            throw new DigiDoc4JException(var6);
        }
    }

    public void addDataFile(DataFile dataFile) {
        this.addDataFile(dataFile.getStream(), dataFile.getName(), dataFile.getMediaType());
    }

    public void addRawSignature(byte[] signatureBytes) {
        logger.debug("");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(signatureBytes);
        this.addRawSignature((InputStream)byteArrayInputStream);
        IOUtils.closeQuietly(byteArrayInputStream);
    }

    public void addRawSignature(InputStream signatureStream) {
        logger.info("Adding raw XAdES signature");

        try {
            this.ddoc.readSignature(signatureStream);
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }
    }

    public List<DataFile> getDataFiles() {
        logger.debug("");
        List<DataFile> dataFiles = new ArrayList();
        ArrayList ddocDataFiles = this.ddoc.getDataFiles();
        if (ddocDataFiles == null) {
            return dataFiles;
        } else {
            Iterator i$ = ddocDataFiles.iterator();

            while(i$.hasNext()) {
                Object ddocDataFile = i$.next();
                ee.sk.digidoc.DataFile dataFile = (ee.sk.digidoc.DataFile)ddocDataFile;

                try {
                    DataFile dataFile1;
                    if (dataFile.getBody() == null) {
                        dataFile1 = new DataFile(dataFile.getFileName(), dataFile.getMimeType());
                        dataFile1.setId(dataFile.getId());
                        dataFiles.add(dataFile1);
                    } else {
                        dataFile1 = new DataFile(dataFile.getBodyAsData(), dataFile.getFileName(), dataFile.getMimeType());
                        dataFile1.setId(dataFile.getId());
                        dataFiles.add(dataFile1);
                    }
                } catch (DigiDocException var7) {
                    logger.error(var7.getMessage());
                    throw new DigiDoc4JException(var7.getNestedException());
                }
            }

            return dataFiles;
        }
    }

    /** @deprecated */
    @Deprecated
    public DataFile getDataFile(int index) {
        logger.debug("Get data file for index " + index);
        return (DataFile)this.getDataFiles().get(index);
    }

    public int countDataFiles() {
        logger.debug("Get the number of data files");
        List<DataFile> dataFiles = this.getDataFiles();
        return dataFiles == null ? 0 : dataFiles.size();
    }

    public void removeDataFile(String fileName) {
        logger.debug("File name: " + fileName);
        this.removeDataFile(new File(fileName));
    }

    private void removeDataFile(File file) {
        logger.info("Removing data file: " + file.getName());
        int index = -1;
        ArrayList ddocDataFiles = this.ddoc.getDataFiles();

        for(int i = 0; i < ddocDataFiles.size(); ++i) {
            ee.sk.digidoc.DataFile dataFile = (ee.sk.digidoc.DataFile)ddocDataFiles.get(i);
            if (dataFile.getFileName().equalsIgnoreCase(file.getName())) {
                index = i;
            }
        }

        if (index == -1) {
            DigiDoc4JException exception = new DigiDoc4JException("File not found");
            logger.error(exception.toString());
            throw exception;
        } else {
            try {
                this.ddoc.removeDataFile(index);
            } catch (DigiDocException var6) {
                logger.error(var6.getMessage());
                throw new DigiDoc4JException(var6.getNestedException());
            }
        }
    }

    /** @deprecated */
    public void removeSignature(int index) {
        logger.info("Removing signature index: " + index);

        try {
            this.ddoc.removeSignature(index);
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }
    }

    public void save(String path) {
        logger.info("Saving container to path: " + path);

        try {
            this.ddoc.writeToFile(new File(path));
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }
    }

    public void save(OutputStream out) {
        logger.info("Saving container to stream");

        try {
            this.ddoc.writeToStream(out);
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }
    }

    public sa.gov.nic.Signature sign(SignatureToken signer) {
        logger.info("Signing DDoc container");
        this.calculateSignature(signer);

        try {
            this.signRaw(signer.sign(this.getDigestAlgorithm(), this.ddocSignature.calculateSignedInfoXML()));
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }

        return new DDocSignature(this.ddocSignature);
    }

    public sa.gov.nic.Signature signRaw(byte[] rawSignature) {
        logger.info("Finalizing DDoc signature");

        try {
            this.ddocSignature.setSignatureValue(rawSignature);
            DDocSignature signature = new DDocSignature(this.ddocSignature);
            if (this.signatureProfile == SignatureProfile.LT_TM) {
                this.ddocSignature.getConfirmation();
            }

            signature.setIndexInArray(this.getSignatureIndexInArray());
            logger.info("Signing DDoc successfully completed");
            return signature;
        } catch (DigiDocException var3) {
            logger.error(var3.getMessage());
            throw new DigiDoc4JException(var3.getNestedException());
        }
    }

    private int getSignatureIndexInArray() {
        return this.ddoc.getSignatures().size() - 1;
    }

    public List<sa.gov.nic.Signature> getSignatures() {
        logger.debug("");
        List<sa.gov.nic.Signature> signatures = new ArrayList();
        ArrayList dDocSignatures = this.ddoc.getSignatures();
        if (dDocSignatures == null) {
            return signatures;
        } else {
            int signatureIndexInArray = 0;
            Iterator i$ = dDocSignatures.iterator();

            while(i$.hasNext()) {
                Object signature = i$.next();
                DDocSignature finalSignature = this.mapJDigiDocSignatureToDigiDoc4J((Signature)signature);
                if (finalSignature != null) {
                    finalSignature.setIndexInArray(signatureIndexInArray);
                    signatures.add(finalSignature);
                    ++signatureIndexInArray;
                }
            }

            return signatures;
        }
    }

    /** @deprecated */
    public sa.gov.nic.Signature getSignature(int index) {
        logger.debug("Get signature for index " + index);
        return (sa.gov.nic.Signature)this.getSignatures().get(index);
    }

    public int countSignatures() {
        logger.debug("Get the number of signatures");
        List<sa.gov.nic.Signature> signatures = this.getSignatures();
        return signatures == null ? 0 : signatures.size();
    }

    private DDocSignature mapJDigiDocSignatureToDigiDoc4J(Signature signature) {
        logger.debug("");
        DDocSignature finalSignature = new DDocSignature(signature);
        KeyInfo keyInfo = signature.getKeyInfo();
        if (keyInfo == null) {
            return null;
        } else {
            X509Certificate signersCertificate = keyInfo.getSignersCertificate();
            finalSignature.setCertificate(new X509Cert(signersCertificate));
            return finalSignature;
        }
    }

    public DocumentType getDocumentType() {
        logger.debug("");
        return DocumentType.DDOC;
    }

    public ValidationResult validate() {
        logger.info("Validating DDoc container");
        ArrayList exceptions = this.ddoc.verify(true, true);
        ArrayList containerExceptions = this.ddoc.validate(true);
        containerExceptions.addAll(this.openContainerExceptions);
        ValidationResultForDDoc result = new ValidationResultForDDoc(exceptions, containerExceptions);
        logger.info("DDoc container is valid: " + result.isValid());
        return result;
    }

    protected Signature calculateSignature(SignatureToken signer) {
        logger.debug("");
        this.prepareSigning(signer.getCertificate());
        return this.ddocSignature;
    }

    private void addConfirmation() {
        logger.debug("");
        Iterator i$ = this.ddoc.getSignatures().iterator();

        while(i$.hasNext()) {
            Object signature = i$.next();

            try {
                ((Signature)signature).getConfirmation();
            } catch (DigiDocException var4) {
                logger.error(var4.getMessage());
                throw new DigiDoc4JException(var4.getNestedException());
            }
        }

    }

    public String getVersion() {
        String version = this.ddoc.getVersion();
        logger.debug("Version: " + version);
        return version;
    }

    public void extendTo(SignatureProfile profile) {
        logger.info("Extending signature profile to " + profile.name());
        if (profile != SignatureProfile.LT_TM) {
            String errorMessage = profile + " profile is not supported for DDOC extension";
            logger.error(errorMessage);
            throw new NotSupportedException(errorMessage);
        } else {
            this.addConfirmation();
        }
    }

    public void setSignatureProfile(SignatureProfile profile) {
        logger.debug("Adding signature profile " + profile);
        if (profile != SignatureProfile.LT_TM && profile != SignatureProfile.B_BES) {
            String errorMessage = profile + " profile is not supported for DDOC";
            logger.error(errorMessage);
            throw new NotSupportedException(errorMessage);
        } else {
            this.signatureProfile = profile;
        }
    }

    public String getFormat() {
        String format = this.ddoc.getFormat();
        logger.debug(format);
        return format;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public sa.gov.nic.Signature finalizeSignature(byte[] signatureValue) {
        return this.signRaw(signatureValue);
    }

    private void initConfigManager(Configuration configuration) {
        configManagerInitializer.initConfigManager(configuration);
    }

    protected void setSignedDoc(SignedDoc signedDoc) {
        this.ddoc = signedDoc;
    }

    protected void setContainerOpeningExceptions(ArrayList<DigiDocException> openContainerExceptions) {
        this.openContainerExceptions = openContainerExceptions;
    }
}
