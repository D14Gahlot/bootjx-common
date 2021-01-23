package com.boot.loaderjs;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.boot.jx.postman.nlp.OpenNLPService;

public class TestOpenNLPService { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		new OpenNLPService().trainCategorizerModelFromFile();
	}

}
