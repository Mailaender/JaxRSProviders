/*******************************************************************************
* Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.cxf.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.jaxrs.JaxRSContainerInstantiator;
import org.eclipse.ecf.provider.jaxrs.server.JaxRSServerContainer;
import org.eclipse.ecf.provider.jaxrs.server.JaxRSServerDistributionProvider;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class CXFServerDistributionProvider extends JaxRSServerDistributionProvider {

	public static final String SERVER_CONFIG_NAME = "ecf.jaxrs.cxf.server";

	public static final String URI_PARAM = "uri";
	public static final String URI_DEFAULT = "http://localhost:8080/cxf";

	public class CXFServerConfiguration implements Configuration {

		private List<Object> extensions;
		private Map<String,Object> properties;
		
		public CXFServerConfiguration(List<Object> extensions, Map<String,Object> props) {
			this.extensions = extensions;
			this.properties = props;
		}
		@Override
		public Set<Class<?>> getClasses() {
			return null;
		}

		@Override
		public Map<Class<?>, Integer> getContracts(Class<?> arg0) {
			return null;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Set<Object> getInstances() {
			return new HashSet(extensions);
		}

		public List<Object> getExtensions() {
			return extensions;
		}
		
		@Override
		public Map<String, Object> getProperties() {
			return properties;
		}

		@Override
		public Object getProperty(String arg0) {
			return properties.get(arg0);
		}

		@Override
		public Collection<String> getPropertyNames() {
			return properties.keySet();
		}

		@Override
		public RuntimeType getRuntimeType() {
			return null;
		}

		@Override
		public boolean isEnabled(Feature arg0) {
			return false;
		}

		@Override
		public boolean isEnabled(Class<? extends Feature> arg0) {
			return false;
		}

		@Override
		public boolean isRegistered(Object arg0) {
			return extensions.contains(arg0);
		}

		@Override
		public boolean isRegistered(Class<?> arg0) {
			return false;
		}
		
	}
	@SuppressWarnings("rawtypes")
	public class CXFServerConfigurable implements Configurable {

		private Map<String,Object> properties = new HashMap<String,Object>();
		private List<Object> extensions = new ArrayList<Object>();
		
		@Override
		public Configuration getConfiguration() {
			return new CXFServerConfiguration(extensions,properties);
		}

		@Override
		public Configurable property(String arg0, Object arg1) {
			properties.put(arg0, arg1);
			return this;
		}

		@Override
		public Configurable register(Class arg0) {
			return null;
		}

		@Override
		public Configurable register(Object arg0) {
			this.extensions.add(arg0);
			return this;
		}

		@Override
		public Configurable register(Class arg0, int arg1) {
			return null;
		}

		@Override
		public Configurable register(Class arg0, Class... arg1) {
			return null;
		}

		@Override
		public Configurable register(Class arg0, Map arg1) {
			return null;
		}

		@Override
		public Configurable register(Object arg0, int arg1) {
			this.extensions.add(arg0);
			return this;
		}

		@Override
		public Configurable register(Object arg0, Class... arg1) {
			this.extensions.add(arg0);
			return this;
		}

		@Override
		public Configurable register(Object arg0, Map arg1) {
			this.extensions.add(arg0);
			return this;
		}
		
	}
	
	public class DPCXFNonSpringJaxrsServlet extends CXFNonSpringJaxrsServlet {
		
		private static final long serialVersionUID = -2618572428261717260L;

		private CXFServerConfiguration config;
		
		public DPCXFNonSpringJaxrsServlet(Application application, CXFServerConfiguration config) {
			super(application);
			this.config = config;
		}

		@Override
		protected void createServerFromApplication(ServletConfig servletConfig) throws ServletException {
	        Application app = getApplication();
	        JAXRSServerFactoryBean bean = ResourceUtils.createApplication(
	                                          app,
	                                          isIgnoreApplicationPath(servletConfig),
	                                          getStaticSubResolutionValue(servletConfig),
	                                          isAppResourceLifecycleASingleton(app, servletConfig),
	                                          getBus());
	        bean.setBus(getBus());
	        bean.setApplication(getApplication());
	        bean.setProviders(config.getExtensions());
	        bean.create();
		}
	}
	

	public CXFServerDistributionProvider(final BundleContext context) {
		super();
		setName(SERVER_CONFIG_NAME);
		setInstantiator(new JaxRSContainerInstantiator(SERVER_CONFIG_NAME) {

			@Override
			protected boolean supportsOSGIConfidentialIntent(ContainerTypeDescription description) {
				return true;
			}

			@Override
			protected boolean supportsOSGIPrivateIntent(ContainerTypeDescription description) {
				return true;
			}

			@Override
			public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters,
					final Configuration configuration) throws ContainerCreateException {
				String uriStr = getParameterValue(parameters, URI_PARAM, URI_DEFAULT);
				URI uri = null;
				try {
					uri = new URI(uriStr);
				} catch (Exception e) {
					throw new ContainerCreateException("Cannot create Jersey Server Container because uri=" + uri, e);
				}
				checkOSGIIntents(description, uri, parameters);
				return new JaxRSServerContainer(context, uri) {
					@Override
					protected Servlet createServlet(final RSARemoteServiceRegistration registration) {
						return new DPCXFNonSpringJaxrsServlet(new Application() {
							@Override
							public Set<Class<?>> getClasses() {
								Set<Class<?>> results = new HashSet<Class<?>>();
								results.add(registration.getService().getClass());
								return results;
							}
							
						}, (CXFServerConfiguration) configuration);
					}

					@Override
					protected void exportRegistration(RSARemoteServiceRegistration reg) {
					}

					@Override
					protected void unexportRegistration(RSARemoteServiceRegistration registration) {
					}
				};
			}
		});
		setDescription("CXF Jax-RS Distribution Provider");
		setServer(true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Configurable createConfigurable() {
		CXFServerConfigurable configurable = new CXFServerConfigurable();
		configurable.register(new JacksonJaxbJsonProvider());
		configurable.register(new ObjectMapperContextResolverComponent());
		return configurable;
	}

}
