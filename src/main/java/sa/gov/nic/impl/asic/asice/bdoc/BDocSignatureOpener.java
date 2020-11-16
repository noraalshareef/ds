// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice.bdoc;

import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.asic.xades.validation.XadesSignatureValidatorFactory;
import sa.gov.nic.impl.asic.xades.validation.XadesSignatureValidator;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.impl.asic.xades.XadesValidationReportGenerator;
import java.util.ArrayList;
import sa.gov.nic.impl.asic.xades.XadesSignatureParser;
import sa.gov.nic.Configuration;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import org.slf4j.Logger;

public class BDocSignatureOpener
{
    private static final Logger logger;
    private final List<DSSDocument> detachedContents;
    private Configuration configuration;
    private XadesSignatureParser xadesSignatureParser;
    
    public BDocSignatureOpener(final List<DSSDocument> detachedContents, final Configuration configuration) {
        this.xadesSignatureParser = new XadesSignatureParser();
        this.configuration = configuration;
        this.detachedContents = detachedContents;
    }
    
    public List<BDocSignature> parse(final DSSDocument xadesDocument) {
        BDocSignatureOpener.logger.debug("Parsing xades document");
        final List<BDocSignature> signatures = new ArrayList<BDocSignature>(1);
        final BDocSignature bDocSignature = this.createBDocSignature(xadesDocument);
        signatures.add(bDocSignature);
        return signatures;
    }
    
    private BDocSignature createBDocSignature(final DSSDocument xadesDocument) {
        final XadesValidationReportGenerator xadesReportGenerator = new XadesValidationReportGenerator(xadesDocument, this.detachedContents, this.configuration);
        final XadesSignature signature = this.xadesSignatureParser.parse(xadesReportGenerator);
        final XadesSignatureValidator xadesValidator = this.createSignatureValidator(signature);
        final BDocSignature bDocSignature = new BDocSignature(signature, xadesValidator);
        bDocSignature.setSignatureDocument(xadesDocument);
        return bDocSignature;
    }
    
    private XadesSignatureValidator createSignatureValidator(final XadesSignature signature) {
        final XadesSignatureValidatorFactory validatorFactory = new XadesSignatureValidatorFactory();
        validatorFactory.setConfiguration(this.configuration);
        validatorFactory.setSignature(signature);
        final XadesSignatureValidator xadesValidator = validatorFactory.create();
        return xadesValidator;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BDocSignatureOpener.class);
    }
}
