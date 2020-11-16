// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import java.util.Date;
import java.util.List;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.exceptions.TimestampAndOcspResponseTimeDeltaTooLargeException;
import sa.gov.nic.utils.DateUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import eu.europa.esig.dss.validation.TimestampToken;
import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;

public class TimestampSignatureValidator extends TimemarkSignatureValidator
{
    private final Logger log;
    private Configuration configuration;
    
    public TimestampSignatureValidator(final XadesSignature signature, final Configuration configuration) {
        super(signature);
        this.log = LoggerFactory.getLogger((Class)TimestampSignatureValidator.class);
        this.configuration = configuration;
    }
    
    @Override
    protected void populateValidationErrors() {
        super.populateValidationErrors();
        this.addSigningTimeErrors();
    }
    
    private void addSigningTimeErrors() {
        final XAdESSignature signature = this.getDssSignature();
        final List<TimestampToken> signatureTimestamps = (List<TimestampToken>)signature.getSignatureTimestamps();
        if (signatureTimestamps == null || signatureTimestamps.isEmpty()) {
            return;
        }
        final Date timestamp = signatureTimestamps.get(0).getGenerationTime();
        if (timestamp == null) {
            return;
        }
        final List<BasicOCSPResp> ocspResponses = (List<BasicOCSPResp>)signature.getOCSPSource().getContainedOCSPResponses();
        if (ocspResponses == null || ocspResponses.isEmpty()) {
            return;
        }
        final Date ocspTime = ocspResponses.get(0).getProducedAt();
        if (ocspTime == null) {
            return;
        }
        final int deltaLimit = this.configuration.getRevocationAndTimestampDeltaInMinutes();
        final long differenceInMinutes = DateUtils.differenceInMinutes(timestamp, ocspTime);
        this.log.debug("Difference in minutes: <{}>", (Object)differenceInMinutes);
        if (!DateUtils.isInRangeMinutes(timestamp, ocspTime, deltaLimit)) {
            this.log.error("The difference between the OCSP response production time and the signature timestamp is too large <{} minutes>", (Object)differenceInMinutes);
            this.addValidationError(new TimestampAndOcspResponseTimeDeltaTooLargeException());
        }
        else if (this.configuration.getAllowedTimestampAndOCSPResponseDeltaInMinutes() < differenceInMinutes && differenceInMinutes < deltaLimit) {
            this.log.warn("The difference (in minutes) between the OCSP response production time and the signature timestamp is in allowable range (<{}>, allowed maximum <{}>)", (Object)differenceInMinutes, (Object)deltaLimit);
            this.addValidationWarning(new DigiDoc4JException("The difference between the OCSP response time and the signature timestamp is in allowable range"));
        }
    }
}
