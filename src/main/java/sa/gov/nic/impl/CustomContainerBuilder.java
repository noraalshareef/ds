// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl;

import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;
import sa.gov.nic.exceptions.TechnicalException;
import java.io.InputStream;
import sa.gov.nic.Configuration;
import sa.gov.nic.Container;
import org.slf4j.Logger;
import sa.gov.nic.ContainerBuilder;

public class CustomContainerBuilder extends ContainerBuilder
{
    private static final Logger logger;
    private String containerType;
    
    public CustomContainerBuilder(final String containerType) {
        this.containerType = containerType;
    }
    
    @Override
    protected Container createNewContainer() {
        if (this.configuration != null) {
            return this.instantiateContainer(this.configuration);
        }
        return this.instantiateContainer();
    }
    
    @Override
    protected Container openContainerFromFile() {
        if (this.configuration != null) {
            return this.instantiateContainer(this.containerFilePath, this.configuration);
        }
        return this.instantiateContainer(this.containerFilePath);
    }
    
    @Override
    protected Container openContainerFromStream() {
        if (this.configuration == null) {
            return this.instantiateContainer(this.containerInputStream);
        }
        return this.instantiateContainer(this.containerInputStream, this.configuration);
    }
    
    @Override
    public ContainerBuilder usingTempDirectory(final String temporaryDirectoryPath) {
        CustomContainerBuilder.logger.warn("Custom containers don't support setting temp directories");
        return this;
    }
    
    private Container instantiateContainer() {
        return this.instantiateContainer(null, (Object[])null);
    }
    
    private Container instantiateContainer(final Configuration configuration) {
        final Class<?>[] parameterTypes = (Class<?>[])new Class[] { Configuration.class };
        final Object[] constructorArguments = { configuration };
        return this.instantiateContainer(parameterTypes, constructorArguments);
    }
    
    private Container instantiateContainer(final String containerFilePath) {
        final Class<?>[] parameterTypes = (Class<?>[])new Class[] { String.class };
        final Object[] constructorArguments = { containerFilePath };
        return this.instantiateContainer(parameterTypes, constructorArguments);
    }
    
    private Container instantiateContainer(final String containerFilePath, final Configuration configuration) {
        final Class<?>[] parameterTypes = (Class<?>[])new Class[] { String.class, Configuration.class };
        final Object[] constructorArguments = { containerFilePath, configuration };
        return this.instantiateContainer(parameterTypes, constructorArguments);
    }
    
    private Container instantiateContainer(final InputStream containerInputStream) {
        final Class<?>[] parameterTypes = (Class<?>[])new Class[] { InputStream.class };
        final Object[] constructorArguments = { containerInputStream };
        return this.instantiateContainer(parameterTypes, constructorArguments);
    }
    
    private Container instantiateContainer(final InputStream containerInputStream, final Configuration configuration) {
        final Class<?>[] parameterTypes = (Class<?>[])new Class[] { InputStream.class, Configuration.class };
        final Object[] constructorArguments = { containerInputStream, configuration };
        return this.instantiateContainer(parameterTypes, constructorArguments);
    }
    
    private Container instantiateContainer(final Class<?>[] parameterTypes, final Object[] constructorArguments) {
        final Class<? extends Container> containerClass = this.getContainerClass();
        CustomContainerBuilder.logger.debug("Instantiating " + this.containerType + " container from class " + containerClass.getName());
        try {
            if (constructorArguments == null || constructorArguments.length == 0) {
                return (Container)containerClass.newInstance();
            }
            final Constructor<? extends Container> constructor = containerClass.getConstructor(parameterTypes);
            return (Container)constructor.newInstance(constructorArguments);
        }
        catch (NoSuchMethodException e) {
            CustomContainerBuilder.logger.error("Unable to instantiate " + this.containerType + " container from class " + containerClass.getName() + " - The class must be public and should have a default constructor and a constructor with Configuration parameter available.");
            throw new TechnicalException("Unable to instantiate " + this.containerType + " container from class " + containerClass.getName(), e);
        }
        catch (ReflectiveOperationException e2) {
            CustomContainerBuilder.logger.error("Unable to instantiate " + this.containerType + " container from class " + containerClass.getName());
            throw new TechnicalException("Unable to instantiate " + this.containerType + " container from class " + containerClass.getName(), e2);
        }
    }
    
    private Class<? extends Container> getContainerClass() {
        return CustomContainerBuilder.containerImplementations.get(this.containerType);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)CustomContainerBuilder.class);
    }
}
