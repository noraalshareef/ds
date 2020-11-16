// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice.bdoc;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import sa.gov.nic.Signature;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import sa.gov.nic.impl.asic.AsicContainerCreator;
import java.io.OutputStream;
import java.io.InputStream;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import sa.gov.nic.impl.asic.asice.AsicEContainer;

public class BDocContainer extends AsicEContainer
{
    private static final Logger logger;
    
    public BDocContainer() {
        this.setType("BDOC");
    }
    
    public BDocContainer(final Configuration configuration) {
        super(configuration);
        this.setType("BDOC");
    }
    
    public BDocContainer(final String containerPath) {
        super(containerPath, "BDOC");
    }
    
    public BDocContainer(final String containerPath, final Configuration configuration) {
        super(containerPath, configuration, "BDOC");
    }
    
    public BDocContainer(final InputStream stream) {
        super(stream, "BDOC");
    }
    
    public BDocContainer(final InputStream stream, final Configuration configuration) {
        super(stream, configuration, "BDOC");
    }
    
    @Override
    public void save(final OutputStream out) {
        this.writeAsicContainer(new AsicContainerCreator(out));
    }
    
    @Override
    protected List<Signature> parseSignatureFiles(final List<DSSDocument> signatureFiles, final List<DSSDocument> detachedContents) {
        final Configuration configuration = this.getConfiguration();
        final BDocSignatureOpener signatureOpener = new BDocSignatureOpener(detachedContents, configuration);
        final List<Signature> signatures = new ArrayList<Signature>(signatureFiles.size());
        for (final DSSDocument signatureFile : signatureFiles) {
            final List<BDocSignature> bDocSignatures = signatureOpener.parse(signatureFile);
            signatures.addAll(bDocSignatures);
        }
        return signatures;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BDocContainer.class);
    }
}
