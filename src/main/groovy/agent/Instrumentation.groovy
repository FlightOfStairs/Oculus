package agent

import org.apache.bcel.generic.ClassGen
import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.generic.MethodGen
import org.apache.bcel.verifier.structurals.ControlFlowGraph
import org.apache.bcel.verifier.structurals.InstructionContext
import org.gcontracts.annotations.Ensures
import org.apache.bcel.generic.InstructionList
import org.apache.bcel.generic.InstructionFactory
import org.apache.bcel.generic.Type
import org.apache.bcel.Constants
import org.apache.bcel.generic.PUSH

public class Instrumentation {

	public final String className;
	private final byte[] bytes;

	public Instrumentation(String className, byte[] bytes) {
		this.className = className;
		this.bytes = bytes;
	}

	@Ensures({ result != null })
	public byte[] instrument(List<BasicBlockIdent> basicBlocks) {
		ClassGen classGen = classGen();


		for(def method : classGen.getMethods()) {
			def methodBlocks = basicBlocks.findAll {
				it.className == className && it.methodName == method.getName() && it.methodSig == it.methodSig
			}
			
			def handleInserts = [:]

			if(methodBlocks.isEmpty()) continue

			MethodGen methodGen = new MethodGen(method, className, classGen.getConstantPool())


			methodBlocks.each { block ->
			
				InstructionList instructionList = new InstructionList()
				InstructionFactory instructionFactory = new InstructionFactory(classGen)

				instructionList.append(new PUSH(classGen.getConstantPool(), block.toString()))
				instructionList.append(instructionFactory.createInvoke("agent.RecorderSingleton", "record", Type.VOID, [Type.STRING] as Type[], Constants.INVOKESTATIC))

				def handle = methodGen.getInstructionList().findHandle(block.position)

				handleInserts[handle] = instructionList
			}
			
			handleInserts.each { handle, code ->
				methodGen.getInstructionList().insert(handle, code)
			}

			classGen.removeMethod(method);
			methodGen.setMaxStack();
			classGen.addMethod(methodGen.getMethod());
		}

		classGen.getJavaClass().dump("HelloWorld.class");
		
		return classGen.getJavaClass().getBytes()
	}

	@Ensures({ result != null })
	public List<BasicBlockIdent> getBasicBlocks() {
		ClassGen classGen = classGen();

		def bbIdents = [];

		for(def method : classGen.getMethods()) {

			MethodGen methodGen = new MethodGen(method, className, classGen.getConstantPool())

			def cfg = new ControlFlowGraph(methodGen)

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

			bbIdents.addAll(basicBlocks.collect { new BasicBlockIdent(className, method.getName(), method.getSignature(), it.first().position, it.size()) })
		}
		
		return bbIdents
	}

	private ClassGen classGen() {
		ClassParser parser = new ClassParser(new ByteArrayInputStream(bytes), className);
		JavaClass clazz = parser.parse();

		return new ClassGen(clazz);
	}
}