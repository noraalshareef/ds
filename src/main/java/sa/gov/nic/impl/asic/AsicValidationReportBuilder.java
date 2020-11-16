// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import sa.gov.nic.exceptions.TechnicalException;
import java.io.Writer;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import sa.gov.nic.SignatureValidationResult;
import eu.europa.esig.dss.jaxb.simplereport.XmlPolicy;
import sa.gov.nic.impl.asic.report.SignatureValidationReportCreator;
import java.util.Date;
import sa.gov.nic.impl.asic.report.ContainerValidationReport;
import eu.europa.esig.dss.validation.reports.Reports;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import eu.europa.esig.dss.DSSUtils;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import eu.europa.esig.dss.validation.reports.SimpleReport;
import sa.gov.nic.impl.asic.report.SignatureValidationReport;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidationData;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import org.slf4j.Logger;

public class AsicValidationReportBuilder
{
    private static final Logger logger;
    private List<DigiDoc4JException> manifestErrors;
    private List<SignatureValidationData> signatureValidationData;
    private String reportInXml;
    
    public AsicValidationReportBuilder(final List<SignatureValidationData> signatureValidationData, final List<DigiDoc4JException> manifestErrors) {
        AsicValidationReportBuilder.logger.debug("Initializing ASiC validation report builder");
        this.manifestErrors = manifestErrors;
        this.signatureValidationData = signatureValidationData;
    }
    
    public String buildXmlReport() {
        if (this.reportInXml == null) {
            this.reportInXml = this.generateNewReport();
        }
        return this.reportInXml;
    }
    
    public List<SignatureValidationReport> buildSignatureValidationReports() {
        return this.createSignaturesValidationReport();
    }
    
    public List<SimpleReport> buildSignatureSimpleReports() {
        final List<SimpleReport> signaturesReport = new ArrayList<SimpleReport>();
        for (final SignatureValidationData validationData : this.signatureValidationData) {
            signaturesReport.add(validationData.getReport().getReport().getSimpleReport());
        }
        return signaturesReport;
    }
    
    public void saveXmlReports(final Path directory) {
        try {
            final InputStream is = new ByteArrayInputStream(this.buildXmlReport().getBytes("UTF-8"));
            DSSUtils.saveToFile(is, directory + File.separator + "validationReport.xml");
            AsicValidationReportBuilder.logger.info("Validation report is generated");
        }
        catch (UnsupportedEncodingException e) {
            AsicValidationReportBuilder.logger.error(e.getMessage());
        }
        catch (IOException e2) {
            AsicValidationReportBuilder.logger.error(e2.getMessage());
        }
        if (!this.signatureValidationData.isEmpty()) {
            for (int n = this.signatureValidationData.size(), i = 0; i < n; ++i) {
                final SignatureValidationData validationData = this.signatureValidationData.get(i);
                final Reports reports = validationData.getReport().getReport();
                try {
                    final InputStream is = new ByteArrayInputStream(reports.getXmlDiagnosticData().getBytes("UTF-8"));
                    DSSUtils.saveToFile(is, directory + File.separator + "validationDiagnosticData" + Integer.toString(i) + ".xml");
                    AsicValidationReportBuilder.logger.info("Validation diagnostic data report is generated");
                }
                catch (UnsupportedEncodingException e3) {
                    AsicValidationReportBuilder.logger.error(e3.getMessage());
                }
                catch (IOException e4) {
                    AsicValidationReportBuilder.logger.error(e4.getMessage());
                }
                try {
                    final InputStream is = new ByteArrayInputStream(reports.getXmlSimpleReport().getBytes("UTF-8"));
                    DSSUtils.saveToFile(is, directory + File.separator + "validationSimpleReport" + Integer.toString(i) + ".xml");
                    AsicValidationReportBuilder.logger.info("Validation simple report is generated");
                }
                catch (UnsupportedEncodingException e3) {
                    AsicValidationReportBuilder.logger.error(e3.getMessage());
                }
                catch (IOException e4) {
                    AsicValidationReportBuilder.logger.error(e4.getMessage());
                }
                try {
                    final InputStream is = new ByteArrayInputStream(reports.getXmlDetailedReport().getBytes("UTF-8"));
                    DSSUtils.saveToFile(is, directory + File.separator + "validationDetailReport" + Integer.toString(i) + ".xml");
                    AsicValidationReportBuilder.logger.info("Validation detailed report is generated");
                }
                catch (UnsupportedEncodingException e3) {
                    AsicValidationReportBuilder.logger.error(e3.getMessage());
                }
                catch (IOException e4) {
                    AsicValidationReportBuilder.logger.error(e4.getMessage());
                }
            }
        }
    }
    
    private String generateNewReport() {
        AsicValidationReportBuilder.logger.debug("Generating a new XML validation report");
        final ContainerValidationReport report = new ContainerValidationReport();
        report.setPolicy(this.extractValidationPolicy());
        report.setValidationTime(new Date());
        report.setSignaturesCount(this.signatureValidationData.size());
        report.setValidSignaturesCount(this.extractValidSignaturesCount());
        report.setSignatures(this.createSignaturesValidationReport());
        report.setContainerErrors(this.createContainerErrors());
        return this.createFormattedXmlString(report);
    }
    
    private List<SignatureValidationReport> createSignaturesValidationReport() {
        final List<SignatureValidationReport> signaturesReport = new ArrayList<SignatureValidationReport>();
        for (final SignatureValidationData validationData : this.signatureValidationData) {
            final SignatureValidationReport signatureValidationReport = SignatureValidationReportCreator.create(validationData);
            signaturesReport.add(signatureValidationReport);
        }
        return signaturesReport;
    }
    
    private XmlPolicy extractValidationPolicy() {
        if (this.signatureValidationData.isEmpty()) {
            return null;
        }
        final SignatureValidationData validationData = this.signatureValidationData.get(0);
        final eu.europa.esig.dss.jaxb.simplereport.SimpleReport simpleReport = validationData.getReport().getReport().getSimpleReportJaxb();
        return simpleReport.getPolicy();
    }
    
    private int extractValidSignaturesCount() {
        int validSignaturesCount = 0;
        for (final SignatureValidationData validationData : this.signatureValidationData) {
            final SignatureValidationResult validationResult = validationData.getValidationResult();
            if (validationResult.isValid()) {
                ++validSignaturesCount;
            }
        }
        return validSignaturesCount;
    }
    
    private List<String> createContainerErrors() {
        final List<String> containerErrors = new ArrayList<String>();
        for (final DigiDoc4JException manifestError : this.manifestErrors) {
            containerErrors.add(manifestError.getMessage());
        }
        return containerErrors;
    }
    
    private String createFormattedXmlString(final ContainerValidationReport simpleReport) {
        try {
            final JAXBContext context = JAXBContext.newInstance(new Class[] { ContainerValidationReport.class });
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", (Object)true);
            final StringWriter stringWriter = new StringWriter();
            marshaller.marshal((Object)simpleReport, (Writer)stringWriter);
            final String xmlReport = stringWriter.toString();
            AsicValidationReportBuilder.logger.trace(xmlReport);
            return xmlReport;
        }
        catch (JAXBException e) {
            throw new TechnicalException("Failed to create validation report in XML: " + e.getMessage(), (Throwable)e);
        }
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicValidationReportBuilder.class);
    }
}
