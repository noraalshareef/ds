//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sa.gov.nic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationRegistry extends HashMap<ConfigurationParameter, String> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRegistry.class);
    private static final long serialVersionUID = 7829136421415567565L;
    private String sealValue = "";

    public ConfigurationRegistry() {
    }

    protected String generateSealValue() {
        return this.seal();
    }

    protected String getSealValue() {
        return this.sealValue;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ConfigurationParameter[] arr$ = ConfigurationParameter.values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            ConfigurationParameter parameter = arr$[i$];
            String value;
            if (this.containsKey(parameter)) {
                value = String.format("%s|%s", parameter, ((String)this.get(parameter)).replaceAll("[\\\\]*[\\|]", "\\\\|"));
            } else {
                value = String.format("%s", parameter);
            }

            logger.trace("Writing {}", value);
            stream.writeUTF(value);
        }

        stream.writeUTF(this.generateSealValue());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        for(int i = 0; i <= ConfigurationParameter.values().length; ++i) {
            try {
                String token = stream.readUTF();

                try {
                    String[] s = StringUtils.split(token, "|");
                    logger.trace("Reading {}", s[0]);
                    this.put(ConfigurationParameter.valueOf(s[0]), s[1].replaceAll("[\\\\]+[\\|]", "\\|"));
                } catch (IndexOutOfBoundsException var5) {
                    logger.trace("Ignoring, no value: {}", var5.getMessage());
                } catch (IllegalArgumentException var6) {
                    logger.debug("Seal <{}> found", token);
                    this.sealValue = token;
                }
            } catch (IOException var7) {
                if (logger.isDebugEnabled()) {
                    logger.warn("Error", var7);
                } else {
                    logger.warn("Error: {}", var7.getMessage());
                }
            }
        }

        this.checkCurrentConfiguration();
    }

    private void checkCurrentConfiguration() {
        ConfigurationRegistry registry = Configuration.getInstance().getRegistry();
        String currentSealValue = registry.generateSealValue();
        if (logger.isDebugEnabled()) {
            logger.debug("Seal {} {} {}", new Object[]{this.sealValue, this.sealValue.equals(currentSealValue) ? "==" : "!=", currentSealValue});
        }

        if (!this.sealValue.equals(currentSealValue)) {
            logger.info("Overwriting deserialized registry with current one");
            this.clear();
            this.putAll(registry);
        }

    }

    private String seal() {
        try {
            return Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(this.calculateToken().getBytes("UTF-8")));
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    private String calculateToken() {
        StringBuilder sb = new StringBuilder();
        ConfigurationParameter[] arr$ = ConfigurationParameter.values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            ConfigurationParameter parameter = arr$[i$];
            sb.append(String.format("%s", this.get(parameter)));
        }

        return sb.toString();
    }
}
