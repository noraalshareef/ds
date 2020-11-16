// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.report;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import eu.europa.esig.dss.jaxb.simplereport.XmlSignature;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class SignatureValidationReport extends XmlSignature
{
    @XmlElement(name = "DocumentName")
    protected String documentName;
    
    public static SignatureValidationReport create(final XmlSignature xmlSignature) {
        final SignatureValidationReport report = new SignatureValidationReport();
        report.setSigningTime(xmlSignature.getSigningTime());
        report.setSignedBy(xmlSignature.getSignedBy());
        report.setIndication(xmlSignature.getIndication());
        report.setSignatureLevel(xmlSignature.getSignatureLevel());
        report.setSubIndication(xmlSignature.getSubIndication());
        report.getErrors().addAll(xmlSignature.getErrors());
        report.getWarnings().addAll(xmlSignature.getWarnings());
        report.getInfos().addAll(xmlSignature.getInfos());
        report.getSignatureScope().addAll(xmlSignature.getSignatureScope());
        report.setId(xmlSignature.getId());
        report.setType(xmlSignature.getType());
        report.setParentId(xmlSignature.getParentId());
        report.setSignatureFormat(xmlSignature.getSignatureFormat());
        return report;
    }
    
    public String getDocumentName() {
        return this.documentName;
    }
    
    public void setDocumentName(final String documentName) {
        this.documentName = documentName;
    }
}
