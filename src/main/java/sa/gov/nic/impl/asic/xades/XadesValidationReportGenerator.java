//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic.impl.asic.xades;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;
import sa.gov.nic.Configuration;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.exceptions.SignatureNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XadesValidationReportGenerator implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(XadesValidationReportGenerator.class);
    private transient SignedDocumentValidator validator;
    private transient Reports validationReport;
    private transient XAdESSignature dssSignature;
    private DSSDocument signatureDocument;
    private List<DSSDocument> detachedContents;
    private Configuration configuration;

    public XadesValidationReportGenerator(DSSDocument signatureDocument, List<DSSDocument> detachedContents, Configuration configuration) {
        this.signatureDocument = signatureDocument;
        this.detachedContents = detachedContents;
        this.configuration = configuration;
    }

    public Reports openValidationReport() {
        if (this.validationReport != null) {
            logger.debug("Using existing validation report");
            return this.validationReport;
        } else {
            this.validationReport = this.createNewValidationReport();
            this.printReport(this.validationReport);
            return this.validationReport;
        }
    }

    public XAdESSignature openDssSignature() {
        if (this.dssSignature == null) {
            this.initXadesValidator();
            this.dssSignature = this.getXAdESSignature();
        }

        return this.dssSignature;
    }

    public void setValidator(SignedDocumentValidator validator) {
        this.validator = validator;
    }

    private Reports createNewValidationReport() {
        try {
            logger.debug("Creating a new validation report");
            InputStream validationPolicyAsStream = this.getValidationPolicyAsStream();
            this.initXadesValidator();
            return this.validator.validateDocument(validationPolicyAsStream);
        } catch (DSSException var2) {
            logger.error("Error creating a new validation report: " + var2.getMessage());
            throw new DigiDoc4JException(var2);
        }
    }

    private void initXadesValidator() {
        if (this.validator == null) {
            this.validator = this.createXadesValidator();
        }

    }

    private SignedDocumentValidator createXadesValidator() {
        logger.debug("Creating a new xades validator");
        XadesValidationDssFacade validationFacade = new XadesValidationDssFacade(this.detachedContents, this.configuration);
        SignedDocumentValidator validator = validationFacade.openXadesValidator(this.signatureDocument);
        return validator;
    }

    private InputStream getValidationPolicyAsStream() {
        String policyFile = this.configuration.getValidationPolicy();
        if (Files.exists(Paths.get(policyFile), new LinkOption[0])) {
            try {
                return new FileInputStream(policyFile);
            } catch (FileNotFoundException var3) {
                logger.warn(var3.getMessage());
            }
        }

        return this.getClass().getClassLoader().getResourceAsStream(policyFile);
    }

    private XAdESSignature getXAdESSignature() {
        logger.debug("Opening XAdES signature");
        List<AdvancedSignature> signatures = this.validator.getSignatures();
        if (signatures != null && !signatures.isEmpty()) {
            if (signatures.size() > 1) {
                logger.warn("Signatures xml file contains more than one signature. This is not properly supported.");
            }

            return (XAdESSignature)signatures.get(0);
        } else {
            logger.error("Unable to open XAdES signature. Content is empty");
            throw new SignatureNotFoundException();
        }
    }

    private void printReport(Reports report) {
        if (logger.isTraceEnabled()) {
            logger.trace("----------------Validation report---------------");
            logger.trace(report.getXmlDetailedReport());
            logger.trace("----------------Simple report-------------------");
            logger.trace(report.getXmlSimpleReport());
        }

    }
}
