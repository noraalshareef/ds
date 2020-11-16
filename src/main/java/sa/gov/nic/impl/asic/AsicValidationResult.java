// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import java.nio.file.Path;

import eu.europa.esig.dss.validation.SignatureQualification;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import org.apache.commons.lang3.StringUtils;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import java.util.ArrayList;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import sa.gov.nic.ValidationResult;

public class AsicValidationResult implements ValidationResult
{
    private List<DigiDoc4JException> errors;
    private List<DigiDoc4JException> warnings;
    private List<DigiDoc4JException> containerErrorsOnly;
    private AsicValidationReportBuilder reportBuilder;
    private List<SimpleReport> simpleReports;
    
    public AsicValidationResult() {
        this.errors = new ArrayList<DigiDoc4JException>();
        this.warnings = new ArrayList<DigiDoc4JException>();
        this.containerErrorsOnly = new ArrayList<DigiDoc4JException>();
        this.simpleReports = new ArrayList<SimpleReport>();
    }
    
    @Override
    public List<DigiDoc4JException> getErrors() {
        return this.errors;
    }
    
    @Override
    public List<DigiDoc4JException> getWarnings() {
        return this.warnings;
    }
    
    @Deprecated
    @Override
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
    
    @Override
    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }
    
    @Override
    public boolean isValid() {
        return !this.hasErrors();
    }
    
    @Override
    public String getReport() {
        return this.reportBuilder.buildXmlReport();
    }
    
    @Override
    public List<SignatureValidationReport> getSignatureReports() {
        return this.reportBuilder.buildSignatureValidationReports();
    }
    
    @Override
    public List<SimpleReport> getSignatureSimpleReports() {
        return this.buildSignatureSimpleReports();
    }
    
    private List<SimpleReport> buildSignatureSimpleReports() {
        if (this.simpleReports.isEmpty()) {
            this.simpleReports = this.reportBuilder.buildSignatureSimpleReports();
        }
        return this.simpleReports;
    }
    
    @Override
    public Indication getIndication(final String signatureId) {
        if (StringUtils.isBlank((CharSequence)signatureId)) {
            final SimpleReport simpleReport = this.getSimpleReport();
            return (simpleReport != null) ? simpleReport.getIndication(simpleReport.getFirstSignatureId()) : null;
        }
        final SimpleReport reportBySignatureId = this.getSimpleReportBySignatureId(signatureId);
        return (reportBySignatureId != null) ? reportBySignatureId.getIndication(signatureId) : null;
    }
    
    @Override
    public SubIndication getSubIndication(final String signatureId) {
        if (StringUtils.isBlank((CharSequence)signatureId)) {
            final SimpleReport simpleReport = this.getSimpleReport();
            return (simpleReport != null) ? simpleReport.getSubIndication(simpleReport.getFirstSignatureId()) : null;
        }
        final SimpleReport reportBySignatureId = this.getSimpleReportBySignatureId(signatureId);
        return (reportBySignatureId != null) ? reportBySignatureId.getSubIndication(signatureId) : null;
    }
    
    @Override
    public SignatureQualification getSignatureQualification(final String signatureId) {
        if (StringUtils.isBlank((CharSequence)signatureId)) {
            final SimpleReport simpleReport = this.getSimpleReport();
            return (simpleReport != null) ? simpleReport.getSignatureQualification(simpleReport.getFirstSignatureId()) : null;
        }
        final SimpleReport reportBySignatureId = this.getSimpleReportBySignatureId(signatureId);
        return (reportBySignatureId != null) ? reportBySignatureId.getSignatureQualification(signatureId) : null;
    }
    
    private SimpleReport getSimpleReport() {
        if (this.buildSignatureSimpleReports().size() > 0) {
            return this.buildSignatureSimpleReports().get(0);
        }
        return null;
    }
    
    private SimpleReport getSimpleReportBySignatureId(final String signatureId) {
        for (final SimpleReport signatureReport : this.buildSignatureSimpleReports()) {
            if (signatureReport.getFirstSignatureId().equals(signatureId)) {
                return signatureReport;
            }
        }
        return null;
    }
    
    @Override
    public List<DigiDoc4JException> getContainerErrors() {
        return this.containerErrorsOnly;
    }
    
    @Override
    public void saveXmlReports(final Path directory) {
        if (directory != null) {
            this.reportBuilder.saveXmlReports(directory);
        }
    }
    
    public void setContainerErrorsOnly(final List<DigiDoc4JException> containerErrorsOnly) {
        this.containerErrorsOnly = containerErrorsOnly;
    }
    
    public void setErrors(final List<DigiDoc4JException> errors) {
        this.errors = errors;
    }
    
    public void setWarnings(final List<DigiDoc4JException> warnings) {
        this.warnings = warnings;
    }
    
    public void setReportBuilder(final AsicValidationReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }
}
