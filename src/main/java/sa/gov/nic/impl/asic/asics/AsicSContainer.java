// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asics;

import org.slf4j.LoggerFactory;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.utils.Helper;
import sa.gov.nic.DataFile;
import sa.gov.nic.impl.asic.asice.AsicESignature;

import java.util.ArrayList;
import sa.gov.nic.impl.asic.asice.AsicESignatureOpener;
import sa.gov.nic.Signature;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import sa.gov.nic.impl.asic.AsicContainerCreator;
import java.io.OutputStream;
import java.io.InputStream;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import sa.gov.nic.impl.asic.AsicContainer;

public class AsicSContainer extends AsicContainer
{
    private static final Logger logger;
    
    public AsicSContainer() {
        this.setType("ASICS");
    }
    
    public AsicSContainer(final Configuration configuration) {
        super(configuration);
        this.setType("ASICS");
    }
    
    public AsicSContainer(final String containerPath) {
        super(containerPath, "ASICS");
    }
    
    public AsicSContainer(final String containerPath, final Configuration configuration) {
        super(containerPath, configuration, "ASICS");
    }
    
    public AsicSContainer(final InputStream stream) {
        super(stream, "ASICS");
    }
    
    public AsicSContainer(final InputStream stream, final Configuration configuration) {
        super(stream, configuration, "ASICS");
    }
    
    @Override
    public void save(final OutputStream out) {
        this.writeAsicContainer(new AsicContainerCreator(out));
    }
    
    @Override
    protected List<Signature> parseSignatureFiles(final List<DSSDocument> signatureFiles, final List<DSSDocument> detachedContents) {
        final Configuration configuration = this.getConfiguration();
        final AsicESignatureOpener signatureOpener = new AsicESignatureOpener(detachedContents, configuration);
        final List<Signature> signatures = new ArrayList<Signature>(signatureFiles.size());
        for (final DSSDocument signatureFile : signatureFiles) {
            final List<AsicESignature> asicSignatures = signatureOpener.parse(signatureFile);
            signatures.addAll(asicSignatures);
        }
        return signatures;
    }
    
    public void replaceDataFile(final DataFile dataFile) {
        if (this.getDataFiles().size() > 0) {
            this.removeDataFile(this.getDataFiles().get(0));
        }
        this.addDataFile(dataFile);
    }
    
    @Override
    protected String createUserAgent() {
        if (!this.getSignatures().isEmpty()) {
            final SignatureProfile profile = this.getSignatures().get(0).getProfile();
            return Helper.createBDocAsicSUserAgent(profile);
        }
        return Helper.createBDocAsicSUserAgent();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicSContainer.class);
    }
}
