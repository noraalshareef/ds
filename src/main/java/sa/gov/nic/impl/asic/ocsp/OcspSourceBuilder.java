// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.ocsp;

import sa.gov.nic.impl.asic.SkDataLoader;
import sa.gov.nic.SignatureProfile;
import sa.gov.nic.Configuration;

public class OcspSourceBuilder
{
    private Configuration configuration;
    private byte[] signatureValue;
    private SignatureProfile signatureProfile;
    
    public static OcspSourceBuilder anOcspSource() {
        return new OcspSourceBuilder();
    }
    
    public SKOnlineOCSPSource build() {
        SKOnlineOCSPSource ocspSource;
        if (this.signatureProfile == SignatureProfile.LT_TM) {
            ocspSource = new BDocTMOcspSource(this.configuration, this.signatureValue);
        }
        else {
            ocspSource = new BDocTSOcspSource(this.configuration);
        }
        final SkDataLoader dataLoader = SkDataLoader.createOcspDataLoader(this.configuration);
        dataLoader.setUserAgentSignatureProfile(this.signatureProfile);
        ocspSource.setDataLoader(dataLoader);
        return ocspSource;
    }
    
    public OcspSourceBuilder withConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
    
    public OcspSourceBuilder withSignatureValue(final byte[] signatureValue) {
        this.signatureValue = signatureValue;
        return this;
    }
    
    public OcspSourceBuilder withSignatureProfile(final SignatureProfile signatureProfile) {
        this.signatureProfile = signatureProfile;
        return this;
    }
}
