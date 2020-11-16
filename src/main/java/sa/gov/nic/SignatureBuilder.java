// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import java.util.HashMap;
import org.slf4j.LoggerFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import sa.gov.nic.exceptions.ContainerWithoutFilesException;
import sa.gov.nic.exceptions.SignerCertificateRequiredException;
import sa.gov.nic.exceptions.SignatureTokenMissingException;
import sa.gov.nic.exceptions.TechnicalException;
import org.apache.commons.lang3.StringUtils;
import sa.gov.nic.exceptions.NotSupportedException;
import sa.gov.nic.impl.ddoc.DDocSignatureBuilder;
import sa.gov.nic.impl.asic.AsicSignatureBuilder;
import sa.gov.nic.impl.asic.asice.AsicESignatureBuilder;
import sa.gov.nic.impl.asic.asice.bdoc.BDocSignatureBuilder;
import eu.europa.esig.dss.Policy;
import java.util.Map;
import org.slf4j.Logger;
import java.io.Serializable;

public abstract class SignatureBuilder implements Serializable
{
    private static final Logger logger;
    protected static Map<String, Class<? extends SignatureBuilder>> customSignatureBuilders;
    protected static Policy policyDefinedByUser;
    protected SignatureParameters signatureParameters;
    protected SignatureToken signatureToken;
    protected Container container;
    
    public SignatureBuilder() {
        this.signatureParameters = new SignatureParameters();
    }
    
    public static SignatureBuilder aSignature(final Container container) {
        final SignatureBuilder builder = createBuilder(container);
        builder.setContainer(container);
        return builder;
    }
    
    private static SignatureBuilder createBuilder(final Container container) {
        final String containerType = container.getType();
        if (isCustomContainerType(containerType)) {
            return createCustomSignatureBuilder(containerType);
        }
        if (isContainerType(containerType, "BDOC")) {
            return new BDocSignatureBuilder();
        }
        if (isContainerType(containerType, "ASICE")) {
            return new AsicESignatureBuilder();
        }
        if (isContainerType(containerType, "ASICS")) {
            return new AsicSignatureBuilder();
        }
        if (isContainerType(containerType, "DDOC")) {
            return new DDocSignatureBuilder();
        }
        SignatureBuilder.logger.error("Unknown container type: " + container.getType());
        throw new NotSupportedException("Unknown container type: " + container.getType());
    }
    
    public static <T extends SignatureBuilder> void setSignatureBuilderForContainerType(final String containerType, final Class<T> signatureBuilderClass) {
        SignatureBuilder.customSignatureBuilders.put(containerType, signatureBuilderClass);
    }
    
    public static void removeCustomSignatureBuilders() {
        SignatureBuilder.customSignatureBuilders.clear();
    }
    
    private static boolean isCustomContainerType(final String containerType) {
        return SignatureBuilder.customSignatureBuilders.containsKey(containerType);
    }
    
    private static boolean isContainerType(final String containerType, final String ddocContainerType) {
        return StringUtils.equalsIgnoreCase((CharSequence)ddocContainerType, (CharSequence)containerType);
    }
    
    private static SignatureBuilder createCustomSignatureBuilder(final String containerType) {
        final Class<? extends SignatureBuilder> builderClass = SignatureBuilder.customSignatureBuilders.get(containerType);
        try {
            SignatureBuilder.logger.debug("Instantiating signature builder class " + builderClass.getName() + " for container type " + containerType);
            return (SignatureBuilder)builderClass.newInstance();
        }
        catch (ReflectiveOperationException e) {
            SignatureBuilder.logger.error("Unable to instantiate custom signature builder class " + builderClass.getName() + " for type " + containerType);
            throw new TechnicalException("Unable to instantiate custom signature builder class " + builderClass.getName() + " for type " + containerType, e);
        }
    }
    
    protected static boolean isDefinedAllPolicyValues() {
        return StringUtils.isNotBlank((CharSequence)SignatureBuilder.policyDefinedByUser.getId()) && SignatureBuilder.policyDefinedByUser.getDigestValue() != null && StringUtils.isNotBlank((CharSequence)SignatureBuilder.policyDefinedByUser.getQualifier()) && SignatureBuilder.policyDefinedByUser.getDigestAlgorithm() != null && StringUtils.isNotBlank((CharSequence)SignatureBuilder.policyDefinedByUser.getSpuri());
    }
    
    public Signature invokeSigning() throws SignatureTokenMissingException {
        if (this.signatureToken == null) {
            SignatureBuilder.logger.error("Cannot invoke signing without signature token. Add 'withSignatureToken()' method call or call 'buildDataToSign() instead.'");
            throw new SignatureTokenMissingException();
        }
        return this.invokeSigningProcess();
    }
    
    protected abstract Signature invokeSigningProcess();
    
    public abstract DataToSign buildDataToSign() throws SignerCertificateRequiredException, ContainerWithoutFilesException;
    
    public abstract Signature openAdESSignature(final byte[] p0);
    
    public SignatureBuilder withCity(final String cityName) {
        this.signatureParameters.setCity(cityName);
        return this;
    }
    
    public SignatureBuilder withStateOrProvince(final String stateOrProvince) {
        this.signatureParameters.setStateOrProvince(stateOrProvince);
        return this;
    }
    
    public SignatureBuilder withPostalCode(final String postalCode) {
        this.signatureParameters.setPostalCode(postalCode);
        return this;
    }
    
    public SignatureBuilder withCountry(final String country) {
        this.signatureParameters.setCountry(country);
        return this;
    }
    
    public SignatureBuilder withRoles(final String... roles) {
        if (this.signatureParameters.getRoles() == null) {
            this.signatureParameters.setRoles(Arrays.asList(roles));
        }
        else {
            this.signatureParameters.getRoles().addAll(Arrays.asList(roles));
        }
        return this;
    }
    
    public SignatureBuilder withSignatureDigestAlgorithm(final DigestAlgorithm digestAlgorithm) {
        this.signatureParameters.setDigestAlgorithm(digestAlgorithm);
        return this;
    }
    
    public SignatureBuilder withSignatureProfile(final SignatureProfile signatureProfile) {
        if (SignatureBuilder.policyDefinedByUser != null && isDefinedAllPolicyValues() && signatureProfile != SignatureProfile.LT_TM) {
            SignatureBuilder.logger.debug("policyDefinedByUser:" + SignatureBuilder.policyDefinedByUser.toString());
            SignatureBuilder.logger.debug("signatureProfile:" + signatureProfile.toString());
            throw new NotSupportedException("Can't define signature policy if it's not LT_TM signature profile ");
        }
        this.signatureParameters.setSignatureProfile(signatureProfile);
        return this;
    }
    
    public SignatureBuilder withSigningCertificate(final X509Certificate certificate) {
        this.signatureParameters.setSigningCertificate(certificate);
        return this;
    }
    
    public SignatureBuilder withSignatureId(final String signatureId) {
        this.signatureParameters.setSignatureId(signatureId);
        return this;
    }
    
    public SignatureBuilder withSignatureToken(final SignatureToken signatureToken) {
        this.signatureToken = signatureToken;
        return this;
    }
    
    public SignatureBuilder withEncryptionAlgorithm(final EncryptionAlgorithm encryptionAlgorithm) {
        this.signatureParameters.setEncryptionAlgorithm(encryptionAlgorithm);
        return this;
    }
    
    protected void setContainer(final Container container) {
        this.container = container;
    }
    
    public SignatureBuilder withOwnSignaturePolicy(final Policy signaturePolicy) {
        if (this.signatureParameters.getSignatureProfile() != null && this.signatureParameters.getSignatureProfile() != SignatureProfile.LT_TM) {
            throw new NotSupportedException("Can't define signature policy if it's not LT_TM signature profile. Define it first. ");
        }
        SignatureBuilder.policyDefinedByUser = signaturePolicy;
        return this;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureBuilder.class);
        SignatureBuilder.customSignatureBuilders = new HashMap<String, Class<? extends SignatureBuilder>>();
    }
}
