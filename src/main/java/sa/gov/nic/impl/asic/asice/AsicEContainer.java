// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice;

import org.slf4j.LoggerFactory;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.utils.Helper;

import java.util.ArrayList;
import sa.gov.nic.Signature;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import sa.gov.nic.impl.asic.AsicContainerCreator;
import java.io.OutputStream;
import java.io.InputStream;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import sa.gov.nic.impl.asic.AsicContainer;

public class AsicEContainer extends AsicContainer
{
    private static final Logger logger;
    
    public AsicEContainer() {
        this.setType("ASICE");
    }
    
    public AsicEContainer(final Configuration configuration) {
        super(configuration);
        this.setType("ASICE");
    }
    
    public AsicEContainer(final String containerPath) {
        super(containerPath, "ASICE");
    }
    
    protected AsicEContainer(final String containerPath, final String containerType) {
        super(containerPath, containerType);
    }
    
    public AsicEContainer(final String containerPath, final Configuration configuration) {
        super(containerPath, configuration, "ASICE");
    }
    
    protected AsicEContainer(final String containerPath, final Configuration configuration, final String containerType) {
        super(containerPath, configuration, containerType);
    }
    
    public AsicEContainer(final InputStream stream) {
        super(stream, "ASICE");
    }
    
    protected AsicEContainer(final InputStream stream, final String containerType) {
        super(stream, containerType);
    }
    
    public AsicEContainer(final InputStream stream, final Configuration configuration) {
        super(stream, configuration, "ASICE");
    }
    
    protected AsicEContainer(final InputStream stream, final Configuration configuration, final String containerType) {
        super(stream, configuration, containerType);
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
    
    @Override
    protected String createUserAgent() {
        if (!this.getSignatures().isEmpty()) {
            final SignatureProfile profile = this.getSignatures().get(0).getProfile();
            return Helper.createBDocUserAgent(profile);
        }
        return Helper.createBDocUserAgent();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicEContainer.class);
    }
}
