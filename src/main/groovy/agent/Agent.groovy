package agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.slf4j.LoggerFactory;

public class Agent implements ClassFileTransformer {
	
	public final TransformManager transformManager;

	private Agent(Instrumentation instrumentation, final List<String> targetList) {
		
		println targetList
		
		this.transformManager = new TransformManager(instrumentation, targetList)
	}
	

	public final byte[] transform(final ClassLoader ldr, final String className, final Class clazz, final ProtectionDomain domain, final byte[] bytes) {
		try { return transformManager.transform(className, bytes); }
		catch(all) { all.printStackTrace() }
	}

	public static void premain(final String options, final Instrumentation inst) {
		if(options == null) {
			println """
Syntax is java -ea -javaagent:oculus.jar=package1,pack.age2,package3 <other java arguments....>

Please give required arguments."""
			
			System.exit(1);
		}
		
		inst.addTransformer(new Agent(inst, options.replaceAll("\\.", "/").split(",") as List<String>), true);
		
		LoggerFactory.getLogger(Agent.class).debug("Occulus agent started.");
	}

}