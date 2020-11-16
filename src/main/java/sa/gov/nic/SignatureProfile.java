// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

public enum SignatureProfile
{
    LT_TM, 
    LT, 
    LTA, 
    B_BES, 
    B_EPES;
    
    public static SignatureProfile findByProfile(final String profile) {
        for (final SignatureProfile signatureProfile : values()) {
            if (signatureProfile.name().equals(profile)) {
                return signatureProfile;
            }
        }
        return null;
    }
}
