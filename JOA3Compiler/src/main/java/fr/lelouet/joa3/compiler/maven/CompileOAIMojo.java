package fr.lelouet.joa3.compiler.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.lelouet.joa3.compiler.OAICompiler;
import fr.lelouet.joa3.compiler.OAICompiler.Config;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

@Mojo(name = "compile")
public class CompileOAIMojo extends AbstractMojo {

	/**
	 * url to fetch the swagger from
	 */
	@Parameter(property = "oai-compiler.url")
	private String url;

	/**
	 * root package for the classes
	 */
	@Parameter(property = "oai-compiler.package")
	private String pck;

	/**
	 * list of additional specs
	 */
	@Parameter(property = "oai-compiler.specs")
	private OAISpec[] specs;

	/**
	 * directory to export the compiled java classes
	 */
	@Parameter(property = "oai-compiler.outDir", defaultValue = "${project.basedir}/src/generated/java")
	private String outFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File targetDir = new File(outFile);
		if (!targetDir.exists() && !targetDir.mkdirs())
			throw new MojoFailureException("can't create dirs for " + targetDir);
		List<OAISpec> specsl = new ArrayList<>();
		if (url != null) {
			OAISpec sp = new OAISpec();
			sp.url = url;
			sp.pck = pck;
			specsl.add(sp);
		}
		if (specs != null)
			Stream.of(specs).forEach(sp -> specsl.add(sp));
		if (specsl.isEmpty()) {
			throw new MojoFailureException("no url specified.");
		}
		Map<String, OpenAPI> package2Spec = new HashMap<>();
		Map<String, String> package2URL = new HashMap<>();
		for (OAISpec spec : specsl) {
			SwaggerParseResult result = new OpenAPIParser().readLocation(spec.url,
					null, null);
			OpenAPI oai = result.getOpenAPI();
			String pck = spec.pck;
			if (pck == null) {
				for (Server serv : oai.getServers()) {
					if (pck == null)
						pck = adr2pck(serv.getUrl());
				}
			}
			if (pck == null) {
				throw new MojoFailureException("can't guess package for specification at url " + spec.url);
			}
			if (package2Spec.put(pck, oai) != null) {
				throw new MojoFailureException("package " + pck + " deduced for spec at url " + spec.url + " is already used");
			}
			if (package2URL.put(pck, spec.url) != null) {
				throw new MojoFailureException("package " + pck + " deduced for spec at url " + spec.url + " is already used");
			}
			System.out.println("generating packages to urls : " + package2URL);
		}
		for (Entry<String, OpenAPI> e : package2Spec.entrySet()) {
			OAICompiler.Config cf = new Config();
			cf.pck = e.getKey();
			cf.oai = e.getValue();
			cf.targetDir = targetDir;
			OAICompiler.compile(cf);
		}
	}

	static String adr2pck(String serverAdr) {
		serverAdr = serverAdr.replaceAll("^.*://", "").replaceAll("\\?.*", "");
		String host = serverAdr.replaceAll("/.*", "");
		String subs = serverAdr.replaceAll("^[^/]*", "");
		ArrayList<String> hostTokens = new ArrayList<>(Arrays.asList(host.split("\\.")));
		Collections.reverse(hostTokens);
		return hostTokens.stream().collect(Collectors.joining(".")) + subs.replaceAll("/", ".");
	}

}
