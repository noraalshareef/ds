// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.X509Cert;
import eu.europa.esig.dss.SignerLocation;
import org.apache.commons.lang3.StringUtils;
import eu.europa.esig.dss.SignatureLevel;
import java.security.cert.X509Certificate;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import sa.gov.nic.impl.asic.ocsp.SKOnlineOCSPSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import sa.gov.nic.impl.asic.ocsp.OcspSourceBuilder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import sa.gov.nic.exceptions.OCSPRequestFailedException;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.impl.asic.asice.bdoc.BDocContainer;
import sa.gov.nic.impl.asic.asice.AsicEContainer;
import sa.gov.nic.impl.asic.asics.AsicSContainer;
import eu.europa.esig.dss.x509.CertificateSource;
import java.util.List;
import sa.gov.nic.Configuration;
import sa.gov.nic.impl.asic.asice.AsicESignature;
import sa.gov.nic.impl.asic.asice.AsicESignatureOpener;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignature;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignatureOpener;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.DataFile;
import java.util.Collection;
import eu.europa.esig.dss.xades.signature.DSSSignatureUtils;
import sa.gov.nic.EncryptionAlgorithm;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import sa.gov.nic.exceptions.InvalidSignatureException;
import sa.gov.nic.exceptions.ContainerWithoutFilesException;
import sa.gov.nic.exceptions.SignerCertificateRequiredException;
import sa.gov.nic.DataToSign;
import sa.gov.nic.exceptions.TechnicalException;
import sa.gov.nic.utils.Helper;
import sa.gov.nic.Signature;
import java.util.Date;
import sa.gov.nic.impl.asic.xades.XadesSigningDssFacade;
import org.slf4j.Logger;
import sa.gov.nic.impl.SignatureFinalizer;
import sa.gov.nic.SignatureBuilder;

public class AsicSignatureBuilder extends SignatureBuilder implements SignatureFinalizer
{
    private static final Logger logger;
    private static final int hexMaxlen = 10;
    protected transient XadesSigningDssFacade facade;
    private Date signingDate;
    private boolean isLTorLTAprofile;
    
    public AsicSignatureBuilder() {
        this.isLTorLTAprofile = false;
    }
    
    @Override
    protected Signature invokeSigningProcess() {
        AsicSignatureBuilder.logger.info("Signing asic container");
        this.signatureParameters.setSigningCertificate(this.signatureToken.getCertificate());
        final byte[] dataToSign = this.getDataToBeSigned();
        Signature result = null;
        byte[] signatureValue = null;
        try {
            signatureValue = this.signatureToken.sign(this.signatureParameters.getDigestAlgorithm(), dataToSign);
            result = this.finalizeSignature(signatureValue);
        }
        catch (TechnicalException e) {
            AsicSignatureBuilder.logger.warn("PROBLEM with signing: " + Helper.bytesToHex(dataToSign, 10) + " -> " + Helper.bytesToHex(signatureValue, 10));
        }
        return result;
    }
    
    @Override
    public DataToSign buildDataToSign() throws SignerCertificateRequiredException, ContainerWithoutFilesException {
        final byte[] dataToSign = this.getDataToBeSigned();
        return new DataToSign(dataToSign, this.signatureParameters, this);
    }
    
    @Override
    public Signature openAdESSignature(final byte[] signatureDocument) {
        if (signatureDocument == null) {
            AsicSignatureBuilder.logger.error("Signature cannot be empty");
            throw new InvalidSignatureException();
        }
        final InMemoryDocument document = new InMemoryDocument(signatureDocument);
        return this.createSignature((DSSDocument)document);
    }
    
    @Override
    public Signature finalizeSignature(byte[] signatureValueBytes) {
        if ((this.signatureParameters.getEncryptionAlgorithm() == EncryptionAlgorithm.ECDSA || this.isEcdsaCertificate()) && DSSSignatureUtils.isAsn1Encoded(signatureValueBytes)) {
            AsicSignatureBuilder.logger.debug("Finalizing signature ASN1: " + Helper.bytesToHex(signatureValueBytes, 10) + " [" + String.valueOf(signatureValueBytes.length) + "]");
            signatureValueBytes = DSSSignatureUtils.convertToXmlDSig(eu.europa.esig.dss.EncryptionAlgorithm.ECDSA, signatureValueBytes);
        }
        AsicSignatureBuilder.logger.debug("Finalizing signature XmlDSig: " + Helper.bytesToHex(signatureValueBytes, 10) + " [" + String.valueOf(signatureValueBytes.length) + "]");
        this.populateParametersForFinalizingSignature(signatureValueBytes);
        final Collection<DataFile> dataFilesToSign = this.getDataFiles();
        this.validateDataFilesToSign(dataFilesToSign);
        final DSSDocument signedDocument = this.facade.signDocument(signatureValueBytes, dataFilesToSign);
        return this.createSignature(signedDocument);
    }
    
    protected Signature createSignature(final DSSDocument signedDocument) {
        AsicSignatureBuilder.logger.debug("Opening signed document validator");
        final Configuration configuration = this.getConfiguration();
        DetachedContentCreator detachedContentCreator = null;
        try {
            detachedContentCreator = new DetachedContentCreator().populate(this.getDataFiles());
        }
        catch (Exception e) {
            AsicSignatureBuilder.logger.error("Error in datafile processing: " + e.getMessage());
            throw new DigiDoc4JException(e);
        }
        final List<DSSDocument> detachedContents = detachedContentCreator.getDetachedContentList();
        Signature signature = null;
        if (SignatureProfile.LT_TM.equals(this.signatureParameters.getSignatureProfile())) {
            final BDocSignatureOpener signatureOpener = new BDocSignatureOpener(detachedContents, configuration);
            final List<BDocSignature> signatureList = signatureOpener.parse(signedDocument);
            signature = signatureList.get(0);
            this.validateOcspResponse(((BDocSignature)signature).getOrigin());
        }
        else {
            final AsicESignatureOpener signatureOpener2 = new AsicESignatureOpener(detachedContents, configuration);
            final List<AsicESignature> signatureList2 = signatureOpener2.parse(signedDocument);
            signature = signatureList2.get(0);
        }
        AsicSignatureBuilder.policyDefinedByUser = null;
        AsicSignatureBuilder.logger.info("Signing asic successfully completed");
        return signature;
    }
    
    protected byte[] getDataToBeSigned() {
        AsicSignatureBuilder.logger.info("Getting data to sign");
        this.initSigningFacade();
        this.populateSignatureParameters();
        final Collection<DataFile> dataFilesToSign = this.getDataFiles();
        this.validateDataFilesToSign(dataFilesToSign);
        final byte[] dataToSign = this.facade.getDataToSign(dataFilesToSign);
        final String signatureId = this.facade.getSignatureId();
        this.signatureParameters.setSignatureId(signatureId);
        return dataToSign;
    }
    
    protected void populateSignatureParameters() {
        this.setDigestAlgorithm();
        this.setSigningCertificate();
        this.setEncryptionAlgorithm();
        this.setSignatureProfile();
        this.setSignerInformation();
        this.setSignatureId();
        this.setSignaturePolicy();
        this.setSigningDate();
        this.setTimeStampProviderSource();
    }
    
    protected void populateParametersForFinalizingSignature(final byte[] signatureValueBytes) {
        if (this.facade == null) {
            this.initSigningFacade();
            this.populateSignatureParameters();
        }
        final Configuration configuration = this.getConfiguration();
        this.facade.setCertificateSource((CertificateSource)configuration.getTSL());
        this.setOcspSource(signatureValueBytes);
    }
    
    protected void initSigningFacade() {
        if (this.facade == null) {
            this.facade = new XadesSigningDssFacade();
        }
    }
    
    protected Configuration getConfiguration() {
        if (this.container instanceof AsicSContainer) {
            return ((AsicSContainer)this.container).getConfiguration();
        }
        if (this.container instanceof AsicEContainer) {
            return ((AsicEContainer)this.container).getConfiguration();
        }
        return ((BDocContainer)this.container).getConfiguration();
    }
    
    protected List<DataFile> getDataFiles() {
        return this.container.getDataFiles();
    }
    
    protected void validateOcspResponse(final XadesSignature xadesSignature) {
        if (this.isBaselineSignatureProfile()) {
            return;
        }
        final List<BasicOCSPResp> ocspResponses = xadesSignature.getOcspResponses();
        if (ocspResponses == null || ocspResponses.isEmpty()) {
            AsicSignatureBuilder.logger.error("Signature does not contain OCSP response");
            throw new OCSPRequestFailedException(xadesSignature.getId());
        }
    }
    
    protected boolean isBaselineSignatureProfile() {
        return this.signatureParameters.getSignatureProfile() != null && (SignatureProfile.B_BES == this.signatureParameters.getSignatureProfile() || SignatureProfile.B_EPES == this.signatureParameters.getSignatureProfile());
    }
    
    protected void setOcspSource(final byte[] signatureValueBytes) {
        final SKOnlineOCSPSource ocspSource = OcspSourceBuilder.anOcspSource().withSignatureProfile(this.signatureParameters.getSignatureProfile()).withSignatureValue(signatureValueBytes).withConfiguration(this.getConfiguration()).build();
        this.facade.setOcspSource((OCSPSource)ocspSource);
    }
    
    protected void setTimeStampProviderSource() {
        final Configuration configuration = this.getConfiguration();
        final OnlineTSPSource tspSource = new OnlineTSPSource(this.getTspSource(configuration));
        final SkDataLoader dataLoader = SkDataLoader.createTimestampDataLoader(configuration);
        dataLoader.setUserAgentSignatureProfile(this.signatureParameters.getSignatureProfile());
        tspSource.setDataLoader((DataLoader)dataLoader);
        this.facade.setTspSource((TSPSource)tspSource);
    }
    
    protected void setDigestAlgorithm() {
        if (this.signatureParameters.getDigestAlgorithm() == null) {
            final Configuration configuration = this.getConfiguration();
            this.signatureParameters.setDigestAlgorithm(configuration.getSignatureDigestAlgorithm());
        }
        this.facade.setSignatureDigestAlgorithm(this.signatureParameters.getDigestAlgorithm());
    }
    
    protected void setEncryptionAlgorithm() {
        if (this.signatureParameters.getEncryptionAlgorithm() == EncryptionAlgorithm.ECDSA || this.isEcdsaCertificate()) {
            AsicSignatureBuilder.logger.debug("Using ECDSA encryption algorithm");
            this.signatureParameters.setEncryptionAlgorithm(EncryptionAlgorithm.ECDSA);
            this.facade.setEncryptionAlgorithm(eu.europa.esig.dss.EncryptionAlgorithm.ECDSA);
        }
        else {
            this.signatureParameters.setEncryptionAlgorithm(EncryptionAlgorithm.RSA);
            this.facade.setEncryptionAlgorithm(eu.europa.esig.dss.EncryptionAlgorithm.RSA);
        }
    }
    
    protected boolean isEcdsaCertificate() {
        final X509Certificate certificate = this.signatureParameters.getSigningCertificate();
        final String algorithm = certificate.getPublicKey().getAlgorithm();
        return algorithm.equals("EC") || algorithm.equals("ECC");
    }
    
    protected void setSignatureProfile() {
        if (this.signatureParameters.getSignatureProfile() != null) {
            this.setSignatureProfile(this.signatureParameters.getSignatureProfile());
        }
        else {
            final SignatureProfile signatureProfile = this.getConfiguration().getSignatureProfile();
            this.setSignatureProfile(signatureProfile);
            this.signatureParameters.setSignatureProfile(signatureProfile);
        }
    }
    
    protected void setSignatureProfile(final SignatureProfile profile) {
        switch (profile) {
            case B_BES: {
                this.facade.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
                break;
            }
            case B_EPES: {
                this.facade.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
                break;
            }
            case LTA: {
                this.isLTorLTAprofile = true;
                this.facade.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);
                break;
            }
            default: {
                this.isLTorLTAprofile = true;
                this.facade.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LT);
                break;
            }
        }
    }
    
    protected void setSignaturePolicy() {
        if (AsicSignatureBuilder.policyDefinedByUser != null && SignatureBuilder.isDefinedAllPolicyValues()) {
            this.facade.setSignaturePolicy(AsicSignatureBuilder.policyDefinedByUser);
        }
    }
    
    protected void setSignatureId() {
        if (StringUtils.isNotBlank((CharSequence)this.signatureParameters.getSignatureId())) {
            this.facade.setSignatureId(this.signatureParameters.getSignatureId());
        }
    }
    
    protected void setSignerInformation() {
        AsicSignatureBuilder.logger.debug("Adding signer information");
        final List<String> signerRoles = this.signatureParameters.getRoles();
        if (!StringUtils.isEmpty((CharSequence)this.signatureParameters.getCity()) || !StringUtils.isEmpty((CharSequence)this.signatureParameters.getStateOrProvince()) || !StringUtils.isEmpty((CharSequence)this.signatureParameters.getPostalCode()) || !StringUtils.isEmpty((CharSequence)this.signatureParameters.getCountry())) {
            final SignerLocation signerLocation = new SignerLocation();
            if (!StringUtils.isEmpty((CharSequence)this.signatureParameters.getCity())) {
                signerLocation.setLocality(this.signatureParameters.getCity());
            }
            if (!StringUtils.isEmpty((CharSequence)this.signatureParameters.getStateOrProvince())) {
                signerLocation.setStateOrProvince(this.signatureParameters.getStateOrProvince());
            }
            if (!StringUtils.isEmpty((CharSequence)this.signatureParameters.getPostalCode())) {
                signerLocation.setPostalCode(this.signatureParameters.getPostalCode());
            }
            if (!StringUtils.isEmpty((CharSequence)this.signatureParameters.getCountry())) {
                signerLocation.setCountry(this.signatureParameters.getCountry());
            }
            this.facade.setSignerLocation(signerLocation);
        }
        this.facade.setSignerRoles(signerRoles);
    }
    
    protected void setSigningCertificate() {
        final X509Certificate signingCert = this.signatureParameters.getSigningCertificate();
        this.facade.setSigningCertificate(signingCert);
    }
    
    protected void setSigningDate() {
        if (this.signingDate == null) {
            this.signingDate = new Date();
        }
        this.facade.setSigningDate(this.signingDate);
        AsicSignatureBuilder.logger.debug("Signing date is going to be " + this.signingDate);
    }
    
    protected void validateDataFilesToSign(final Collection<DataFile> dataFilesToSign) {
        if (dataFilesToSign.isEmpty()) {
            AsicSignatureBuilder.logger.error("Container does not contain any data files");
            throw new ContainerWithoutFilesException();
        }
    }
    
    protected boolean isTimeMarkProfile() {
        return this.signatureParameters.getSignatureProfile() != null && this.signatureParameters.getSignatureProfile() == SignatureProfile.LT_TM;
    }
    
    protected boolean isTimeStampProfile() {
        return this.signatureParameters.getSignatureProfile() != null && this.signatureParameters.getSignatureProfile() == SignatureProfile.LT;
    }
    
    protected boolean isEpesProfile() {
        return this.signatureParameters.getSignatureProfile() != null && this.signatureParameters.getSignatureProfile() == SignatureProfile.B_EPES;
    }
    
    private String getTspSource(final Configuration configuration) {
        if (this.isLTorLTAprofile) {
            final X509Cert x509Cert = new X509Cert(this.signatureParameters.getSigningCertificate());
            final String certCountry = x509Cert.getSubjectName(X509Cert.SubjectName.C);
            final String tspSourceByCountry = configuration.getTspSourceByCountry(certCountry);
            if (StringUtils.isNotBlank((CharSequence)tspSourceByCountry)) {
                return tspSourceByCountry;
            }
        }
        return configuration.getTspSource();
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)AsicSignatureBuilder.class);
    }
}
