package agent

import org.slf4j.LoggerFactory
import java.lang.instrument.Instrumentation

class TransformManager {
	private final Set<String> classesSeen = [] as Set

	private final Instrumentation instrumentation;

	private final List<String> targets

	public TransformManager(Instrumentation instrumentation, final List<String> targets) {
		this.targets = targets;

		this.instrumentation = instrumentation;
		
		RecorderSingleton.INSTANCE.addListener({
			LoggerFactory.getLogger(this.class).debug("Probe fired: {} ", it)

			instrumentation.retransformClasses(Class.forName(it.className.replaceAll("/", ".")));
		} as RecorderListener)

		addShutdownHook {

			(new File("occulus.txt")).withWriter { writer ->
				classesSeen.each { className ->
					RecorderSingleton.INSTANCE.basicBlocksRemaining(className).each {
						writer.write(it.toString() + "\n");
					}
				}
			}
		}
	}

	public byte[] transform(final String className, final byte[] bytes) {
		if(! targets.any { className.startsWith(it) }) return null;

		def instrumentater = new Instrumenter(className, bytes);

		if(!classesSeen.contains(className)) {
			RecorderSingleton.INSTANCE.addBlocks(instrumentater.getBasicBlocks());
			classesSeen << className
		}

		return instrumentater.instrument(RecorderSingleton.INSTANCE.basicBlocksRemaining(className));
	}
}