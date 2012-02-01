/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.recorder.configuration;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.excilys.ebi.gatling.recorder.ui.enumeration.FilterStrategy;
import com.excilys.ebi.gatling.recorder.ui.enumeration.ResultType;

public class Configuration {

	public static Configuration getInstance() {
		return instance;
	}

	public static void initFromExistingConfig(Configuration c) {
		instance.setPort(c.getPort());
		instance.setSslPort(c.getSslPort());
		instance.setProxy(c.getProxy());
		instance.setFilterStrategy(c.getFilterStrategy());
		instance.setPatterns(c.getPatterns());
		instance.setOutputFolder(c.getOutputFolder());
		instance.setResultTypes(c.getResultTypes());
		instance.setSaveConfiguration(true);
		instance.setEncoding(c.getEncoding());
	}

	public static void initFromCommandLineOptions(CommandLineOptions c) {
		instance.setPort(c.getLocalPort());
		instance.setSslPort(c.getLocalPortSsl());
		instance.getProxy().setHost(c.getProxyHost());
		instance.getProxy().setPort(c.getProxyPort());
		instance.getProxy().setSslPort(c.getProxyPortSsl());
		if (c.getOutputFolder() != null)
			instance.setOutputFolder(c.getOutputFolder());

		instance.getResultTypes().clear();
		if (c.isResultText())
			instance.getResultTypes().add(ResultType.TEXT);
		if (c.isResultScala())
			instance.getResultTypes().add(ResultType.SCALA);

		if (c.isRunningFrame())
			instance.setConfigurationSkipped(true);

		instance.setIdePackage(c.getIdePackage());
		instance.setRequestBodiesFolder(c.getRequestBodiesFolder());
		instance.setEncoding(c.getEncoding());
	}

	private static final Configuration instance = new Configuration();

	private int port = 8000;
	private int sslPort = 8001;
	private ProxyConfig proxy = new ProxyConfig();
	private FilterStrategy filterStrategy = FilterStrategy.NONE;
	private List<Pattern> patterns = new ArrayList<Pattern>();
	private String outputFolder = System.getProperty("user.home");
	private List<ResultType> resultTypes = new ArrayList<ResultType>();
	private boolean saveConfiguration;
	private String encoding = Charset.defaultCharset().toString();
	private transient String requestBodiesFolder;
	private transient boolean configurationSkipped;
	private transient String idePackage;

	private Configuration() {

	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ProxyConfig getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfig proxy) {
		this.proxy = proxy;
	}

	public FilterStrategy getFilterStrategy() {
		return filterStrategy;
	}

	public void setFilterStrategy(FilterStrategy filterStrategy) {
		this.filterStrategy = filterStrategy;
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public List<ResultType> getResultTypes() {
		return resultTypes;
	}

	public void setResultTypes(List<ResultType> resultTypes) {
		this.resultTypes = resultTypes;
	}

	public int getSslPort() {
		return sslPort;
	}

	public void setSslPort(int sslPort) {
		this.sslPort = sslPort;
	}

	public boolean isSaveConfiguration() {
		return saveConfiguration;
	}

	public void setSaveConfiguration(boolean saveConfiguration) {
		this.saveConfiguration = saveConfiguration;
	}

	public boolean isConfigurationSkipped() {
		return configurationSkipped;
	}

	public void setConfigurationSkipped(boolean skipConfiguration) {
		this.configurationSkipped = skipConfiguration;
	}

	public String getIdePackage() {
		return idePackage;
	}

	public void setIdePackage(String idePackage) {
		this.idePackage = idePackage;
	}

	@Override
	public String toString() {
		return "Configuration [port=" + port + ", sslPort=" + sslPort + ", proxy=" + proxy + ", filterStrategy=" + filterStrategy + ", patterns=" + patterns + ", outputFolder="
				+ outputFolder + ", resultTypes=" + resultTypes + ", saveConfiguration=" + saveConfiguration + ", configurationSkipped=" + configurationSkipped + "]";
	}

	public String getRequestBodiesFolder() {
		return requestBodiesFolder;
	}

	public void setRequestBodiesFolder(String requestBodiesFolder) {
		this.requestBodiesFolder = requestBodiesFolder;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
