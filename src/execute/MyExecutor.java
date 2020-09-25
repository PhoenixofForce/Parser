package execute;

import com.sun.istack.internal.Nullable;
import parser.Parser;
import parser.Tree;
import parser.TreeBuilder;
import scanner.Scanner;
import tokens.Token;
import java.util.List;
import java.util.Map;

public class MyExecutor extends Executor {

	private Map<String, String[]> tokenMap;
	public TagHandler tagHandler;

	public static void main(String[] args) {
		MyExecutor ex = new MyExecutor();
		ex.execute("a = (0.3-0.2); b = (3+a);");

		System.out.println("a = " + ex.tagHandler.getFloat("a"));	//a = 0.10000001
		System.out.println("b = " + ex.tagHandler.getFloat("b"));	//b = 3.1

		ex.executeFile("res/examples/example01");

		System.out.println("a = " + ex.tagHandler.getFloat("a"));	//a = 4.6
		System.out.println("b = " + ex.tagHandler.getFloat("b"));	//b = 7.6
		System.out.println("c = " + ex.tagHandler.getFloat("c"));	//c = -90.8
	}

	public MyExecutor() {
		this.tagHandler = new TagHandler();
		tokenMap = Token.loadTokenFile("res/token.tmap");
	}

	public void execute(String in) {
		List<Token> tokens = Scanner.scan(tokenMap, in);
		Tree t = TreeBuilder.toTree(Parser.parse("res/lang.gram", tokens), tokens);
		execute(t);
	}

	public void executeFile(String in) {
		List<Token> tokens = Scanner.scanFile(tokenMap, in);
		Tree t = TreeBuilder.toTree(Parser.parse("res/lang.gram", tokens), tokens);
		execute(t);
	}

	@Override
	public Object execute(Tree in) {
		switch(in.getValue()) {
			case "<S>":
				execute(in.getChild(0));
				break;
			case "<ASSERT>":
				executeAssert(in.getChild(0), in.getChild(2), in.getChildAmount() == 5? in.getChild(4): null);
				break;
			default:
				System.out.println(in.getValue());
				for(int i = 0; i < in.getChildAmount(); i++) {
					System.out.println("- " + i + " " + in.getChild(i).getValue());
				}
		}

		return null;
	}

	private void executeAssert(Tree id, Tree number, @Nullable  Tree nextAssert) {
		String variable = executeID(id);
		float value = executeNumber(number);

		tagHandler.setValue(variable, value);

		if(nextAssert != null) execute(nextAssert);
	}

	private String executeID(Tree id) {
		if(id instanceof Tree.Leaf) {
			Tree.Leaf l = (Tree.Leaf) id;
			return ((Token.IDToken) l.getToken()).getID();
		}

		return "ERROR";
	}

	private float executeNumber(Tree in) {
		if(in.getChildAmount() == 1) {
			return executeFloat(in.getChild(0));
		} else if(in.getChildAmount() == 3) {
			return executeLine(in.getChild(1));
		}

		return 0;
	}

	private float executeLine(Tree in) {
		float val = executeNumber(in.getChild(0));

		return executeLineMore(in.getChild(1), val);
	}

	private float executeLineMore(Tree in, float val) {
		float out = val;
		float val2 = executeLine_W_EPS(in.getChild(1));

		if(in.getChild(0).getValue().equalsIgnoreCase("PLUS")) out += val2;
		else out -= val2;

		return out;
	}

	private float executeLine_W_EPS(Tree in) {
		float value = executeNumber(in.getChild(0));

		if(in.getChildAmount() == 2) {
			value = executeLineMore_W_EPS(in.getChild(1), value);
		}

		return value;
	}

	private float executeLineMore_W_EPS(Tree in, float val) {
		float out = val;
		float val2 = executeLine_W_EPS(in.getChild(1));

		if(in.getChild(0).getValue().equalsIgnoreCase("PLUS")) out += val2;
		else out -= val2;

		return out;
	}

	private float executeFloat(Tree in) {
		if(in instanceof Tree.Leaf) {
			Tree.Leaf l = (Tree.Leaf) in;
			Token token = l.getToken();

			if(token instanceof Token.FloatToken) {
				return ((Token.FloatToken) l.getToken()).getFloat();
			} else if(token instanceof Token.IDToken) {
				return tagHandler.getFloat(((Token.IDToken) l.getToken()).getID());
			}
		}

		return 0;
	}
}
