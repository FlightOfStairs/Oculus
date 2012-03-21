package agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.slf4j.LoggerFactory;

public class Agent implements ClassFileTransformer {
	
	public final List<String> targetList;
	
	public final TransformManager transformManager = new TransformManager();

	private Agent(final List<String> targetList) {
		this.targetList = targetList;
		
		
	}
	

	public final byte[] transform(final ClassLoader ldr, final String className, final Class clazz, final ProtectionDomain domain, final byte[] bytes) {
		if(! targetList.any { className.startsWith(it) }) return bytes;

		try { return transformManager.transform(className, bytes); }
		catch(all) { all.printStackTrace() }
	}

	public static void premain(final String options, final Instrumentation inst) {
		inst.addTransformer(new Agent(["agenttest/"]));
		
		LoggerFactory.getLogger(Agent.class).debug("Occulus agent started.");
	}

}