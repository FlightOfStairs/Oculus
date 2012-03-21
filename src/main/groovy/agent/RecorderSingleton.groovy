package agent;


import groovy.transform.Synchronized
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires;

public enum RecorderSingleton {
	INSTANCE;

	public static void record(String s) {
		def parts = s.split(" ")
		INSTANCE.record(new BasicBlockIdent(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
	}

	private final Map<String, List<BasicBlockIdent>> probesRemaining = [:];

	private final List<RecorderListener> listeners = [];

	@Requires({ ident != null &&  probesRemaining.containsKey(ident.className) })
	@Synchronized
	public void record(BasicBlockIdent ident) {
		probesRemaining[ident.className].remove(ident);

		listeners.each { it.probeEvent(ident) }
	}

	@Requires( { idents != null })
	@Ensures({ idents.collect({ it.className }).every { probesRemaining.containsKey(it) } })
	public void addBlocks(Collection<BasicBlockIdent> idents) {
		idents.each { 
			if(! probesRemaining.containsKey(it.className))
				probesRemaining[it.className] = []
			probesRemaining[it.className] << it
		}
	}

	public void addListener(RecorderListener listener) {
		listeners << listener
	}

	@Ensures({ result != null })
	public List<BasicBlockIdent> basicBlocksRemaining(String className) {
		return probesRemaining[className].clone();
	}
}
