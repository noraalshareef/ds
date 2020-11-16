// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import eu.europa.esig.dss.InMemoryDocument;
import org.w3c.dom.Node;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.x509.crl.ListCRLSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import java.util.Date;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.Policy;
import eu.europa.esig.dss.BLevelParameters;
import eu.europa.esig.dss.SignerLocation;
import eu.europa.esig.dss.EncryptionAlgorithm;
import sa.gov.nic.DigestAlgorithm;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import eu.europa.esig.dss.x509.CertificateToken;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import eu.europa.esig.dss.DSSException;
import sa.gov.nic.exceptions.TechnicalException;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.impl.asic.DetachedContentCreator;
import sa.gov.nic.DataFile;
import java.util.Collection;
import sa.gov.nic.impl.asic.SKCommonCertificateVerifier;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.DSSDocument;
import java.util.List;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.slf4j.Logger;

public class XadesSigningDssFacade
{
    private static final Logger logger;
    private XAdESService xAdESService;
    private XAdESSignatureParameters xAdESSignatureParameters;
    private List<DSSDocument> detachedContentList;
    private CertificateVerifier certificateVerifier;
    
    public XadesSigningDssFacade() {
        this.xAdESSignatureParameters = new XAdESSignatureParameters();
        this.detachedContentList = null;
        this.certificateVerifier = (CertificateVerifier)new SKCommonCertificateVerifier();
        this.initDefaultXadesParameters();
        this.initCertificateVerifier();
        this.initXadesMultipleService();
    }
    
    public byte[] getDataToSign(final Collection<DataFile> dataFiles) {
        XadesSigningDssFacade.logger.debug("Getting data to sign from DSS");
        DetachedContentCreator detachedContentCreator = null;
        try {
            detachedContentCreator = new DetachedContentCreator().populate(dataFiles);
        }
        catch (Exception e) {
            XadesSigningDssFacade.logger.error("Error in datafiles processing: " + e.getMessage());
            throw new DigiDoc4JException(e);
        }
        this.detachedContentList = detachedContentCreator.getDetachedContentList();
        this.xAdESSignatureParameters.setDetachedContents((List)this.detachedContentList);
        XadesSigningDssFacade.logger.debug("Signature parameters: " + this.xAdESSignatureParameters.toString());
        final ToBeSigned dataToSign = this.xAdESService.getDataToSign((List)this.detachedContentList, this.xAdESSignatureParameters);
        XadesSigningDssFacade.logger.debug("Got data to sign from DSS");
        return dataToSign.getBytes();
    }
    
    public DSSDocument signDocument(final byte[] signatureValue, final Collection<DataFile> dataFiles) {
        XadesSigningDssFacade.logger.debug("Signing document with DSS");
        if (this.detachedContentList == null) {
            DetachedContentCreator detachedContentCreator = null;
            try {
                detachedContentCreator = new DetachedContentCreator().populate(dataFiles);
            }
            catch (Exception e) {
                XadesSigningDssFacade.logger.error("Error in datafiles processing: " + e.getMessage());
                throw new DigiDoc4JException(e);
            }
            this.detachedContentList = detachedContentCreator.getDetachedContentList();
        }
        XadesSigningDssFacade.logger.debug("Signature parameters: " + this.xAdESSignatureParameters.toString());
        final SignatureValue dssSignatureValue = new SignatureValue(this.xAdESSignatureParameters.getSignatureAlgorithm(), signatureValue);
        DSSDocument signedDocument;
        try {
            signedDocument = this.xAdESService.signDocument((List)this.detachedContentList, this.xAdESSignatureParameters, dssSignatureValue);
        }
        catch (DSSException e2) {
            XadesSigningDssFacade.logger.warn("Signing document in DSS failed:" + e2.getMessage());
            throw new TechnicalException("Got error in signing process: ", (Throwable)e2);
        }
        final DSSDocument correctedSignedDocument = this.surroundWithXadesXmlTag(signedDocument);
        return correctedSignedDocument;
    }
    
    @Deprecated
    public DSSDocument extendSignature(final DSSDocument xadesSignature, final DSSDocument detachedContent) {
        XadesSigningDssFacade.logger.debug("Extending signature with DSS");
        this.xAdESSignatureParameters.setDetachedContents((List)Arrays.asList(detachedContent));
        final DSSDocument extendedSignature = this.xAdESService.extendDocument(xadesSignature, this.xAdESSignatureParameters);
        XadesSigningDssFacade.logger.debug("Finished extending signature with DSS");
        return extendedSignature;
    }
    
    public DSSDocument extendSignature(final DSSDocument xadesSignature, final List<DSSDocument> detachedContents) {
        XadesSigningDssFacade.logger.debug("Extending signature with DSS");
        this.xAdESSignatureParameters.setDetachedContents((List)detachedContents);
        final DSSDocument extendedSignature = this.xAdESService.extendDocument(xadesSignature, this.xAdESSignatureParameters);
        XadesSigningDssFacade.logger.debug("Finished extending signature with DSS");
        return extendedSignature;
    }
    
    public void setSigningCertificate(final X509Certificate certificate) {
        final CertificateToken signingCertificate = new CertificateToken(certificate);
        this.xAdESSignatureParameters.setSigningCertificate(signingCertificate);
    }
    
    public void setOcspSource(final OCSPSource ocspSource) {
        this.certificateVerifier.setOcspSource(ocspSource);
    }
    
    public void setCertificateSource(final CertificateSource certificateSource) {
        this.certificateVerifier.setTrustedCertSource(certificateSource);
    }
    
    public void setSignatureDigestAlgorithm(final DigestAlgorithm digestAlgorithm) {
        this.xAdESSignatureParameters.setDigestAlgorithm(digestAlgorithm.getDssDigestAlgorithm());
    }
    
    public void setEncryptionAlgorithm(final EncryptionAlgorithm encryptionAlgorithm) {
        this.xAdESSignatureParameters.setEncryptionAlgorithm(encryptionAlgorithm);
    }
    
    public void setSignerLocation(final SignerLocation signerLocation) {
        this.xAdESSignatureParameters.bLevel().setSignerLocation(signerLocation);
    }
    
    public void setSignerRoles(final Collection<String> signerRoles) {
        final BLevelParameters bLevelParameters = this.xAdESSignatureParameters.bLevel();
        for (final String signerRole : signerRoles) {
            bLevelParameters.addClaimedSignerRole(signerRole);
        }
    }
    
    public void setSignaturePolicy(final Policy signaturePolicy) {
        this.xAdESSignatureParameters.bLevel().setSignaturePolicy(signaturePolicy);
    }
    
    public void setSignatureLevel(final SignatureLevel signatureLevel) {
        this.xAdESSignatureParameters.setSignatureLevel(signatureLevel);
    }
    
    public String getSignatureId() {
        return this.xAdESSignatureParameters.getDeterministicId();
    }
    
    public void setSignatureId(final String signatureId) {
        XadesSigningDssFacade.logger.debug("Setting deterministic id: " + signatureId);
        this.xAdESSignatureParameters.setDeterministicId(signatureId);
    }
    
    public void setSigningDate(final Date signingDate) {
        this.xAdESSignatureParameters.getBLevelParams().setSigningDate(signingDate);
    }
    
    public void setEn319132(final boolean isSigningCertificateV2) {
        this.xAdESSignatureParameters.setEn319132(isSigningCertificateV2);
    }
    
    public void getEn319132() {
        this.xAdESSignatureParameters.isEn319132();
    }
    
    public void setTspSource(final TSPSource tspSource) {
        this.xAdESService.setTspSource(tspSource);
    }
    
    private void initDefaultXadesParameters() {
        this.xAdESSignatureParameters.clearCertificateChain();
        this.xAdESSignatureParameters.getBLevelParams().setSigningDate(new Date());
        this.xAdESSignatureParameters.setSignaturePackaging(SignaturePackaging.DETACHED);
        this.xAdESSignatureParameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LT);
        this.xAdESSignatureParameters.setDigestAlgorithm(eu.europa.esig.dss.DigestAlgorithm.SHA256);
        this.xAdESSignatureParameters.setSigningCertificateDigestMethod(eu.europa.esig.dss.DigestAlgorithm.SHA256);
        this.xAdESSignatureParameters.setEn319132(false);
    }
    
    private void initCertificateVerifier() {
        this.certificateVerifier.setCrlSource((CRLSource)null);
        this.certificateVerifier.setSignatureCRLSource((ListCRLSource)null);
    }
    
    private void initXadesMultipleService() {
        this.xAdESService = new XAdESService(this.certificateVerifier);
    }
    
    private DSSDocument surroundWithXadesXmlTag(final DSSDocument signedDocument) {
        XadesSigningDssFacade.logger.debug("Surrounding signature document with xades tag");
        final Document signatureDom = DomUtils.buildDOM(signedDocument);
        final Element signatureElement = signatureDom.getDocumentElement();
        final Document document = XmlDomCreator.createDocument("http://uri.etsi.org/02918/v1.2.1#", "asic:XAdESSignatures", signatureElement);
        final byte[] documentBytes = DSSXMLUtils.serializeNode((Node)document);
        return (DSSDocument)new InMemoryDocument(documentBytes);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)XadesSigningDssFacade.class);
    }
}
