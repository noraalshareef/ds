// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice;

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

public class AsicESignatureOpener
{
    private static final Logger logger;
    private final List<DSSDocument> detachedContents;
    private Configuration configuration;
    private XadesSignatureParser xadesSignatureParser;
    
    public AsicESignatureOpener(final List<DSSDocument> detachedContents, final Configuration configuration) {
        this.xadesSignatureParser = new XadesSignatureParser();
        this.configuration = configuration;
        this.detachedContents = detachedContents;
    }
    
    public List<AsicESignature> parse(final DSSDocument xadesDocument) {
        AsicESignatureOpener.logger.debug("Parsing xades document");
        final List<AsicESignature> signatures = new ArrayList<AsicESignature>(1);
        final AsicESignature asicSignature = this.createAsicESignature(xadesDocument);
        signatures.add(asicSignature);
        return signatures;
    }
    
    private AsicESignature createAsicESignature(final DSSDocument xadesDocument) {
        final XadesValidationReportGenerator xadesReportGenerator = new XadesValidationReportGenerator(xadesDocument, this.detachedContents, this.configuration);
        final XadesSignature signature = this.xadesSignatureParser.parse(xadesReportGenerator);
        final XadesSignatureValidator xadesValidator = this.createSignatureValidator(signature);
        final AsicESignature asicSignature = new AsicESignature(signature, xadesValidator);
        asicSignature.setSignatureDocument(xadesDocument);
        return asicSignature;
    }
    
    private XadesSignatureValidator createSignatureValidator(final XadesSignature signature) {
        final XadesSignatureValidatorFactory validatorFactory = new XadesSignatureValidatorFactory();
        validatorFactory.setConfiguration(this.configuration);
        validatorFactory.setSignature(signature);
        final XadesSignatureValidator xadesValidator = validatorFactory.create();
        return xadesValidator;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicESignatureOpener.class);
    }
}
