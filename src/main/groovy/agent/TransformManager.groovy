package agent

import org.slf4j.LoggerFactory

class TransformManager {
	private final Set<String> classesSeen = [] as Set
	
	private final List<String> targets

	public TransformManager(final List<String> targets) {
		this.targets = targets;
		
		RecorderSingleton.INSTANCE.addListener({
			LoggerFactory.getLogger(this.class).debug("Probe fired: {} ", it)
		} as RecorderListener)
		
	}

	public byte[] transform(final String className, final byte[] bytes) {
		if(! targets.any { className.startsWith(it) }) return bytes;

		def instrumentation = new Instrumentation(className, bytes);

		if(!classesSeen.contains(className)) {
			RecorderSingleton.INSTANCE.addBlocks(instrumentation.getBasicBlocks());
			classesSeen << className
		}

		return instrumentation.instrument(RecorderSingleton.INSTANCE.basicBlocksRemaining(className));
	}
}