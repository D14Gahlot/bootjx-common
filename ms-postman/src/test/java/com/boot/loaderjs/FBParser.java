package com.boot.loaderjs;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Map;

public class FBParser {
	public static class Parser {

		public static Node parse(String expr) throws IOException {
			StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(expr));
			tokenizer.nextToken();
			Parser parser = new Parser(tokenizer);
			Node result = parser.parseExpression();
			if (tokenizer.ttype != StreamTokenizer.TT_EOF) {
				throw new RuntimeException("EOF expected, got "
						+ tokenizer.ttype + "/" + tokenizer.sval);
			}
			return result;
		}

		private StreamTokenizer tokenizer;

		private Parser(StreamTokenizer tokenizer) {
			this.tokenizer = tokenizer;
		}

		private Node parseExpression() throws IOException {
			Node left = parseAnd();
			if (tokenizer.ttype == StreamTokenizer.TT_WORD
					&& tokenizer.sval.equals("OR")) {
				tokenizer.nextToken();
				return new OperationNode(OperationNode.Type.OR,
						left, parseExpression());
			}
			return left;
		}

		private Node parseAnd() throws IOException {
			Node left = parseRelational();
			if (tokenizer.ttype == StreamTokenizer.TT_WORD
					&& tokenizer.sval.equals("AND")) {
				tokenizer.nextToken();
				return new OperationNode(OperationNode.Type.AND,
						left, parseAnd());
			}
			return left;
		}

		private Node parseRelational() throws IOException {
			Node left = parsePrimary();
			OperationNode.Type type;
			switch (tokenizer.ttype) {
			case '<':
				type = OperationNode.Type.LESS;
				break;
			case '=':
				type = OperationNode.Type.EQUALS;
				break;
			case '>':
				type = OperationNode.Type.GREATER;
				break;
			default:
				return left;
			}
			tokenizer.nextToken();
			return new OperationNode(type, left, parseRelational());
		}

		private Node parsePrimary() throws IOException {
			Node result;
			if (tokenizer.ttype == '(') {
				tokenizer.nextToken();
				result = parseExpression();
				if (tokenizer.ttype != ')') {
					throw new RuntimeException(") expected, got "
							+ tokenizer.ttype + "/" + tokenizer.sval);
				}
			} else if (tokenizer.ttype == '"' || tokenizer.ttype == '\'') {
				result = new LiteralNode(tokenizer.sval);
			} else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
				result = new LiteralNode(tokenizer.nval);
			} else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
				result = new FieldNode(tokenizer.sval);
			} else {
				throw new RuntimeException("Unrecognized token: "
						+ tokenizer.ttype + "/" + tokenizer.sval);
			}
			tokenizer.nextToken();
			return result;
		}
	}

	interface Node {
		Object eval(Map<String, Object> data);
	}

	public static class FieldNode implements Node {
		private String name;

		FieldNode(String name) {
			this.name = name;
		}

		public Object eval(Map<String, Object> data) {
			return data.get(name);
		}
	}

	public static class LiteralNode implements Node {
		private Object value;

		public LiteralNode(Object value) {
			this.value = value;
		}

		public Object eval(Map<String, Object> data) {
			return value;
		}
	}

	public static class OperationNode implements Node {
		enum Type {
			AND, OR, LESS, GREATER, EQUALS
		}

		private Type type;
		private Node leftChild;
		private Node rightChild;

		OperationNode(Type type, Node leftChild, Node rightChild) {
			this.type = type;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object eval(Map<String, Object> data) {
			Object left = leftChild.eval(data);
			Object right = rightChild.eval(data);
			switch (type) {
			case AND:
				return ((Boolean) left) && ((Boolean) right);
			case OR:
				return ((Boolean) left) || ((Boolean) right);
			case LESS:
				return ((Comparable) left).compareTo(right) < 0;
			case EQUALS:
				return left.equals(right);
			case GREATER:
				return ((Comparable) left).compareTo(right) > 0;
			default:
				throw new RuntimeException("Invalid op: " + type);
			}
		}
	}
}
