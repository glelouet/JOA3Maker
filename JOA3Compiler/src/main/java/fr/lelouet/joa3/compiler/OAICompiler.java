package fr.lelouet.joa3.compiler;

import java.io.File;

import io.swagger.v3.oas.models.OpenAPI;

public class OAICompiler {

	public static class Config {
		public OpenAPI oai;
		public String pck;
		public File targetDir;

	}

	public static boolean compile(Config cf) {
		return true;
	}

}
