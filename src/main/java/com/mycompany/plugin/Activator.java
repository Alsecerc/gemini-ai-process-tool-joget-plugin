package com.mycompany.plugin;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Bundle Activator for Gemini AI Process Tool
 * Registers the plugin using its class name (NOT interface name)
 */
public class Activator implements BundleActivator {

    protected List<ServiceRegistration<?>> registrationList;

    @Override
    public void start(BundleContext context) {
        System.out.println("=== Starting Gemini AI Process Tool Plugin ===");
        
        try {
            registrationList = new ArrayList<>();
            
            // Create plugin instance
            GeminiAIProcessTool plugin = new GeminiAIProcessTool();
            
            // Register using CLASS NAME, not interface name
            String serviceName = plugin.getClass().getName();
            ServiceRegistration<?> registration = context.registerService(
                serviceName,    // ← FIXED: Use class name
                plugin,         // ← The actual plugin instance  
                null
            );
            registrationList.add(registration);
            
            System.out.println("✅ SUCCESS: Gemini AI Process Tool Plugin registered");
            System.out.println("   Service Name: " + serviceName);
            System.out.println("   Plugin Name: " + plugin.getName());
            System.out.println("   Plugin Version: " + plugin.getVersion());
            System.out.println("   Plugin Class: " + plugin.getClassName());
            
        } catch (Exception e) {
            System.err.println("❌ FAILED to start Gemini AI Process Tool Plugin:");
            System.err.println("   Error: " + e.getMessage());
        }
    }

    @Override
    public void stop(BundleContext context) {
        System.out.println("=== Stopping Gemini AI Process Tool Plugin ===");
        
        try {
            if (registrationList != null) {
                for (ServiceRegistration<?> registration : registrationList) {
                    if (registration != null) {
                        registration.unregister();
                    }
                }
                registrationList.clear();
            }
            System.out.println("✅ Gemini AI Process Tool Plugin stopped successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error stopping plugin: " + e.getMessage());
        }
    }
}