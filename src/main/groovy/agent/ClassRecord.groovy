package agent

import org.gcontracts.annotations.Requires
import org.gcontracts.annotations.Ensures

public class ClassRecord {
	public final String className;

	private final seenBlocks = [] as Set<BasicBlockIdent>
	private final unseenBlocks = [] as Set<BasicBlockIdent>

	@Requires({ className != null && className.length() != 0 && basicBlocks != null })
	public ClassRecord(String className, Collection<BasicBlockIdent> basicBlocks) {
		this.className = className

		unseenBlocks.addAll(basicBlocks)
	}
	
	@Requires({ block != null })
	@Ensures({ ! unseenBlocks.contains(block) && seenBlocks.contains(block) })
	public void seenBlock(BasicBlockIdent block) {
		unseenBlocks.remove(block)
		seenBlocks.add(block)
	}

	@Ensures({ result != null })
	public Set<BasicBlockIdent> remainingBlocks() {
		return unseenBlocks.clone();
	}

	@Override
	public String toString() {
		def used = seenBlocks.collectAll({ it.sourceStart..it.sourceEnd }).flatten() as Set
		def unused = unseenBlocks.collectAll({ it.sourceStart..it.sourceEnd }).flatten() as Set

		def partUsed = used.intersect(unused)

		unused.removeAll(partUsed)

		return "${className.replaceAll("/", ".")}\t${unused.sort()}\t${partUsed.sort()}"
	}
}
