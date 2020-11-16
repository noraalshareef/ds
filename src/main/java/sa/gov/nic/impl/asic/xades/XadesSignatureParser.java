// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import sa.gov.nic.utils.Helper;
import eu.europa.esig.dss.x509.SignaturePolicy;
import org.apache.commons.lang3.StringUtils;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import org.slf4j.Logger;

public class XadesSignatureParser
{
    private static final Logger logger;
    
    public XadesSignature parse(final XadesValidationReportGenerator xadesReportGenerator) {
        XadesSignatureParser.logger.debug("Parsing XAdES signature");
        final XAdESSignature xAdESSignature = xadesReportGenerator.openDssSignature();
        final SignatureLevel signatureLevel = xAdESSignature.getDataFoundUpToLevel();
        XadesSignatureParser.logger.debug("Signature profile is " + signatureLevel);
        if (this.isEpesSignature(signatureLevel, xAdESSignature)) {
            XadesSignatureParser.logger.debug("Using EPES signature");
            return new EpesSignature(xadesReportGenerator);
        }
        if (this.isBesSignature(signatureLevel)) {
            XadesSignatureParser.logger.debug("Using BES signature");
            return new BesSignature(xadesReportGenerator);
        }
        if (this.isTimeMarkSignature(xAdESSignature)) {
            XadesSignatureParser.logger.debug("Using Time Mark signature");
            return new TimemarkSignature(xadesReportGenerator);
        }
        if (this.isTimestampArchiveSignature(signatureLevel)) {
            XadesSignatureParser.logger.debug("Using Time Stamp Archive signature");
            return new TimestampArchiveSignature(xadesReportGenerator);
        }
        XadesSignatureParser.logger.debug("Using Timestamp signature");
        return new TimestampSignature(xadesReportGenerator);
    }
    
    private boolean isEpesSignature(final SignatureLevel signatureLevel, final XAdESSignature xAdESSignature) {
        return this.isBesSignature(signatureLevel) && this.containsPolicyId(xAdESSignature);
    }
    
    private boolean isBesSignature(final SignatureLevel signatureLevel) {
        return signatureLevel == SignatureLevel.XAdES_BASELINE_B;
    }
    
    private boolean isTimestampArchiveSignature(final SignatureLevel signatureLevel) {
        return signatureLevel == SignatureLevel.XAdES_BASELINE_LTA || signatureLevel == SignatureLevel.XAdES_A;
    }
    
    private boolean containsPolicyId(final XAdESSignature xAdESSignature) {
        xAdESSignature.checkSignaturePolicy(new SignaturePolicyProvider());
        final SignaturePolicy policyId = xAdESSignature.getPolicyId();
        return policyId != null && StringUtils.isNotBlank((CharSequence)policyId.getIdentifier());
    }
    
    private boolean isTimeMarkSignature(final XAdESSignature xAdESSignature) {
        if (!this.containsPolicyId(xAdESSignature)) {
            return false;
        }
        final SignaturePolicy policyId = xAdESSignature.getPolicyId();
        final String identifier = Helper.getIdentifier(policyId.getIdentifier());
        return StringUtils.equals((CharSequence)"1.3.6.1.4.1.10015.1000.3.2.1", (CharSequence)identifier);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)XadesSignatureParser.class);
    }
}
