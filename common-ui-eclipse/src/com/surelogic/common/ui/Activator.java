package com.surelogic.common.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    private static Activator plugin;

    public Activator() {
        if (plugin != null) {
            throw new IllegalStateException(Activator.class.getName()
                    + " instance already exits, it should be a singleton.");
        }
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        try {
            SLImages.dispose();
        } finally {
            super.stop(context);
        }
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance.
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Gets the identifier for this plug in.
     * 
     * @return an identifier, such as <tt>com.surelogic.common</tt>. In rare
     *         cases, for example bad plug in XML, it may be {@code null}.
     * @see Bundle#getSymbolicName()
     */
    public String getPlugInId() {
        return plugin.getBundle().getSymbolicName();
    }
}
