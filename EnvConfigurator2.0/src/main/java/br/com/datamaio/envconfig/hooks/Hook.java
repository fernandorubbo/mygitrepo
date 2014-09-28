package br.com.datamaio.envconfig.hooks;

import groovy.lang.Script;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import br.com.datamaio.envconfig.cmd.Command;
import br.com.datamaio.envconfig.cmd.Command.Interaction;
import br.com.datamaio.envconfig.conf.ConfEnvironments;
import br.com.datamaio.envconfig.conf.Configuration;

public abstract class Hook extends Script {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Map<String, String> HOSTS = new HashMap<String, String>();
	
	protected ConfEnvironments envs;
	protected Map<String, String> props;
	protected Configuration conf;
	private final Properties transientProps = new Properties();
	protected final Command command;

	public Hook(){
		command = Command.get();
	}

	public boolean pre() {return true;}
	public void post() {}
	public void finish() {
		Set<Object> keySet = transientProps.keySet();
		for (Object key : keySet) {
			props.remove(key);
		}
	}

	//---------------- init command delegates ---------------
	
	public String osname() {
		return Command.osname();
	}

	public boolean isLinux() {
		return Command.isLinux();
	}
	
	public boolean isWindows(){
        return Command.isWindows();
    }
	
	public void execute(String file) {
		command.execute(file);
	}
	
	public String distribution() {
		return command.distribution();
	}

	public  void dos2unix(String file) {
		command.dos2unix(file);		
	}

	public String groupadd(final String group) {
		return command.groupadd(group);
	}

	public String groupadd(final String group, final String options) {
		return command.groupadd(group, options);
	}

	public String useradd(final String user) {
		return command.useradd(user);
	}

	public String useradd(final String user, final String options) {
		return command.useradd(user, options);
	}

	public String passwd(final String user, final String passwd) {
		return command.passwd(user, passwd);
	}

	public String chmod(String mode, String file) {
		return command.chmod(mode, file);
	}

	public String chmod(String mode, String file, boolean recursive) {
		return command.chmod(mode, file, recursive);
	}

	public String chown(String user, String file) {
		return command.chown(user, file);
	}

	public String chown(String user, String file, boolean recursive) {
		return command.chown(user, file, recursive);
	}

	public String chown(String user, String group, String file, boolean recursive) {
		return command.chown(user, group, file, recursive);
	}

	public String ln(final String link, final String targetFile) {
		return command.ln(link, targetFile);
	}

	public boolean exists(String file){
		return command.exists(file);
	}
	
	public String whoami() {
		return command.whoami();
	}

	public void mkdir(String dir) {
		command.mkdir(dir);
	}

	public void mv(String from, String to) {
		command.mv(from, to);
	}

	public List<String> ls(String path) {
		return command.ls(path);
	}

	public void rm(String path) {
		command.rm(path);
	}

	public void cp(String from, String to) {
		command.cp(from, to);
	}

	// --- run ----

	public String run(String cmd) {
		return command.run(cmd);
	}

	public String run(List<String> cmdList) {
		return command.run(cmdList);
	}

	public String run(String cmd, final boolean printOutput) {
		return command.run(cmd, printOutput);
	}

	public String run(List<String> cmdList, final boolean printOutput) {
		return command.run(cmdList, printOutput);
	}

	public String run(String cmd, final int... successfulExec) {
		return command.run(cmd, successfulExec);
	}

	public String run(List<String> cmdList, final int... successfulExec) {
		return command.run(cmdList, successfulExec);
	}

	public String run(String cmd, Interaction interact) {
		return command.run(cmd, interact);
	}

	public String run(List<String> cmdList, Interaction interact) {
		return command.run(cmdList, interact);
	}

	/**
	 * Este metodo nao faz interacao nenhuma. Isto é, ele não mostra o output e
	 * nem o erro. Mas se o retorno do comando for diferente de 0, ele continua
	 * lancando uma exception
	 *
	 * OBS> este metodo foi criado pois alguns executaveis travavam lendo o
	 * output
	 */
	public String runWithNoInteraction(String cmd) {
		return command.runWithNoInteraction(cmd);
	}

	public String runWithNoInteraction(List<String> cmdList) {
		return command.runWithNoInteraction(cmdList);
	}

    // --- env methods ---

	protected boolean isDesenv(){
		return !isTst() && !isHom() && !isProd();
	}

	protected boolean isTst(){
		final String address = whatIsMyIp();
		return envs.isTst(address);
	}

	protected boolean isHom(){
		final String address = whatIsMyIp();
		return envs.isHom(address);
	}

	protected boolean isProd(){
		final String address = whatIsMyIp();
		return envs.isProd(address);
	}

    protected String whatIsMyIp()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        String ip = addr.getHostAddress();
	        if("127.0.0.1".equals(ip)){ 
	        	// Cai aqui quando tem no /etc/hosts a identificação do nome com 127.0.0.1
	        	ip = getIpFromDNS(addr.getHostName());
	        }
			return ip;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
    protected String whatIsMyHostName()
    {
        try {
			final InetAddress addr = InetAddress.getLocalHost();
	        return addr.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void log(String msg) {
        LOGGER.info("\t" + msg);
    }
        
    // --- properties methods ---
    
	protected void addTransientProperty(final String key, final Boolean value){
		addTransientProperty(key, "" + value);
	}

	protected void addTransientProperty(final String key, final String value){
		props.put(key, value);
		transientProps.put(key, value);
	}

	protected void addPersistentProperty(final String key, final String value){
		props.put(key, value);
	}

	protected String get(final String key){
		if(props==null)
			return null;
	    return props.get(key);
	}

	protected boolean contains(final String key){
        return props.get(key)!=null;
    }
	
    

	// ------ private methods ------

	private String getIpFromDNS(String hostName) {
		if(!HOSTS.containsKey(hostName)) { 
			System.out.println("\t\t\tBuscando IP no DNS para o host " + hostName);
			final List<String> dnsRecs = getDNSRecs(hostName, "A");
			final String ip = dnsRecs.size()>0 ? dnsRecs.get(0) : "127.0.0.1";
			HOSTS.put(hostName, ip);
		}
		return HOSTS.get(hostName);
	}
	
	 /**
     * Rertorna todos os registros do DNS para um dado dominio
     *
     * @param domain domínio, e.g. xyz.dbserver.com.br, no qual você deseja conhecer os registros do DNS.
     * @param types  e.g."MX","A" para descrever quais registros vc deseja.
     * 			<ul>
     * 				<li> MX: o resultado contém a prioridade (lower better) seguido pelo mailserver
     * 				<li> A: o resultado contém apenas o IP
     * 			</ul>
     *
     * @return lista de resultados
     */
	private List<String> getDNSRecs(String domain, String... types) {
		
		List<String> results = new ArrayList<String>(15);

		try {
			final Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			
			final DirContext ictx = new InitialDirContext(env);
			final Attributes attrs = ictx.getAttributes(domain, types);
			for (NamingEnumeration<? extends Attribute> e = attrs.getAll(); e.hasMoreElements();) {
				final Attribute a = (Attribute) e.nextElement();
				for (int i = 0; i < a.size(); i++) {
					results.add((String) a.get(i));
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		if (results.size() == 0) {
			System.err.println("Falha para encontrar um registro no DNS para o domínio " + domain);
		}
		return results;
	}
    
	protected void setEnvs(ConfEnvironments envs) {
		this.envs = envs;
	}
	protected void setProps(Map<String, String> props) {
		this.props = props;
	}
	protected void setConf(Configuration conf) {
		this.conf = conf;
	}
	
}