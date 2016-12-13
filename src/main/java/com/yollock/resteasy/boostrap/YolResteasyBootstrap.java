package com.yollock.resteasy.boostrap;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Created by yangbo12 on 2016/12/2.
 */
public class YolResteasyBootstrap extends ResteasyBootstrap {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        YolListenerBootstrap config = new YolListenerBootstrap(event.getServletContext());
        deployment = config.createDeployment();
        deployment.start();

        servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
        servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
        servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());

    }
}
