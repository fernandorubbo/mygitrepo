package br.com.datamaio.envconfig.groovy;

import groovy.lang.GroovyObject;

import java.io.File;
import java.util.Properties;

public class FileHookEmbeddedGroovy extends EmbeddedGroovy {
	
	public FileHookEmbeddedGroovy(final File src, final File target, Properties props) {
		super(src.getAbsolutePath() + ".groovy", props);
		setSrc(src);
		setTarget(target);
	}
	
	private void setSrc(final File src) {
		if(hook!=null) {
			final GroovyObject groovyObject = (GroovyObject) hook;
			groovyObject.setProperty("srcPath", src.getAbsolutePath());
			groovyObject.setProperty("srcFile", src);
		}
	}	

	private void setTarget(final File target) {
		if(hook!=null) {
			final GroovyObject groovyObject = (GroovyObject) hook;
			groovyObject.setProperty("targetPath", target.getAbsolutePath());
			groovyObject.setProperty("targetFile", target);
		}
	}	
}