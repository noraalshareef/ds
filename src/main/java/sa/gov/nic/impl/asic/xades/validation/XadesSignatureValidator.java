// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import org.slf4j.LoggerFactory;
import sa.gov.nic.exceptions.InvalidOcspNonceException;
import sa.gov.nic.impl.asic.OcspNonceValidator;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import sa.gov.nic.exceptions.InvalidTimestampException;
import sa.gov.nic.exceptions.CertificateRevokedException;
import eu.europa.esig.dss.validation.process.MessageTag;
import sa.gov.nic.exceptions.MultipleSignedPropertiesException;
import sa.gov.nic.exceptions.SignedPropertiesMissingException;
import org.w3c.dom.Element;
import eu.europa.esig.dss.xades.XPathQueryHolder;
import sa.gov.nic.exceptions.WrongPolicyIdentifierQualifierException;
import org.w3c.dom.Node;
import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.x509.SignaturePolicy;
import sa.gov.nic.exceptions.WrongPolicyIdentifierException;
import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.utils.Helper;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import java.util.Map;
import sa.gov.nic.SignatureValidationResult;
import java.util.ArrayList;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import eu.europa.esig.dss.validation.reports.Reports;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import org.slf4j.Logger;

public class XadesSignatureValidator implements SignatureValidator
{
    public static final String TM_POLICY = "1.3.6.1.4.1.10015.1000.3.2.1";
    private static final Logger logger;
    private static final String OIDAS_URN = "OIDAsURN";
    private static final String XADES_SIGNED_PROPERTIES = "http://uri.etsi.org/01903#SignedProperties";
    protected XadesSignature signature;
    private transient Reports validationReport;
    private transient SimpleReport simpleReport;
    private List<DigiDoc4JException> validationErrors;
    private List<DigiDoc4JException> validationWarnings;
    private String signatureId;
    
    public XadesSignatureValidator(final XadesSignature signature) {
        this.validationErrors = new ArrayList<DigiDoc4JException>();
        this.validationWarnings = new ArrayList<DigiDoc4JException>();
        this.signature = signature;
        this.signatureId = signature.getId();
    }
    
    @Override
    public SignatureValidationResult extractValidationErrors() {
        XadesSignatureValidator.logger.debug("Extracting validation errors");
        final XadesValidationResult validationResult = this.signature.validate();
        this.validationReport = validationResult.getReport();
        final Map<String, SimpleReport> simpleReports = validationResult.extractSimpleReports();
        this.simpleReport = this.getSimpleReport(simpleReports);
        this.populateValidationErrors();
        return this.createValidationResult();
    }
    
    protected void populateValidationErrors() {
        this.addPolicyValidationErrors();
        this.addPolicyUriValidationErrors();
        this.addPolicyErrors();
        this.addSignedPropertiesReferenceValidationErrors();
        this.addReportedErrors();
        this.addReportedWarnings();
        this.addTimestampErrors();
        this.addOcspErrors();
    }
    
    protected void addValidationError(final DigiDoc4JException error) {
        error.setSignatureId(this.getDssSignature().getId());
        this.validationErrors.add(error);
    }
    
    protected void addValidationWarning(final DigiDoc4JException warning) {
        warning.setSignatureId(this.getDssSignature().getId());
        this.validationWarnings.add(warning);
    }
    
    protected void addPolicyErrors() {
    }
    
    protected XAdESSignature getDssSignature() {
        return this.signature.getDssSignature();
    }
    
    private void addPolicyValidationErrors() {
        XadesSignatureValidator.logger.debug("Extracting policy validation errors");
        final XAdESSignature dssSignature = this.getDssSignature();
        final SignaturePolicy policy = dssSignature.getPolicyId();
        if (policy != null && dssSignature.getSignatureTimestamps().isEmpty()) {
            final String policyIdentifier = Helper.getIdentifier(policy.getIdentifier());
            if (!StringUtils.equals((CharSequence)"1.3.6.1.4.1.10015.1000.3.2.1", (CharSequence)policyIdentifier)) {
                this.addValidationError(new WrongPolicyIdentifierException("Wrong policy identifier: " + policyIdentifier));
            }
            else {
                this.addPolicyIdentifierQualifierValidationErrors();
            }
        }
        else if (policy != null && !dssSignature.getSignatureTimestamps().isEmpty()) {
            XadesSignatureValidator.logger.debug("Signature profile is not LT_TM, but has defined policy");
        }
    }
    
    private void addPolicyUriValidationErrors() {
        XadesSignatureValidator.logger.debug("Extracting policy URL validation errors");
        final SignaturePolicy policy = this.getDssSignature().getPolicyId();
        if (policy != null && StringUtils.isBlank((CharSequence)policy.getUrl())) {
            this.addValidationError(new WrongPolicyIdentifierException("Error: The URL in signature policy is empty or not available"));
        }
    }
    
    private void addPolicyIdentifierQualifierValidationErrors() {
        XadesSignatureValidator.logger.debug("Extracting policy identifier qualifier validation errors");
        final XPathQueryHolder xPathQueryHolder = this.getDssSignature().getXPathQueryHolder();
        final Element signatureElement = this.getDssSignature().getSignatureElement();
        final Element element = DomUtils.getElement((Node)signatureElement, xPathQueryHolder.XPATH_SIGNATURE_POLICY_IDENTIFIER);
        final Element identifier = DomUtils.getElement((Node)element, "./xades:SignaturePolicyId/xades:SigPolicyId/xades:Identifier");
        final String qualifier = identifier.getAttribute("Qualifier");
        if (!StringUtils.equals((CharSequence)"OIDAsURN", (CharSequence)qualifier)) {
            this.addValidationError(new WrongPolicyIdentifierQualifierException("Wrong policy identifier qualifier: " + qualifier));
        }
    }
    
    private void addSignedPropertiesReferenceValidationErrors() {
        XadesSignatureValidator.logger.debug("Extracting signed properties reference validation errors");
        final int propertiesReferencesCount = this.findSignedPropertiesReferencesCount();
        final String sigId = this.getDssSignature().getId();
        if (propertiesReferencesCount == 0) {
            XadesSignatureValidator.logger.error("Signed properties are missing for signature " + sigId);
            this.addValidationError(new SignedPropertiesMissingException("Signed properties missing"));
        }
        if (propertiesReferencesCount > 1) {
            XadesSignatureValidator.logger.error("Multiple signed properties for signature " + sigId);
            final DigiDoc4JException error = new MultipleSignedPropertiesException("Multiple signed properties");
            this.addValidationError(error);
        }
    }
    
    private int findSignedPropertiesReferencesCount() {
        final List<Element> signatureReferences = (List<Element>)this.getDssSignature().getSignatureReferences();
        int nrOfSignedPropertiesReferences = 0;
        for (final Element signatureReference : signatureReferences) {
            final String type = signatureReference.getAttribute("Type");
            if (StringUtils.equals((CharSequence)"http://uri.etsi.org/01903#SignedProperties", (CharSequence)type)) {
                ++nrOfSignedPropertiesReferences;
            }
        }
        return nrOfSignedPropertiesReferences;
    }
    
    private void addReportedErrors() {
        XadesSignatureValidator.logger.debug("Extracting reported errors");
        if (this.simpleReport != null) {
            for (final String errorMessage : this.simpleReport.getErrors(this.signatureId)) {
                if (this.isRedundantErrorMessage(errorMessage)) {
                    XadesSignatureValidator.logger.debug("Ignoring redundant error message: " + errorMessage);
                }
                else {
                    XadesSignatureValidator.logger.error(errorMessage);
                    if (errorMessage.contains(MessageTag.BBB_XCV_ISCR_ANS.getMessage())) {
                        this.addValidationError(new CertificateRevokedException(errorMessage));
                    }
                    else if (errorMessage.contains(MessageTag.PSV_IPSVC_ANS.getMessage())) {
                        this.addValidationError(new CertificateRevokedException(errorMessage));
                    }
                    else {
                        final String sigId = this.getDssSignature().getId();
                        this.addValidationError(new DigiDoc4JException(errorMessage, sigId));
                    }
                }
            }
        }
    }
    
    private boolean isRedundantErrorMessage(final String errorMessage) {
        return StringUtils.equalsIgnoreCase((CharSequence)errorMessage, (CharSequence)MessageTag.ADEST_ROBVPIIC_ANS.getMessage()) || StringUtils.equalsIgnoreCase((CharSequence)errorMessage, (CharSequence)MessageTag.LTV_ABSV_ANS.getMessage()) || StringUtils.equalsIgnoreCase((CharSequence)errorMessage, (CharSequence)MessageTag.ARCH_LTVV_ANS.getMessage()) || StringUtils.equalsIgnoreCase((CharSequence)errorMessage, (CharSequence)MessageTag.BBB_XCV_RFC_ANS.getMessage()) || StringUtils.equalsIgnoreCase((CharSequence)errorMessage, (CharSequence)MessageTag.BBB_XCV_SUB_ANS.getMessage());
    }
    
    private void addReportedWarnings() {
        if (this.simpleReport != null) {
            for (final String warning : this.simpleReport.getWarnings(this.signatureId)) {
                XadesSignatureValidator.logger.warn(warning);
                this.validationWarnings.add(new DigiDoc4JException(warning, this.signatureId));
            }
        }
    }
    
    private void addTimestampErrors() {
        if (!this.isTimestampValidForSignature()) {
            XadesSignatureValidator.logger.error("Signature " + this.signatureId + " has an invalid timestamp");
            this.addValidationError(new InvalidTimestampException());
        }
    }
    
    private boolean isTimestampValidForSignature() {
        XadesSignatureValidator.logger.debug("Finding timestamp errors for signature " + this.signatureId);
        final DiagnosticData diagnosticData = this.validationReport.getDiagnosticData();
        if (diagnosticData == null) {
            return true;
        }
        final List<String> timestampIdList = (List<String>)diagnosticData.getTimestampIdList(this.signatureId);
        if (timestampIdList == null || timestampIdList.isEmpty()) {
            return true;
        }
        final String timestampId = timestampIdList.get(0);
        final DetailedReport detailedReport = this.validationReport.getDetailedReport();
        final Indication indication = detailedReport.getTimestampValidationIndication(timestampId);
        return this.isIndicationValid(indication);
    }
    
    private SimpleReport getSimpleReport(final Map<String, SimpleReport> simpleReports) {
        final SimpleReport simpleRep = simpleReports.get(this.signatureId);
        if (simpleRep != null && simpleReports.size() == 1) {
            return simpleReports.values().iterator().next();
        }
        return simpleRep;
    }
    
    private void addOcspErrors() {
        final OcspNonceValidator ocspValidator = new OcspNonceValidator(this.getDssSignature());
        if (!ocspValidator.isValid()) {
            XadesSignatureValidator.logger.error("OCSP nonce is invalid");
            this.addValidationError(new InvalidOcspNonceException());
        }
    }
    
    private SignatureValidationResult createValidationResult() {
        final SignatureValidationResult result = new SignatureValidationResult();
        result.setErrors(this.validationErrors);
        result.setWarnings(this.validationWarnings);
        return result;
    }
    
    private boolean isIndicationValid(final Indication indication) {
        return indication == Indication.PASSED || indication == Indication.TOTAL_PASSED;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)XadesSignatureValidator.class);
    }
}
