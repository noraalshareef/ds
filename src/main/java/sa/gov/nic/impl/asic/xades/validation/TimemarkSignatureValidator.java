// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import sa.gov.nic.exceptions.UntrustedRevocationSourceException;
import org.apache.commons.lang3.StringUtils;
import java.security.cert.X509Certificate;
import java.util.Date;
import sa.gov.nic.exceptions.SignedWithExpiredCertificateException;
import org.w3c.dom.Element;
import eu.europa.esig.dss.xades.XPathQueryHolder;
import sa.gov.nic.exceptions.InvalidTimemarkSignatureException;
import org.w3c.dom.Node;
import eu.europa.esig.dss.DomUtils;
import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import org.slf4j.Logger;

public class TimemarkSignatureValidator extends XadesSignatureValidator
{
    private final Logger log;
    
    public TimemarkSignatureValidator(final XadesSignature signature) {
        super(signature);
        this.log = LoggerFactory.getLogger((Class)TimemarkSignatureValidator.class);
    }
    
    @Override
    protected void populateValidationErrors() {
        super.populateValidationErrors();
        this.addCertificateExpirationError();
        this.addRevocationErrors();
    }
    
    @Override
    protected void addPolicyErrors() {
        this.log.debug("Extracting TM signature policy errors");
        final XPathQueryHolder xPathQueryHolder = this.getDssSignature().getXPathQueryHolder();
        final Element signaturePolicyImpliedElement = DomUtils.getElement((Node)this.getDssSignature().getSignatureElement(), String.format("%s%s", xPathQueryHolder.XPATH_SIGNATURE_POLICY_IDENTIFIER, xPathQueryHolder.XPATH__SIGNATURE_POLICY_IMPLIED.replace(".", "")));
        if (signaturePolicyImpliedElement != null) {
            this.log.error("Signature contains forbidden element");
            this.addValidationError(new InvalidTimemarkSignatureException("Signature contains forbidden <SignaturePolicyImplied> element"));
        }
    }
    
    private void addCertificateExpirationError() {
        final Date signingTime = this.signature.getTrustedSigningTime();
        if (signingTime == null) {
            return;
        }
        final X509Certificate signerCert = this.signature.getSigningCertificate().getX509Certificate();
        final boolean isCertValid = signingTime.compareTo(signerCert.getNotBefore()) >= 0 && signingTime.compareTo(signerCert.getNotAfter()) <= 0;
        if (!isCertValid) {
            this.log.error("Signature has been created with expired certificate");
            this.addValidationError(new SignedWithExpiredCertificateException());
        }
    }
    
    private void addRevocationErrors() {
        final DiagnosticData diagnosticData = this.signature.validate().getReport().getDiagnosticData();
        if (diagnosticData == null) {
            return;
        }
        final String certificateRevocationSource = diagnosticData.getCertificateRevocationSource(diagnosticData.getSigningCertificateId());
        this.log.debug("Revocation source is <{}>", (Object)certificateRevocationSource);
        if (StringUtils.equalsIgnoreCase((CharSequence)"CRLToken", (CharSequence)certificateRevocationSource)) {
            this.log.error("Signing certificate revocation source is CRL instead of OCSP");
            this.addValidationError(new UntrustedRevocationSourceException());
        }
    }
}
