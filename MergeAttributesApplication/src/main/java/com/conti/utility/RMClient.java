package com.conti.utility;

import org.apache.wink.client.ClientConfig;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

@SuppressWarnings("deprecation")
public class RMClient{
	  
	  private RMClient() {
	    //DO nothing
	  }
	  
	   /**
	   * Fetch the internal ClientConfig instance of input client
	   * @param client {@link JazzFormAuthClient} instance
	   * @return {@link ClientConfig} instance
	   */
	  public static ClientConfig getInternalClientConfig(JazzFormAuthClient client) {
	    return null;
	  }

	}