// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import java.util.LinkedHashMap;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import java.util.Map;
import eu.europa.esig.dss.validation.reports.Reports;

public class XadesValidationResult
{
    private Reports validationReport;
    
    public XadesValidationResult(final Reports validationReport) {
        this.validationReport = validationReport;
    }
    
    public Reports getReport() {
        return this.validationReport;
    }
    
    public Map<String, SimpleReport> extractSimpleReports() {
        final Map<String, SimpleReport> simpleReports = new LinkedHashMap<String, SimpleReport>();
        final SimpleReport simpleReport = this.validationReport.getSimpleReport();
        if (simpleReport.getSignatureIdList().size() > 0) {
            simpleReports.put(simpleReport.getSignatureIdList().get(0), simpleReport);
        }
        return simpleReports;
    }
}
