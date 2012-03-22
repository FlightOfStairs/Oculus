package agent

import org.slf4j.LoggerFactory
import java.lang.instrument.Instrumentation

class RecordManager {
	private final Map<String, ClassRecord> classes = [:]

	private final Instrumentation instrumentation

	private final List<String> targets

	public RecordManager(Instrumentation instrumentation, final List<String> targets) {
		this.targets = targets;

		this.instrumentation = instrumentation;
		
		RecorderSingleton.INSTANCE.addListener({
			if(classes[it.className].remainingBlocks().contains(it)) {
				LoggerFactory.getLogger(this.class).debug("Retransforming after probe hit: {}", it)
				classes[it.className].seenBlock(it);

				instrumentation.retransformClasses(Class.forName(it.className.replaceAll("/", ".")))
			}

		} as RecorderListener)

		addShutdownHook {

			(new File("oculus.txt")).withWriter { writer ->
				classes.values().each { classRecord ->
					if(classRecord.remainingBlocks().size() != 0)
						writer.write(classRecord.toString() + "\n");
				}
			}
		}
	}

	public byte[] transform(final String className, final byte[] bytes) {
		if(! targets.any { className.startsWith(it) }) return null;

		def instrumenter = new Instrumenter(className, bytes)

		if(! classes.containsKey(className)) {
			classes[className] = new ClassRecord(className, instrumenter.getBasicBlocks())
		}

		return instrumenter.instrument(classes[className].remainingBlocks())
	}
}