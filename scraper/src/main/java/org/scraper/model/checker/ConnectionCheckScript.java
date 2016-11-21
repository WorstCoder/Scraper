package org.scraper.model.checker;

import org.scraper.model.Proxy;

public class ConnectionCheckScript extends ConnectionCheck {
	
	private static final String URL = "http://absolutelydisgusting.ml/ping.php";
	private static final String TITLE = "Letters";
	
	public ConnectionCheckScript(Proxy.Type type) {
		super(type, URL, TITLE);
	}
	
	public Proxy.Anonymity getAnonymity() {
		String text = response.text();
		String anonymity = "";
		if (text.contains("|"))
			anonymity = text.split("\\|")[1];
		switch (anonymity) {
			case "e": {
				return Proxy.Anonymity.ELITE;
			}
			case "a": {
				return Proxy.Anonymity.ANONYMOUS;
			}
			case "t": {
				return Proxy.Anonymity.TRANSPARENT;
			}
			default: {
				return Proxy.Anonymity.TRANSPARENT;
			}
		}
	}
	
	
}