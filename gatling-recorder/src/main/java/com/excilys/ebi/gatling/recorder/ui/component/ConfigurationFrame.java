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
package com.excilys.ebi.gatling.recorder.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.excilys.ebi.gatling.recorder.configuration.Configuration;
import com.excilys.ebi.gatling.recorder.configuration.Pattern;
import com.excilys.ebi.gatling.recorder.http.event.ShowConfigurationFrameEvent;
import com.excilys.ebi.gatling.recorder.http.event.ShowRunningFrameEvent;
import com.excilys.ebi.gatling.recorder.ui.enumeration.FilterStrategy;
import com.excilys.ebi.gatling.recorder.ui.enumeration.ResultType;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class ConfigurationFrame extends JFrame {

	public static final Logger logger = LoggerFactory.getLogger(ConfigurationFrame.class);

	public final JTextField txtPort = new JTextField(null, 4);
	public final JTextField txtSslPort = new JTextField(null, 4);

	public final JTextField txtProxyHost = new JTextField(null, 15);
	public final JTextField txtProxyPort = new JTextField(null, 4);
	public final JTextField txtProxySslPort = new JTextField(null, 4);

	public final JComboBox cbFilterStrategies = new JComboBox();
	public final JCheckBox chkSavePref = new JCheckBox("Save preferences");
	public final JTextField txtOutputFolder = new JTextField(49);
	public final FilterTable tblFilters = new FilterTable();
	public final List<JCheckBox> resultTypes = new ArrayList<JCheckBox>();
	public final JComboBox cbOutputEncoding = new JComboBox();

	JButton btnFiltersAdd = new JButton("+");
	JButton btnFiltersDel = new JButton("-");
	JButton btnOutputFolder = new JButton("Browse");
	JButton btnClear = new JButton("Clear");
	JButton btnStart = new JButton("Start !");

	JPanel pnlTop = null;
	JPanel pnlCenter = null;
	JPanel pnlBottom = null;

	public ConfigurationFrame() {

		/** Initialization of the frame **/

		setTitle("Gatling Recorder - Configuration");
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(800, 640));
		setResizable(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImages(Commons.getIconList());

		/** Initialization of components **/

		initTopPanel();
		initCenterPanel();
		initBottomPanel();

		populateItemsFromConfiguration();

		setListeners();
	}

	private void initTopPanel() {
		/***** Creating Top Panel (Network) *****/
		pnlTop = new JPanel(new BorderLayout());

		/* Gatling Image */
		JPanel pnlImage = new JPanel();
		pnlImage.add(new JLabel(Commons.getGatlingImage()));

		/* Network Panel */
		JPanel pnlNetwork = new JPanel(new BorderLayout());
		pnlNetwork.setBorder(BorderFactory.createTitledBorder("Network"));
		pnlNetwork.setLayout(new BorderLayout());

		/* Local proxy host panel */
		JPanel localProxyHostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		localProxyHostPanel.add(new JLabel("Listening port* : "));
		localProxyHostPanel.add(new JLabel("                                    localhost"));

		/* Local proxy ports panel */
		JPanel localProxyPortsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		localProxyPortsPanel.add(new JLabel("HTTP"));
		localProxyPortsPanel.add(txtPort);
		localProxyPortsPanel.add(new JLabel("HTTPS"));
		localProxyPortsPanel.add(txtSslPort);

		/* Local proxy panel */
		JPanel localProxyPanel = new JPanel(new FlowLayout());
		localProxyPanel.add(localProxyHostPanel);
		localProxyPanel.add(localProxyPortsPanel);

		/* Outgoing proxy host panel */
		JPanel outgoingProxyHostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outgoingProxyHostPanel.add(new JLabel("Outgoing proxy : "));

		/* Outgoing proxy ports panel */
		JPanel outgoingProxyPortsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		outgoingProxyPortsPanel.add(new JLabel("host:"));
		outgoingProxyPortsPanel.add(txtProxyHost);
		outgoingProxyPortsPanel.add(new JLabel("HTTP"));
		outgoingProxyPortsPanel.add(txtProxyPort);
		outgoingProxyPortsPanel.add(new JLabel("HTTPS"));
		outgoingProxyPortsPanel.add(txtProxySslPort);

		/* Outgoing proxy panel */
		JPanel outgoingProxyPanel = new JPanel(new FlowLayout());
		outgoingProxyPanel.add(outgoingProxyHostPanel);
		outgoingProxyPanel.add(outgoingProxyPortsPanel);

		/* Adding panels to newtworkPanel */
		pnlNetwork.add(localProxyPanel, BorderLayout.NORTH);
		pnlNetwork.add(outgoingProxyPanel, BorderLayout.SOUTH);

		/* Adding Image and network panel to top panel */
		pnlTop.add(pnlImage, BorderLayout.WEST);
		pnlTop.add(pnlNetwork, BorderLayout.EAST);

		/* Adding panel to Frame */
		add(pnlTop, BorderLayout.NORTH);
	}

	private void initCenterPanel() {
		/***** Creating Center Panel (Filters) *****/
		pnlCenter = new JPanel();
		pnlCenter.setBorder(BorderFactory.createTitledBorder("Filters"));
		pnlCenter.setLayout(new BorderLayout());

		// Fill Combo Box for Strategies
		for (FilterStrategy ft : FilterStrategy.values())
			cbFilterStrategies.addItem(ft);

		/* Filter Actions panel */
		JPanel filterActionsPanel = new JPanel();
		filterActionsPanel.add(new JLabel("Strategy"));
		filterActionsPanel.add(cbFilterStrategies);
		filterActionsPanel.add(btnFiltersAdd);
		filterActionsPanel.add(btnFiltersDel);
		filterActionsPanel.add(btnClear);

		/* Adding panels to centerPanel */
		pnlCenter.add(tblFilters, BorderLayout.CENTER);
		pnlCenter.add(filterActionsPanel, BorderLayout.PAGE_END);

		/* Adding panel to Frame */
		add(pnlCenter, BorderLayout.CENTER);
	}

	private void initBottomPanel() {
		/***** Creating Bottom Panel (Output + Start) *****/
		pnlBottom = new JPanel();
		pnlBottom.setLayout(new BorderLayout());

		/* Output Folder Panel */
		JPanel outputFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outputFolderPanel.add(new JLabel("Output folder* : "));
		outputFolderPanel.add(txtOutputFolder);
		outputFolderPanel.add(btnOutputFolder);
		
		/* Output type panel */
		JPanel outputTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outputTypePanel.add(new JLabel("Type: "));

		for (ResultType rt : ResultType.values())
			resultTypes.add(new JCheckBox(rt.getLabel()));
		for (JCheckBox cb : resultTypes)
			outputTypePanel.add(cb);
		
		for (Charset c : Charset.availableCharsets().values())
			cbOutputEncoding.addItem(c);
		
		cbOutputEncoding.setSelectedItem(Charset.defaultCharset());

		/* Output Panel */
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
		outputPanel.add(outputFolderPanel, BorderLayout.NORTH);
		
		JPanel outputFormatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outputFormatPanel.add(new JLabel("Encoding: "));
		outputFormatPanel.add(cbOutputEncoding);
		
		outputPanel.add(outputTypePanel, BorderLayout.EAST);
		outputPanel.add(outputFormatPanel, BorderLayout.WEST);

		/* Start Action Panel */
		JPanel startActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		startActionPanel.add(chkSavePref);
		startActionPanel.add(btnStart);

		chkSavePref.setHorizontalTextPosition(SwingConstants.LEFT);

		/* Adding panels to bottomPanel */
		pnlBottom.add(outputPanel, BorderLayout.NORTH);
		pnlBottom.add(startActionPanel, BorderLayout.SOUTH);

		/* Adding panel to Frame */
		add(pnlBottom, BorderLayout.SOUTH);
	}

	private void setListeners() {
		// Enables or disables filter edition depending on the selected strategy
		cbFilterStrategies.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && e.getItem().equals(FilterStrategy.NONE)) {
					tblFilters.setEnabled(false);
					tblFilters.setFocusable(false);
				} else {
					tblFilters.setEnabled(true);
					tblFilters.setFocusable(true);
				}
			}
		});

		// Adds a filter row when + button clicked
		btnFiltersAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tblFilters.addRow();
			}
		});

		// Removes selected filter when - button clicked
		btnFiltersDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tblFilters.removeSelectedRow();
			}
		});

		// Removes all filters when clear button clicked
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tblFilters.removeAllElements();
			}
		});

		// Opens a save dialog when Browse button clicked
		btnOutputFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
					return;

				File chosenFile = fc.getSelectedFile();
				txtOutputFolder.setText(chosenFile.getPath());
			}
		});

		// Validates form when Start button clicked
		btnStart.addActionListener(new ConfigurationValidatorListener(this));
	}

	@Subscribe
	public void onShowConfigurationFrameEvent(ShowConfigurationFrameEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
			}
		});
	}

	@Subscribe
	public void onShowRunningFrameEvent(ShowRunningFrameEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setVisible(false);
			}
		});
	}

	private void populateItemsFromConfiguration() {
		logger.debug("Configuration: {}", Configuration.getInstance());

		txtPort.setText(String.valueOf(Configuration.getInstance().getPort()));
		txtSslPort.setText(String.valueOf(Configuration.getInstance().getSslPort()));
		txtProxyHost.setText(Configuration.getInstance().getProxy().getHost());
		txtProxyPort.setText(String.valueOf(Configuration.getInstance().getProxy().getPort()));
		txtProxySslPort.setText(String.valueOf(Configuration.getInstance().getProxy().getSslPort()));
		cbFilterStrategies.setSelectedItem(Configuration.getInstance().getFilterStrategy());
		for (Pattern pattern : Configuration.getInstance().getPatterns())
			tblFilters.addRow(pattern);
		txtOutputFolder.setText(Configuration.getInstance().getOutputFolder());
		chkSavePref.setSelected(Configuration.getInstance().isSaveConfiguration());
		for (JCheckBox cb : resultTypes)
			for (ResultType resultType : Configuration.getInstance().getResultTypes())
				if (cb.getText().equals(resultType.getLabel()))
					cb.setSelected(true);
	}
}