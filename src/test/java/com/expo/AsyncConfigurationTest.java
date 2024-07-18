package com.expo;

import com.expo.config.AsyncConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.expo.config.AsyncConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncConfigurationTest {

    @Test
    public void testAsyncExecutorBean() {
        // Create an instance of the AsyncConfiguration
        AsyncConfiguration asyncConfiguration = new AsyncConfiguration();

        // Get the asyncExecutor bean
        Executor executor = asyncConfiguration.asyncExecutor();

        // Verify that the returned executor is an instance of ThreadPoolTaskExecutor
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
    }

    @Test
    public void testThreadPoolProperties() {
        // Create an instance of the AsyncConfiguration
        AsyncConfiguration asyncConfiguration = new AsyncConfiguration();

        // Get the asyncExecutor bean
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfiguration.asyncExecutor();

        // Verify the core pool size is set to 10
        assertEquals(10, executor.getCorePoolSize());

        // Verify the maximum pool size is set to 50
        assertEquals(50, executor.getMaxPoolSize());

        // Verify the queue capacity is set to 100
        assertEquals(100, executor.getQueueCapacity());

        // Verify the thread name prefix
        assertEquals("AsyncExecutorThread-", executor.getThreadNamePrefix());
    }





}
