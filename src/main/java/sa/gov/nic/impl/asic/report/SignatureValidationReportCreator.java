// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.report;

import org.slf4j.LoggerFactory;
import sa.gov.nic.SignatureProfile;
import eu.europa.esig.dss.validation.policy.rules.Indication;

import java.util.List;
import sa.gov.nic.exceptions.DigiDoc4JException;
import eu.europa.esig.dss.jaxb.simplereport.XmlSignature;
import eu.europa.esig.dss.jaxb.simplereport.SimpleReport;
import eu.europa.esig.dss.validation.reports.Reports;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidationData;
import org.slf4j.Logger;

public class SignatureValidationReportCreator
{
    private static final Logger logger;
    private SignatureValidationData validationData;
    private Reports reports;
    private SimpleReport simpleReport;
    private SignatureValidationReport signatureValidationReport;
    
    public SignatureValidationReportCreator(final SignatureValidationData validationData) {
        this.validationData = validationData;
        this.reports = validationData.getReport().getReport();
        this.simpleReport = this.reports.getSimpleReportJaxb();
    }
    
    public static SignatureValidationReport create(final SignatureValidationData validationData) {
        return new SignatureValidationReportCreator(validationData).createSignatureValidationReport();
    }
    
    private SignatureValidationReport createSignatureValidationReport() {
        this.signatureValidationReport = this.cloneSignatureValidationReport();
        this.updateMissingErrors();
        this.updateDocumentName();
        this.updateIndication();
        this.updateSignatureFormat();
        return this.signatureValidationReport;
    }
    
    private SignatureValidationReport cloneSignatureValidationReport() {
        if (this.simpleReport.getSignature().size() > 1) {
            SignatureValidationReportCreator.logger.warn("Simple report contains more than one signature: " + this.simpleReport.getSignature().size());
        }
        final XmlSignature signatureXmlReport = this.simpleReport.getSignature().get(0);
        return SignatureValidationReport.create(signatureXmlReport);
    }
    
    private void updateMissingErrors() {
        final List<String> errors = (List<String>)this.signatureValidationReport.getErrors();
        for (final DigiDoc4JException error : this.validationData.getValidationResult().getErrors()) {
            if (!errors.contains(error.getMessage())) {
                errors.add(error.getMessage());
            }
        }
    }
    
    private void updateDocumentName() {
        final String documentName = this.reports.getDiagnosticData().getDocumentName();
        this.signatureValidationReport.setDocumentName(documentName);
    }
    
    private void updateIndication() {
        if (!this.validationData.getValidationResult().isValid() && (this.signatureValidationReport.getIndication() == Indication.TOTAL_PASSED || this.signatureValidationReport.getIndication() == Indication.PASSED)) {
            this.signatureValidationReport.setIndication(Indication.INDETERMINATE);
        }
    }
    
    private void updateSignatureFormat() {
        if (this.validationData.getSignatureProfile() == SignatureProfile.LT_TM) {
            this.signatureValidationReport.setSignatureFormat("XAdES_BASELINE_LT_TM");
        }
        if (this.validationData.getSignatureProfile() == SignatureProfile.B_EPES) {
            this.signatureValidationReport.setSignatureFormat("XAdES_BASELINE_B_EPES");
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureValidationReportCreator.class);
    }
}
