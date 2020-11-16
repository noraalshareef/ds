// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import java.nio.file.Path;
import eu.europa.esig.dss.validation.SignatureQualification;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import sa.gov.nic.exceptions.NotYetImplementedException;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import sa.gov.nic.exceptions.NotSupportedException;
import java.util.ArrayList;
import org.bouncycastle.tsp.TimeStampToken;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import sa.gov.nic.ValidationResult;

public class TimeStampValidationResult implements ValidationResult
{
    private List<DigiDoc4JException> errors;
    private List<DigiDoc4JException> warnings;
    private List<DigiDoc4JException> containerErrorsOnly;
    private AsicValidationReportBuilder reportBuilder;
    private List<SimpleReport> simpleReports;
    private String signedBy;
    private String signedTime;
    private TimeStampToken timeStampToken;
    
    public TimeStampValidationResult() {
        this.errors = new ArrayList<DigiDoc4JException>();
        this.warnings = new ArrayList<DigiDoc4JException>();
        this.containerErrorsOnly = new ArrayList<DigiDoc4JException>();
        this.simpleReports = new ArrayList<SimpleReport>();
        this.signedBy = "";
        this.signedTime = "";
    }
    
    public TimeStampToken getTimeStampToken() {
        return this.timeStampToken;
    }
    
    public void setTimeStampToken(final TimeStampToken timeStampToken) {
        this.timeStampToken = timeStampToken;
    }
    
    public String getSignedTime() {
        return this.signedTime;
    }
    
    public void setSignedTime(final String signedTime) {
        this.signedTime = signedTime;
    }
    
    public String getSignedBy() {
        return this.signedBy;
    }
    
    public void setSignedBy(final String signedBy) {
        this.signedBy = signedBy;
    }
    
    @Override
    public List<DigiDoc4JException> getErrors() {
        return this.errors;
    }
    
    @Override
    public List<DigiDoc4JException> getWarnings() {
        throw new NotSupportedException("Not Supported in case of timestamp token");
    }
    
    public void setErrors(final List<DigiDoc4JException> errors) {
        this.errors = errors;
    }
    
    public void setWarnings(final List<DigiDoc4JException> warnings) {
        this.warnings = warnings;
    }
    
    @Override
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
    
    @Override
    public boolean hasWarnings() {
        return false;
    }
    
    @Override
    public boolean isValid() {
        return !this.hasErrors();
    }
    
    @Override
    public String getReport() {
        return null;
    }
    
    @Override
    public List<SignatureValidationReport> getSignatureReports() {
        throw new NotSupportedException("Not Supported in case of timestamp token");
    }
    
    @Override
    public List<SimpleReport> getSignatureSimpleReports() {
        throw new NotYetImplementedException();
    }
    
    @Deprecated
    @Override
    public Indication getIndication(final String signatureId) {
        throw new NotSupportedException("Not supported in case of timestamp token container");
    }
    
    public Indication getIndication() {
        if (!this.hasErrors()) {
            return Indication.TOTAL_PASSED;
        }
        return Indication.TOTAL_FAILED;
    }
    
    @Deprecated
    @Override
    public SubIndication getSubIndication(final String signatureId) {
        throw new NotSupportedException("Not Supported in case of timestamp token");
    }
    
    @Override
    public SignatureQualification getSignatureQualification(final String signatureId) {
        throw new NotSupportedException("Not Supported in case of timestamp token");
    }
    
    @Override
    public List<DigiDoc4JException> getContainerErrors() {
        return this.getErrors();
    }
    
    @Override
    public void saveXmlReports(final Path directory) {
        throw new NotYetImplementedException();
    }
}
