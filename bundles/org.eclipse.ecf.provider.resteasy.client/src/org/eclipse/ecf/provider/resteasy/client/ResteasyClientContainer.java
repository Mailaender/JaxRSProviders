/*******************************************************************************
* Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.resteasy.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.jaxrs.client.JaxRSClientContainer;
//import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
//import org.jboss.resteasy.client.jaxrs.ResteasyClient;
//import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
//import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ResteasyClientContainer extends JaxRSClientContainer {

	public static final String CONTAINER_TYPE_NAME = "ecf.container.client.resteasy";

	public class MyClassLoader extends ClassLoader {

		public MyClassLoader() {
			super(ResteasyClientContainer.class.getClassLoader());
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			System.out.println("loadClass=" + name);
			try {
				Class<?> result = super.loadClass(name);
				System.out.println("loadedClass=" + result + " with classloader=" + result.getClass().getClassLoader());
				return result;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw e;
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
				throw e;
			}
		}

	}

	@Override
	protected Object createJaxRSProxy(ClassLoader cl, @SuppressWarnings("rawtypes") Class interfaceClass)
			throws ECFException {
		Thread currentThread = Thread.currentThread();
		ClassLoader ccl = currentThread.getContextClassLoader();
		try {
			currentThread.setContextClassLoader(ResteasyClientContainer.class.getClassLoader());
			Client client = ClientBuilder.newClient();
			@SuppressWarnings("unused")
			WebTarget target = client.target(getConnectedTarget());
			Object result = null;
			// ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
			// @SuppressWarnings({ "unchecked", "rawtypes" })
			// ProxyBuilder proxyBuilder = rtarget.proxyBuilder(interfaceClass);
			// //proxyBuilder.classloader(cl);
			// Object result = proxyBuilder.build();
			return result;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new ECFException("client could not be create", t);
		} finally {
			currentThread.setContextClassLoader(ccl);
		}
	}

}
