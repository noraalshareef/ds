// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import sa.gov.nic.impl.asic.xades.validation.XadesValidationResult;
import org.slf4j.Logger;

public abstract class DssXadesSignature implements XadesSignature
{
    private static final Logger logger;
    private XadesValidationReportGenerator reportGenerator;
    
    public DssXadesSignature(final XadesValidationReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }
    
    @Override
    public XadesValidationResult validate() {
        DssXadesSignature.logger.debug("Validating xades signature");
        return new XadesValidationResult(this.reportGenerator.openValidationReport());
    }
    
    @Override
    public XAdESSignature getDssSignature() {
        return this.reportGenerator.openDssSignature();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DssXadesSignature.class);
    }
}
