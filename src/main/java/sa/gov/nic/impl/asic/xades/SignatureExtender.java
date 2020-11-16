// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.Policy;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignatureBuilder;
import sa.gov.nic.exceptions.NotSupportedException;
import eu.europa.esig.dss.client.http.DataLoader;
import sa.gov.nic.impl.asic.SkDataLoader;
import sa.gov.nic.impl.asic.ocsp.SKOnlineOCSPSource;
import sa.gov.nic.impl.asic.ocsp.OcspSourceBuilder;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import sa.gov.nic.impl.asic.AsicSignature;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import eu.europa.esig.dss.x509.CertificateSource;

import java.util.ArrayList;
import sa.gov.nic.Signature;
import java.util.List;
import eu.europa.esig.dss.DSSDocument;
import sa.gov.nic.Configuration;
import java.util.Set;
import sa.gov.nic.SignatureProfile;
import java.util.Map;
import org.slf4j.Logger;

public class SignatureExtender
{
    private static final Logger logger;
    private static final Map<SignatureProfile, Set<SignatureProfile>> possibleExtensions;
    private Configuration configuration;
    private DSSDocument detachedContent;
    private List<DSSDocument> detachedContents;
    private XadesSigningDssFacade extendingFacade;
    
    public SignatureExtender(final Configuration configuration, final DSSDocument detachedContent) {
        this.configuration = configuration;
        this.detachedContent = detachedContent;
        this.extendingFacade = new XadesSigningDssFacade();
    }
    
    public SignatureExtender(final Configuration configuration, final List<DSSDocument> detachedContent) {
        this.configuration = configuration;
        this.detachedContents = detachedContent;
        this.extendingFacade = new XadesSigningDssFacade();
    }
    
    public List<DSSDocument> extend(final List<Signature> signaturesToExtend, final SignatureProfile profile) {
        SignatureExtender.logger.debug("Extending signatures to " + profile);
        this.validatePossibilityToExtendTo(signaturesToExtend, profile);
        this.prepareExtendingFacade(profile);
        final List<DSSDocument> extendedSignatures = new ArrayList<DSSDocument>();
        for (final Signature signature : signaturesToExtend) {
            final DSSDocument extendedSignature = this.extendSignature(signature, profile);
            extendedSignatures.add(extendedSignature);
        }
        SignatureExtender.logger.debug("Finished extending signatures");
        return extendedSignatures;
    }
    
    private void prepareExtendingFacade(final SignatureProfile profile) {
        this.extendingFacade.setCertificateSource((CertificateSource)this.configuration.getTSL());
        final OnlineTSPSource tspSource = this.createTimeStampProviderSource(profile);
        this.extendingFacade.setTspSource((TSPSource)tspSource);
        final SignatureLevel signatureLevel = this.getSignatureLevel(profile);
        this.extendingFacade.setSignatureLevel(signatureLevel);
        this.setSignaturePolicy(profile);
    }
    
    private DSSDocument extendSignature(final Signature signature, final SignatureProfile profile) {
        final OCSPSource ocspSource = this.createOcspSource(profile, ((AsicSignature)signature).getOrigin().getSignatureValue());
        this.extendingFacade.setOcspSource(ocspSource);
        final DSSDocument signatureDocument = ((AsicSignature)signature).getSignatureDocument();
        return this.extendingFacade.extendSignature(signatureDocument, this.detachedContents);
    }
    
    private OCSPSource createOcspSource(final SignatureProfile profile, final byte[] signatureValue) {
        final SKOnlineOCSPSource ocspSource = OcspSourceBuilder.anOcspSource().withSignatureProfile(profile).withSignatureValue(signatureValue).withConfiguration(this.configuration).build();
        return (OCSPSource)ocspSource;
    }
    
    private OnlineTSPSource createTimeStampProviderSource(final SignatureProfile profile) {
        final OnlineTSPSource tspSource = new OnlineTSPSource(this.configuration.getTspSource());
        final SkDataLoader dataLoader = SkDataLoader.createTimestampDataLoader(this.configuration);
        dataLoader.setUserAgentSignatureProfile(profile);
        tspSource.setDataLoader((DataLoader)dataLoader);
        return tspSource;
    }
    
    private SignatureLevel getSignatureLevel(final SignatureProfile profile) {
        if (profile == SignatureProfile.LT || profile == SignatureProfile.LT_TM) {
            return SignatureLevel.XAdES_BASELINE_LT;
        }
        if (profile == SignatureProfile.LTA) {
            return SignatureLevel.XAdES_BASELINE_LTA;
        }
        SignatureExtender.logger.error("Extending signature to " + profile + " is not supported");
        throw new NotSupportedException("Extending signature to " + profile + " is not supported");
    }
    
    private void setSignaturePolicy(final SignatureProfile profile) {
        if (profile == SignatureProfile.LT_TM) {
            final Policy signaturePolicy = BDocSignatureBuilder.createBDocSignaturePolicy();
            this.extendingFacade.setSignaturePolicy(signaturePolicy);
        }
    }
    
    private void validatePossibilityToExtendTo(final List<Signature> signatures, final SignatureProfile profile) {
        SignatureExtender.logger.debug("Validating if it's possible to extend all the signatures to " + profile);
        for (final Signature signature : signatures) {
            if (!this.canExtendSignatureToProfile(signature, profile)) {
                final String message = "It is not possible to extend " + signature.getProfile() + " signature to " + signature.getProfile() + ".";
                SignatureExtender.logger.error(message);
                throw new NotSupportedException(message);
            }
        }
    }
    
    private boolean canExtendSignatureToProfile(final Signature signature, final SignatureProfile profile) {
        return SignatureExtender.possibleExtensions.get(signature.getProfile()).contains(profile);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureExtender.class);
        (possibleExtensions = new HashMap<SignatureProfile, Set<SignatureProfile>>(5)).put(SignatureProfile.B_BES, new HashSet<SignatureProfile>(Arrays.asList(SignatureProfile.LT, SignatureProfile.LTA)));
        SignatureExtender.possibleExtensions.put(SignatureProfile.B_EPES, new HashSet<SignatureProfile>(Collections.singletonList(SignatureProfile.LT_TM)));
        SignatureExtender.possibleExtensions.put(SignatureProfile.LT, new HashSet<SignatureProfile>(Collections.singletonList(SignatureProfile.LTA)));
        SignatureExtender.possibleExtensions.put(SignatureProfile.LT_TM, Collections.emptySet());
        SignatureExtender.possibleExtensions.put(SignatureProfile.LTA, Collections.emptySet());
    }
}
