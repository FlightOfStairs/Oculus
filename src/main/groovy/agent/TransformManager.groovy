package agent

import org.slf4j.LoggerFactory

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.generic.ClassGen
import org.apache.bcel.generic.MethodGen
import org.apache.bcel.verifier.structurals.ControlFlowGraph
import org.apache.bcel.verifier.structurals.InstructionContext

class TransformManager {

	public byte[] transform(String className, byte[] bytes) {

		ClassParser parser = new ClassParser(new ByteArrayInputStream(bytes), className);
		JavaClass clazz = parser.parse();

		ClassGen classGen = new ClassGen(clazz);

		for(def method : classGen.getMethods()) {

			MethodGen methodGen = new MethodGen(method, className, classGen.getConstantPool())
			
			def cfg = new ControlFlowGraph(methodGen)
		
			println blockStarts(cfg)
			
		}
		return clazz.getBytes();
	}

	public static List<Integer> blockStarts(ControlFlowGraph cfg) {
		def predecessorMap = [:]

		List<InstructionContext> contexts = cfg.getInstructionContexts().sort { it.getInstruction().position }

		contexts.each { pred ->
			if(! predecessorMap.containsKey(pred)) predecessorMap[pred] = []
			pred.successors.each {
				if(! predecessorMap.containsKey(it)) predecessorMap[it] = []
				predecessorMap[it] << pred
			}
		}

		def basicBlocks = [];

		def block = [];

		while(contexts.size() != 0) {
			InstructionContext current = contexts.remove(0);

			if(predecessorMap[current].size() >= 2) {
				basicBlocks << block
				block = []
			}

			block << current.getInstruction()

			if(contexts.size() == 0 ||
					current.successors.size() != 1 ||
					current.successors[0].instruction.position != contexts[0].instruction.position) {

				basicBlocks << block
				block = []
			}
		}
		
		return basicBlocks.collect { it[0].position }
	}
}