// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.report;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import eu.europa.esig.dss.jaxb.simplereport.SimpleReport;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "SimpleReport")
public class ContainerValidationReport extends SimpleReport
{
    @XmlElement(name = "Signature")
    protected List<SignatureValidationReport> signatures;
    @XmlElement(name = "ContainerError")
    protected List<String> containerErrors;
    
    public List<String> getContainerErrors() {
        return this.containerErrors;
    }
    
    public void setContainerErrors(final List<String> containerErrors) {
        this.containerErrors = containerErrors;
    }
    
    public List<SignatureValidationReport> getSignatures() {
        if (this.signatures == null) {
            this.signatures = new ArrayList<SignatureValidationReport>();
        }
        return this.signatures;
    }
    
    public void setSignatures(final List<SignatureValidationReport> signatures) {
        this.signatures = signatures;
    }
}
