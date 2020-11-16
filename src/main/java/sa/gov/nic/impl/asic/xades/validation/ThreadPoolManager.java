// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import org.slf4j.LoggerFactory;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import sa.gov.nic.Configuration;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;

public class ThreadPoolManager
{
    private static final Logger logger;
    private static ExecutorService defaultThreadExecutor;
    private Configuration configuration;
    
    public ThreadPoolManager(final Configuration configuration) {
        this.configuration = configuration;
    }
    
    public static void setDefaultThreadExecutor(final ExecutorService threadExecutor) {
        ThreadPoolManager.defaultThreadExecutor = threadExecutor;
    }
    
    public ExecutorService getThreadExecutor() {
        if (this.configuration.getThreadExecutor() != null) {
            return this.configuration.getThreadExecutor();
        }
        if (ThreadPoolManager.defaultThreadExecutor == null) {
            initializeDefaultThreadExecutor();
        }
        return ThreadPoolManager.defaultThreadExecutor;
    }
    
    private static synchronized void initializeDefaultThreadExecutor() {
        if (ThreadPoolManager.defaultThreadExecutor == null) {
            final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
            ThreadPoolManager.logger.debug("Initializing a new default thread pool executor with " + numberOfProcessors + " threads");
            ThreadPoolManager.defaultThreadExecutor = Executors.newFixedThreadPool(numberOfProcessors);
        }
    }
    
    public <T> Future<T> submit(final Callable<T> task) {
        return this.getThreadExecutor().submit(task);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ThreadPoolManager.class);
    }
}
