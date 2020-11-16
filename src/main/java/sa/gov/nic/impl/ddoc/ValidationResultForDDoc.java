// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.DOMImplementationLS;
import eu.europa.esig.dss.validation.SignatureQualification;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import java.util.Collections;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import org.w3c.dom.Comment;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;

//import ee.sk.digidoc.SignedDoc;
import java.util.ArrayList;
//import ee.sk.digidoc.DigiDocException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import org.slf4j.Logger;
import sa.gov.nic.ValidationResult;

public class ValidationResultForDDoc implements ValidationResult
{
    private static final Logger logger;
    private List<DigiDoc4JException> containerExceptions;
    private boolean hasFatalErrors;
    private List<DigiDoc4JException> errors;
    private Document report;
    private Element rootElement;
    
    public ValidationResultForDDoc(final List<DigiDocException> exceptions) {
        this(exceptions, null);
        ValidationResultForDDoc.logger.debug("");
    }
    
    public ValidationResultForDDoc(final List<DigiDocException> exceptions, final List<DigiDocException> openContainerExceptions) {
        this.containerExceptions = new ArrayList<DigiDoc4JException>();
        this.hasFatalErrors = false;
        this.errors = new ArrayList<DigiDoc4JException>();
        ValidationResultForDDoc.logger.debug("");
        this.initXMLReport();
        if (openContainerExceptions != null) {
            for (final DigiDocException exception : openContainerExceptions) {
                final DigiDoc4JException digiDoc4JException = new DigiDoc4JException(exception.getCode(), exception.getMessage());
                this.containerExceptions.add(digiDoc4JException);
                //if (SignedDoc.hasFatalErrs((ArrayList)openContainerExceptions)) {
                 //   this.hasFatalErrors = true;
               // }
            }
            exceptions.addAll(0, openContainerExceptions);
        }
        for (final DigiDocException exception : exceptions) {
            if (exception.getMessage().contains("X509IssuerName has none or invalid namespace:") || exception.getMessage().contains("X509SerialNumber has none or invalid namespace:")) {
                this.generateReport(exception, false);
            }
            else {
                this.generateReport(exception, true);
            }
        }
    }
    
    private void generateReport(final DigiDocException exception, final boolean isError) {
        final String message = exception.getMessage();
        final int code = exception.getCode();
        String warningOrError;
        if (!isError) {
            warningOrError = "warning";
        }
        else {
            final DigiDoc4JException digiDoc4JException = new DigiDoc4JException(code, message);
            this.errors.add(digiDoc4JException);
            warningOrError = "error";
        }
        ValidationResultForDDoc.logger.debug("Validation " + warningOrError + "." + " Code: " + code + ", message: " + message);
        final Element childElement = this.report.createElement(warningOrError);
        childElement.setAttribute("Code", Integer.toString(code));
        childElement.setAttribute("Message", message);
        this.rootElement.appendChild(childElement);
    }
    
    public boolean hasFatalErrors() {
        ValidationResultForDDoc.logger.debug("Has fatal errors: " + this.hasFatalErrors);
        return this.hasFatalErrors;
    }
    
    private void initXMLReport() {
        ValidationResultForDDoc.logger.debug("");
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            this.report = db.newDocument();
            this.rootElement = this.report.createElement("root");
            this.report.appendChild(this.rootElement);
            final Comment comment = this.report.createComment("DDoc verification result");
            this.report.insertBefore(comment, this.rootElement);
        }
        catch (ParserConfigurationException e) {
            ValidationResultForDDoc.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
    }
    
    @Override
    public List<DigiDoc4JException> getErrors() {
        ValidationResultForDDoc.logger.debug("Returning " + this.errors.size() + " errors");
        return this.errors;
    }
    
    @Override
    public List<DigiDoc4JException> getWarnings() {
        ValidationResultForDDoc.logger.debug("");
        return new ArrayList<DigiDoc4JException>();
    }
    
    @Override
    public boolean hasErrors() {
        final boolean hasErrors = this.errors.size() != 0;
        ValidationResultForDDoc.logger.debug("Has Errors: " + hasErrors);
        return hasErrors;
    }
    
    @Override
    public boolean hasWarnings() {
        ValidationResultForDDoc.logger.debug("");
        return false;
    }
    
    @Override
    public boolean isValid() {
        ValidationResultForDDoc.logger.debug("");
        return !this.hasErrors();
    }
    
    @Override
    public String getReport() {
        ValidationResultForDDoc.logger.debug("");
        return reportToString(this.report);
    }
    
    @Override
    public List<SignatureValidationReport> getSignatureReports() {
        ValidationResultForDDoc.logger.debug("Not For DDOC");
        return Collections.emptyList();
    }
    
    @Override
    public List<SimpleReport> getSignatureSimpleReports() {
        ValidationResultForDDoc.logger.debug("Not For DDOC");
        return Collections.emptyList();
    }
    
    @Override
    public Indication getIndication(final String signatureID) {
        ValidationResultForDDoc.logger.debug("value is not detected in case of JDigiDoc library and DDOC documents");
        return null;
    }
    
    @Override
    public SubIndication getSubIndication(final String signatureID) {
        ValidationResultForDDoc.logger.debug("value is not detected in case of JDigiDoc library and DDOC documents");
        return null;
    }
    
    @Override
    public SignatureQualification getSignatureQualification(final String signatureId) {
        ValidationResultForDDoc.logger.debug("value is not detected in case of JDigiDoc library and DDOC documents");
        return null;
    }
    
    static String reportToString(final Document document) {
        ValidationResultForDDoc.logger.debug("");
        final DOMImplementationLS domImplementation = (DOMImplementationLS)document.getImplementation();
        final LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(document);
    }
    
    @Override
    public List<DigiDoc4JException> getContainerErrors() {
        ValidationResultForDDoc.logger.debug("");
        return this.containerExceptions;
    }
    
    @Override
    public void saveXmlReports(final Path directory) {
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ValidationResultForDDoc.class);
    }
}
