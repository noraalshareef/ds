// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.pades;

import java.nio.file.Path;
import eu.europa.esig.dss.validation.SignatureQualification;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import org.apache.commons.lang3.StringUtils;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import java.util.Arrays;
import java.util.ArrayList;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import sa.gov.nic.ValidationResult;

public class PadesValidationResult implements ValidationResult
{
    private String report;
    private List<DigiDoc4JException> errors;
    private List<DigiDoc4JException> warnings;
    private List<SimpleReport> simpleReports;
    
    public PadesValidationResult(final SimpleReport simpleReport) {
        this.errors = new ArrayList<DigiDoc4JException>();
        this.warnings = new ArrayList<DigiDoc4JException>();
        this.simpleReports = new ArrayList<SimpleReport>();
        this.simpleReports = Arrays.asList(simpleReport);
    }
    
    @Override
    public boolean hasErrors() {
        return !this.getErrors().isEmpty();
    }
    
    @Override
    public boolean hasWarnings() {
        return !this.getWarnings().isEmpty();
    }
    
    public void setErrors(final List<DigiDoc4JException> errors) {
        this.errors = errors;
    }
    
    public void setWarnings(final List<DigiDoc4JException> warnings) {
        this.warnings = warnings;
    }
    
    @Override
    public boolean isValid() {
        return !this.hasErrors();
    }
    
    @Override
    public List<DigiDoc4JException> getErrors() {
        return this.errors;
    }
    
    @Override
    public List<DigiDoc4JException> getWarnings() {
        return this.warnings;
    }
    
    @Override
    public String getReport() {
        return this.report;
    }
    
    public void setReport(final String report) {
        this.report = report;
    }
    
    @Override
    public List<SignatureValidationReport> getSignatureReports() {
        return null;
    }
    
    @Override
    public List<SimpleReport> getSignatureSimpleReports() {
        return this.simpleReports;
    }
    
    @Override
    public Indication getIndication(final String signatureId) {
        if (StringUtils.isNotBlank((CharSequence)signatureId)) {
            return this.simpleReports.get(0).getIndication(signatureId);
        }
        throw new DigiDoc4JException("Signature id must be not null");
    }
    
    @Override
    public SubIndication getSubIndication(final String signatureId) {
        if (StringUtils.isNotBlank((CharSequence)signatureId)) {
            return this.simpleReports.get(0).getSubIndication(signatureId);
        }
        throw new DigiDoc4JException("Signature id must be not null");
    }
    
    @Override
    public SignatureQualification getSignatureQualification(final String signatureId) {
        return null;
    }
    
    @Override
    public List<DigiDoc4JException> getContainerErrors() {
        return null;
    }
    
    @Override
    public void saveXmlReports(final Path directory) {
    }
}
