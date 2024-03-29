package agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.slf4j.LoggerFactory;

public class Agent implements ClassFileTransformer {
	
	public final RecordManager transformManager;

	private Agent(Instrumentation instrumentation, final List<String> targetList) {
		this.transformManager = new RecordManager(instrumentation, targetList)
	}
	

	public final byte[] transform(final ClassLoader ldr, final String className, final Class clazz, final ProtectionDomain domain, final byte[] bytes) {
		try { return transformManager.transform(className, bytes); }
		catch(all) { all.printStackTrace() }
	}

	public static void premain(final String options, final Instrumentation inst) {
		if(options == null) DummyMain.main([] as String[])
		
		inst.addTransformer(new Agent(inst, options.replaceAll("\\.", "/").split(",") as List<String>), true);
		
		LoggerFactory.getLogger(Agent.class).debug("Occulus agent started.");
	}

}