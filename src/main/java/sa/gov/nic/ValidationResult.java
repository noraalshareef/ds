// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.nio.file.Path;
import eu.europa.esig.dss.validation.SignatureQualification;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;

public interface ValidationResult
{
    List<DigiDoc4JException> getErrors();
    
    List<DigiDoc4JException> getWarnings();
    
    @Deprecated
    boolean hasErrors();
    
    boolean hasWarnings();
    
    boolean isValid();
    
    String getReport();
    
    List<SignatureValidationReport> getSignatureReports();
    
    List<SimpleReport> getSignatureSimpleReports();
    
    Indication getIndication(final String p0);
    
    SubIndication getSubIndication(final String p0);
    
    SignatureQualification getSignatureQualification(final String p0);
    
    List<DigiDoc4JException> getContainerErrors();
    
    void saveXmlReports(final Path p0);
}
