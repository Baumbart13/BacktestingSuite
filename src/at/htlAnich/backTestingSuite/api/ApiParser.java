package at.htlAnich.backTestingSuite.api;

import at.htlAnich.stockUpdater.CredentialLoader;

public class ApiParser extends at.htlAnich.stockUpdater.api.ApiParser {
	public ApiParser(CredentialLoader.ApiCredentials creds){
		this(creds.apiKey());
	}
	public ApiParser(String apiKey){
		super(apiKey);
	}
	public ApiParser(ApiParser parser){
		super(parser);
	}
}
