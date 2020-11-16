// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.security.cert.X509Certificate;
import java.util.List;
import org.slf4j.Logger;
import java.io.Serializable;

public class SignatureParameters implements Serializable
{
    private static final Logger logger;
    private SignatureProductionPlace productionPlace;
    private List<String> roles;
    private String signatureId;
    private DigestAlgorithm digestAlgorithm;
    private EncryptionAlgorithm encryptionAlgorithm;
    private SignatureProfile signatureProfile;
    private X509Certificate signingCertificate;
    
    public SignatureParameters() {
        this.productionPlace = new SignatureProductionPlace();
        this.roles = new ArrayList<String>();
    }
    
    @Deprecated
    public SignatureProductionPlace getProductionPlace() {
        return this.productionPlace;
    }
    
    public String getCity() {
        return this.productionPlace.getCity();
    }
    
    public void setCity(final String city) {
        this.productionPlace.setCity(city);
    }
    
    public String getStateOrProvince() {
        return this.productionPlace.getStateOrProvince();
    }
    
    public void setStateOrProvince(final String stateOrProvince) {
        this.productionPlace.setStateOrProvince(stateOrProvince);
    }
    
    public String getPostalCode() {
        return this.productionPlace.getPostalCode();
    }
    
    public void setPostalCode(final String postalCode) {
        this.productionPlace.setPostalCode(postalCode);
    }
    
    public String getCountry() {
        return this.productionPlace.getCountry();
    }
    
    public void setCountry(final String country) {
        this.productionPlace.setCountry(country);
    }
    
    public List<String> getRoles() {
        return this.roles;
    }
    
    @Deprecated
    public void setProductionPlace(final SignatureProductionPlace productionPlace) {
        this.productionPlace = productionPlace;
    }
    
    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }
    
    public void setSignatureId(final String signatureId) {
        SignatureParameters.logger.debug("Set signature id to " + signatureId);
        this.signatureId = signatureId;
    }
    
    public String getSignatureId() {
        return this.signatureId;
    }
    
    public void setDigestAlgorithm(final DigestAlgorithm algorithm) {
        this.digestAlgorithm = algorithm;
    }
    
    public DigestAlgorithm getDigestAlgorithm() {
        return this.digestAlgorithm;
    }
    
    public EncryptionAlgorithm getEncryptionAlgorithm() {
        return this.encryptionAlgorithm;
    }
    
    public void setEncryptionAlgorithm(final EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
    
    public SignatureProfile getSignatureProfile() {
        return this.signatureProfile;
    }
    
    public void setSignatureProfile(final SignatureProfile signatureProfile) {
        this.signatureProfile = signatureProfile;
    }
    
    public void setSigningCertificate(final X509Certificate signingCertificate) {
        this.signingCertificate = signingCertificate;
    }
    
    public X509Certificate getSigningCertificate() {
        return this.signingCertificate;
    }
    
    public SignatureParameters copy() {
        SignatureParameters.logger.debug("");
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        SignatureParameters copySignatureParameters = null;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            copySignatureParameters = (SignatureParameters)ois.readObject();
        }
        catch (Exception e) {
            SignatureParameters.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
        finally {
            IOUtils.closeQuietly((OutputStream)oos);
            IOUtils.closeQuietly((InputStream)ois);
            IOUtils.closeQuietly((OutputStream)bos);
        }
        return copySignatureParameters;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureParameters.class);
    }
}
