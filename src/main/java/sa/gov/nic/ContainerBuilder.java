// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import sa.gov.nic.utils.Helper;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.signers.TimestampToken;
import eu.europa.esig.dss.DigestAlgorithm;
import java.io.File;
import sa.gov.nic.exceptions.InvalidDataFileException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.impl.pades.PadesContainerBuilder;
import sa.gov.nic.impl.asic.asice.AsicEContainerBuilder;
import sa.gov.nic.impl.asic.asics.AsicSContainerBuilder;
import sa.gov.nic.impl.ddoc.DDocContainerBuilder;
import sa.gov.nic.impl.asic.asice.bdoc.BDocContainerBuilder;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.impl.CustomContainerBuilder;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public abstract class ContainerBuilder
{
    private static final Logger logger;
    protected static Map<String, Class<? extends Container>> containerImplementations;
    protected Configuration configuration;
    protected List<ContainerDataFile> dataFiles;
    protected String containerFilePath;
    protected InputStream containerInputStream;
    protected static String containerType;
    private DataFile timeStampToken;
    
    public ContainerBuilder() {
        this.dataFiles = new ArrayList<ContainerDataFile>();
    }
    
    public static ContainerBuilder aContainer() {
        return aContainer(Container.DocumentType.BDOC);
    }
    
    public static ContainerBuilder aContainer(final String type) {
        ContainerBuilder.containerType = type;
        if (isCustomContainerType(type)) {
            return new CustomContainerBuilder(type);
        }
        try {
            return aContainer(Container.DocumentType.valueOf(type));
        }
        catch (IllegalArgumentException e) {
            throw new NotSupportedException(String.format("Container type <%s> is unsupported", type));
        }
    }
    
    public static ContainerBuilder aContainer(final Container.DocumentType type) {
        ContainerBuilder.containerType = type.name();
        if (isCustomContainerType(ContainerBuilder.containerType)) {
            return new CustomContainerBuilder(ContainerBuilder.containerType);
        }
        switch (type) {
            case BDOC: {
                return new BDocContainerBuilder();
            }
            case DDOC: {
                return new DDocContainerBuilder();
            }
            case ASICS: {
                return new AsicSContainerBuilder();
            }
            case ASICE: {
                return new AsicEContainerBuilder();
            }
            case PADES: {
                return new PadesContainerBuilder();
            }
            default: {
                throw new NotSupportedException(String.format("Container type <%s> is unsupported", type));
            }
        }
    }
    
    public Container build() {
        if (this.shouldOpenContainerFromFile()) {
            return this.openContainerFromFile();
        }
        if (this.shouldOpenContainerFromStream()) {
            return this.openContainerFromStream();
        }
        final Container container = this.createNewContainer();
        this.addDataFilesToContainer(container);
        if (this.timeStampToken != null) {
            this.addTimeStampTokenToContainer(container);
        }
        return container;
    }
    
    public ContainerBuilder withConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
    
    public ContainerBuilder withDataFile(final String filePath, final String mimeType) throws InvalidDataFileException {
        if ("ASICS".equals(ContainerBuilder.containerType) && !this.dataFiles.isEmpty()) {
            throw new DigiDoc4JException("Cannot add second file in case of ASICS container");
        }
        this.dataFiles.add(new ContainerDataFile(filePath, mimeType));
        return this;
    }
    
    public ContainerBuilder withDataFile(final InputStream inputStream, final String fileName, final String mimeType) throws InvalidDataFileException {
        if ("ASICS".equals(ContainerBuilder.containerType) && !this.dataFiles.isEmpty()) {
            throw new DigiDoc4JException("Cannot add second file in case of ASICS container");
        }
        this.dataFiles.add(new ContainerDataFile(inputStream, fileName, mimeType));
        return this;
    }
    
    public ContainerBuilder withDataFile(final File file, final String mimeType) throws InvalidDataFileException {
        if ("ASICS".equals(ContainerBuilder.containerType) && !this.dataFiles.isEmpty()) {
            throw new DigiDoc4JException("Cannot add second file in case of ASICS container");
        }
        this.dataFiles.add(new ContainerDataFile(file.getPath(), mimeType));
        return this;
    }
    
    public ContainerBuilder withDataFile(final DataFile dataFile) {
        if ("ASICS".equals(ContainerBuilder.containerType) && !this.dataFiles.isEmpty()) {
            throw new DigiDoc4JException("Cannot add second file in case of ASICS container");
        }
        this.dataFiles.add(new ContainerDataFile(dataFile));
        return this;
    }
    
    public ContainerBuilder withTimeStampToken(final DigestAlgorithm digestAlgorithm) {
        this.timeStampToken = TimestampToken.generateTimestampToken(digestAlgorithm, this.dataFiles, this.configuration);
        return this;
    }
    
    public ContainerBuilder fromExistingFile(final String filePath) {
        this.containerFilePath = filePath;
        return this;
    }
    
    public ContainerBuilder fromStream(final InputStream containerInputStream) {
        this.containerInputStream = containerInputStream;
        return this;
    }
    
    public static <T extends Container> void setContainerImplementation(final String containerType, final Class<T> containerClass) {
        ContainerBuilder.logger.info("Using <{}> for container type <{}>", (Object)containerClass.getName(), (Object)containerType);
        ContainerBuilder.containerImplementations.put(containerType, containerClass);
    }
    
    public static void removeCustomContainerImplementations() {
        ContainerBuilder.logger.info("Removing custom container implementations");
        ContainerBuilder.containerImplementations.clear();
    }
    
    protected abstract Container createNewContainer();
    
    protected Container openContainerFromFile() {
        if (this.configuration == null) {
            return ContainerOpener.open(this.containerFilePath);
        }
        return ContainerOpener.open(this.containerFilePath, this.configuration);
    }
    
    protected Container openContainerFromStream() {
        if (this.configuration == null) {
            final boolean actAsBigFilesSupportEnabled = true;
            return ContainerOpener.open(this.containerInputStream, actAsBigFilesSupportEnabled);
        }
        return ContainerOpener.open(this.containerInputStream, this.configuration);
    }
    
    protected void addDataFilesToContainer(final Container container) {
        for (final ContainerDataFile file : this.dataFiles) {
            if (file.isStream) {
                container.addDataFile(file.inputStream, file.filePath, file.mimeType);
            }
            else if (file.isDataFile()) {
                container.addDataFile(file.dataFile);
            }
            else {
                container.addDataFile(file.filePath, file.mimeType);
            }
        }
    }
    
    private void addTimeStampTokenToContainer(final Container container) {
        container.setTimeStampToken(this.timeStampToken);
    }
    
    protected boolean shouldOpenContainerFromFile() {
        return StringUtils.isNotBlank((CharSequence)this.containerFilePath);
    }
    
    protected boolean shouldOpenContainerFromStream() {
        return this.containerInputStream != null;
    }
    
    public abstract ContainerBuilder usingTempDirectory(final String p0);
    
    private static boolean isCustomContainerType(final String containerType) {
        return ContainerBuilder.containerImplementations.containsKey(containerType);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ContainerBuilder.class);
        ContainerBuilder.containerImplementations = new HashMap<String, Class<? extends Container>>();
    }
    
    public class ContainerDataFile
    {
        public String filePath;
        String mimeType;
        public InputStream inputStream;
        DataFile dataFile;
        public boolean isStream;
        
        public ContainerDataFile(final String filePath, final String mimeType) {
            this.filePath = filePath;
            this.mimeType = mimeType;
            this.isStream = false;
            this.validateDataFile();
        }
        
        public ContainerDataFile(final InputStream inputStream, final String filePath, final String mimeType) {
            this.filePath = filePath;
            this.mimeType = mimeType;
            this.inputStream = inputStream;
            this.isStream = true;
            this.validateDataFile();
            this.validateFileName();
        }
        
        public ContainerDataFile(final DataFile dataFile) {
            this.dataFile = dataFile;
            this.isStream = false;
        }
        
        public boolean isDataFile() {
            return this.dataFile != null;
        }
        
        private void validateDataFile() {
            if (StringUtils.isBlank((CharSequence)this.filePath)) {
                throw new InvalidDataFileException("File name/path cannot be empty");
            }
            if (StringUtils.isBlank((CharSequence)this.mimeType)) {
                throw new InvalidDataFileException("Mime type cannot be empty");
            }
        }
        
        private void validateFileName() {
            if (Helper.hasSpecialCharacters(this.filePath)) {
                throw new InvalidDataFileException("File name " + this.filePath + " must not contain special characters like: " + "[\\\\<>:\"/|?*]");
            }
        }
    }
}
