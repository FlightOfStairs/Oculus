package agent;


import groovy.transform.Synchronized
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires;

public enum RecorderSingleton {
	INSTANCE;

	public static void record(String s) {
		def parts = s.split(" ")
		INSTANCE.record(new BasicBlockIdent(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]),Integer.parseInt(parts[4]), Integer.parseInt(parts[5])));
	}

	private final List<RecorderListener> listeners = [];

	@Requires({ ident != null })
	@Synchronized
	public void record(BasicBlockIdent ident) {
		// May occur several times for each bb...
		// http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/Instrumentation.html#redefineClasses%28java.lang.instrument.ClassDefinition...%29

		listeners.each { it.probeEvent(ident) }
	}
	public void addListener(RecorderListener listener) {
		listeners << listener
	}
}
