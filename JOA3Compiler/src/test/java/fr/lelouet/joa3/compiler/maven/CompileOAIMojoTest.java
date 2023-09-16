package fr.lelouet.joa3.compiler.maven;

import java.net.MalformedURLException;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.joa3.compiler.maven.CompileOAIMojo;

public class CompileOAIMojoTest {

	@Test
	public void testadr2pck() throws MalformedURLException {
		Assert.assertEquals(CompileOAIMojo.adr2pck("maps.google.com"), "com.google.maps");
		Assert.assertEquals(CompileOAIMojo.adr2pck("my/dir"), "my.dir");
		Assert.assertEquals(CompileOAIMojo.adr2pck("a.b.c/d/e?f=g&h=i"), "c.b.a.d.e");
	}

}
