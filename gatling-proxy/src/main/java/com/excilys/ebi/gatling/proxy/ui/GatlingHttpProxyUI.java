package com.excilys.ebi.gatling.proxy.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.excilys.ebi.gatling.proxy.ui.component.ConfigurationFrame;

public class GatlingHttpProxyUI {

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			public void run() {
				JFrame confFrame = new ConfigurationFrame();
				confFrame.setVisible(true);
			}
		});
	}
}
