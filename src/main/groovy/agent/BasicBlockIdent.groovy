package agent

import groovy.transform.Immutable;

@Immutable
final class BasicBlockIdent {
	String className;
	String methodName;
	String methodSig;
	int position;

	int sourceStart;
	int sourceEnd;

	@Override
	public String toString() { return className + " " + methodName + " " + methodSig + " " + position + " " + sourceStart + " " + sourceEnd }
}
