// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.x509.crl.ListCRLSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import sa.gov.nic.impl.asic.SKCommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.DSSException;
import sa.gov.nic.exceptions.InvalidSignatureException;
import sa.gov.nic.utils.Helper;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidator;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import sa.gov.nic.Configuration;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import org.slf4j.Logger;

public class XadesValidationDssFacade
{
    private static final Logger logger;
    private List<DSSDocument> detachedContents;
    private Configuration configuration;
    private CertificateVerifier certificateVerifier;
    
    public XadesValidationDssFacade(final List<DSSDocument> detachedContents, final Configuration configuration) {
        this.detachedContents = detachedContents;
        this.configuration = configuration;
        this.certificateVerifier = this.createCertificateVerifier();
    }
    
    public SignedDocumentValidator openXadesValidator(final DSSDocument signature) {
        try {
            XadesValidationDssFacade.logger.debug("Opening signature validator");
            final SignedDocumentValidator validator = (SignedDocumentValidator)new XMLDocumentValidator(signature);
            XadesValidationDssFacade.logger.debug("Finished opening signature validator");
            validator.setDetachedContents((List)this.detachedContents);
            validator.setCertificateVerifier(this.certificateVerifier);
            final SignaturePolicyProvider signaturePolicyProvider = Helper.getBdocSignaturePolicyProvider(signature);
            validator.setSignaturePolicyProvider(signaturePolicyProvider);
            return validator;
        }
        catch (DSSException e) {
            XadesValidationDssFacade.logger.error("Failed to parse xades signature: " + e.getMessage());
            throw new InvalidSignatureException();
        }
    }
    
    private CertificateVerifier createCertificateVerifier() {
        XadesValidationDssFacade.logger.debug("Creating new certificate verifier");
        final CertificateVerifier certificateVerifier = (CertificateVerifier)new SKCommonCertificateVerifier();
        certificateVerifier.setCrlSource((CRLSource)null);
        certificateVerifier.setSignatureCRLSource((ListCRLSource)null);
        XadesValidationDssFacade.logger.debug("Setting trusted cert source to the certificate verifier");
        certificateVerifier.setTrustedCertSource((CertificateSource)this.configuration.getTSL());
        XadesValidationDssFacade.logger.debug("Finished creating certificate verifier");
        return certificateVerifier;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)XadesValidationDssFacade.class);
    }
}
