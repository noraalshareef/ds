// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.Serializable;

public class SignatureProductionPlace implements Serializable
{
    private static final Logger logger;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String country;
    
    public SignatureProductionPlace() {
    }
    
    public SignatureProductionPlace(final String city, final String stateOrProvince, final String postalCode, final String country) {
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    public String getCity() {
        return this.city;
    }
    
    public void setCity(final String city) {
        SignatureProductionPlace.logger.debug("City: " + city);
        this.city = city;
    }
    
    public String getStateOrProvince() {
        return this.stateOrProvince;
    }
    
    public void setStateOrProvince(final String stateOrProvince) {
        SignatureProductionPlace.logger.debug("State/province: " + stateOrProvince);
        this.stateOrProvince = stateOrProvince;
    }
    
    public String getPostalCode() {
        return this.postalCode;
    }
    
    public void setPostalCode(final String postalCode) {
        SignatureProductionPlace.logger.debug("Postal code: " + postalCode);
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return this.country;
    }
    
    public void setCountry(final String country) {
        SignatureProductionPlace.logger.debug("Country: " + country);
        this.country = country;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)SignatureProductionPlace.class);
    }
}
