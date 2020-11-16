// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

public class Version
{
    public static final String VERSION;
    
    static {
        VERSION = Version.class.getPackage().getImplementationVersion();
    }
}
