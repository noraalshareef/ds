//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic.impl.asic.asice;

import eu.europa.esig.dss.DSSDocument;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import sa.gov.nic.Configuration;
import sa.gov.nic.Signature;
import sa.gov.nic.SignatureValidationResult;
import sa.gov.nic.ValidationResult;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.exceptions.UnsupportedFormatException;
import sa.gov.nic.impl.asic.AsicParseResult;
import sa.gov.nic.impl.asic.AsicValidationReportBuilder;
import sa.gov.nic.impl.asic.AsicValidationResult;
import sa.gov.nic.impl.asic.manifest.ManifestErrorMessage;
import sa.gov.nic.impl.asic.manifest.ManifestParser;
import sa.gov.nic.impl.asic.manifest.ManifestValidator;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidationData;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidationTask;
import sa.gov.nic.impl.asic.xades.validation.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsicEContainerValidator implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(AsicEContainerValidator.class);
    private List<DigiDoc4JException> errors = new ArrayList();
    private List<DigiDoc4JException> warnings = new ArrayList();
    private AsicParseResult containerParseResult;
    private boolean validateManifest;
    private List<SignatureValidationData> signatureValidationData = new ArrayList();
    private List<DigiDoc4JException> manifestErrors;
    private ThreadPoolManager threadPoolManager;

    public AsicEContainerValidator(Configuration configuration) {
        this.threadPoolManager = new ThreadPoolManager(configuration);
        this.validateManifest = false;
    }

    public AsicEContainerValidator(AsicParseResult containerParseResult, Configuration configuration) {
        this.containerParseResult = containerParseResult;
        this.threadPoolManager = new ThreadPoolManager(configuration);
        this.validateManifest = true;
    }

    public ValidationResult validate(List<Signature> signatures) {
        logger.debug("Validating container");
        this.validateSignatures(signatures);
        this.extractManifestErrors(signatures);
        AsicValidationResult result = this.createValidationResult();
        logger.info("Is container valid: " + result.isValid());
        return result;
    }

    private void validateSignatures(List<Signature> signatures) {
        List<Future<SignatureValidationData>> validationData = this.startSignatureValidationInParallel(signatures);
        this.extractValidatedSignatureErrors(validationData);
    }

    private List<Future<SignatureValidationData>> startSignatureValidationInParallel(List<Signature> signatures) {
        List<Future<SignatureValidationData>> futures = new ArrayList();
        Iterator i$ = signatures.iterator();

        while(i$.hasNext()) {
            Signature signature = (Signature)i$.next();
            SignatureValidationTask validationExecutor = new SignatureValidationTask(signature);
            Future<SignatureValidationData> validationDataFuture = this.threadPoolManager.submit(validationExecutor);
            futures.add(validationDataFuture);
        }

        return futures;
    }

    private void extractValidatedSignatureErrors(List<Future<SignatureValidationData>> validationFutures) {
        logger.debug("Extracting errors from the signatures");
        Iterator i$ = validationFutures.iterator();

        while(i$.hasNext()) {
            Future validationFuture = (Future)i$.next();

            try {
                SignatureValidationData validationData = (SignatureValidationData)validationFuture.get();
                this.extractSignatureErrors(validationData);
            } catch (ExecutionException | InterruptedException var5) {
                logger.error("Error validating signatures on multiple threads: " + var5.getMessage());
                throw new TechnicalException("Error validating signatures on multiple threads: " + var5.getMessage(), var5);
            }
        }

    }

    public void setValidateManifest(boolean validateManifest) {
        this.validateManifest = validateManifest;
    }

    private void extractSignatureErrors(SignatureValidationData validationData) {
        logger.debug("Extracting signature errors for signature " + validationData.getSignatureId());
        this.signatureValidationData.add(validationData);
        SignatureValidationResult validationResult = validationData.getValidationResult();
        List<DigiDoc4JException> signatureErrors = validationResult.getErrors();
        this.errors.addAll(signatureErrors);
        this.warnings.addAll(validationResult.getWarnings());
    }

    private void extractManifestErrors(List<Signature> signatures) {
        logger.debug("Extracting manifest errors");
        this.manifestErrors = this.findManifestErrors(signatures);
        this.errors.addAll(this.manifestErrors);
    }

    private AsicValidationResult createValidationResult() {
        AsicValidationReportBuilder reportBuilder = new AsicValidationReportBuilder(this.signatureValidationData, this.manifestErrors);
        AsicValidationResult result = new AsicValidationResult();
        result.setErrors(this.errors);
        result.setWarnings(this.warnings);
        result.setContainerErrorsOnly(this.manifestErrors);
        result.setReportBuilder(reportBuilder);
        return result;
    }

    private List<DigiDoc4JException> findManifestErrors(List<Signature> signatures) {
        if (this.validateManifest && this.containerParseResult != null) {
            ManifestParser manifestParser = this.containerParseResult.getManifestParser();
            ArrayList manifestExceptions;
            if (manifestParser != null && manifestParser.containsManifestFile()) {
                manifestExceptions = new ArrayList();
                List<DSSDocument> detachedContents = this.containerParseResult.getDetachedContents();
                List<ManifestErrorMessage> manifestErrorMessageList = (new ManifestValidator(manifestParser, detachedContents, signatures)).validateDocument();
                Iterator i$ = manifestErrorMessageList.iterator();

                while(i$.hasNext()) {
                    ManifestErrorMessage manifestErrorMessage = (ManifestErrorMessage)i$.next();
                    manifestExceptions.add(new DigiDoc4JException(manifestErrorMessage.getErrorMessage(), manifestErrorMessage.getSignatureId()));
                }

                return manifestExceptions;
            } else {
                logger.error("Container is missing manifest.xml");
                manifestExceptions = new ArrayList();
                manifestExceptions.add(new UnsupportedFormatException("Container does not contain a manifest file"));
                return manifestExceptions;
            }
        } else {
            return Collections.emptyList();
        }
    }
}
