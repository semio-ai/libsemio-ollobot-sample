package xyz.semio.script;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ScriptInterpreter {
  private Map<String, Object> _boundObjects = new HashMap<String, Object>();

  public boolean bind(final String name, final Object object) {
    return this._boundObjects.put(name, object) == null;
  }

  public boolean unbind(final String name) {
    return this._boundObjects.remove(name) != null;
  }

  public static class Token {
    enum Type {
      ID,
      INTEGER,
      REAL,
      STRING,
      OPEN_PAREN,
      CLOSE_PAREN,
      COMMA,
      EQ,
      DOT,
      ADD,
      SUB,
      MUL,
      DIV,
      MOD,
      LOGICAL_AND,
      LOGICAL_OR,
      LOGICAL_NOT,
      TRUE,
      FALSE,
      SEMICOLON,
      AT,
      GT,
      LT,
      GTE,
      LTE,
      EQEQ
    }

    private Type _type;
    private CharSequence _text;
    private int _startPosition;

    public Token(final Type type, final CharSequence text, final int startPosition) {
      this._type = type;
      this._text = text;
      this._startPosition = startPosition;
    }

    public Type getType() {
      return this._type;
    }

    public CharSequence getText() {
      return this._text;
    }

    public int getPosition() {
      return this._startPosition;
    }

    public static String getTypeString(final Type type) {
      switch(type) {
        case ID: return "ID";
        case INTEGER: return "INTEGER";
        case REAL: return "REAL";
        case STRING: return "STRING";
        case OPEN_PAREN: return "OPEN_PAREN";
        case CLOSE_PAREN: return "CLOSE_PAREN";
        case COMMA: return "COMMA";
        case TRUE: return "TRUE";
        case FALSE: return "FALSE";
        case LOGICAL_AND: return "LOGICAL_AND";
        case LOGICAL_OR: return "LOGICAL_OR";
        case LOGICAL_NOT: return "LOGICAL_NOT";
        case EQ: return "EQ";
        case DOT: return "DOT";
        case ADD: return "ADD";
        case SUB: return "SUB";
        case MUL: return "MUL";
        case DIV: return "DIV";
        case MOD: return "MOD";
        case SEMICOLON: return "SEMICOLON";
        case AT: return "AT";
        case GT: return "GT";
        case LT: return "LT";
        case GTE: return "GTE";
        case LTE: return "LTE";
        case EQEQ: return "EQEQ";
      }
      return "?";
    }

    public String toString() {
      return getTypeString(this._type) + " : " + this._text;
    }
  }

  private static boolean isNumber(final char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isAlpha(final char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private static boolean isWhitespace(final char c) {
    return c == ' ' || c == '\t' || c == '\n';
  }

  public class LexerException extends Throwable {
    /**
     *
     */
    private static final long serialVersionUID = -6324797096324212332L;
    private String _what;
    private int _where;

    LexerException(final String what, final int where) {
      this._what = what;
      this._where = where;
    }

    public String getWhat() {
      return this._what;
    }

    public int getWhere() {
      return this._where;
    }

    public String toString() {
      return this._what;
    }
  }

  public List<Token> lex(final CharSequence c) throws LexerException {
    List<Token> ret = new ArrayList<Token>();
    int i = 0;
    while(i < c.length()) {
      // ID
      if(isAlpha(c.charAt(i))) {
        int start = i++;
        while(i < c.length() && !isWhitespace(c.charAt(i)) && isAlpha(c.charAt(i))) ++i;
        if(i < c.length() && isNumber(c.charAt(i))) throw new LexerException("Numbers are not allowed in identifiers", i);

        Token.Type type = Token.Type.ID;
        CharSequence subSeq = c.subSequence(start, i);

        if(subSeq.toString().equals("true")) type = Token.Type.TRUE;
        else if(subSeq.toString().equals("false")) type = Token.Type.FALSE;

        ret.add(new Token(type, subSeq, start));
      }
      // INTEGER or FLOAT
      else if(isNumber(c.charAt(i)))
      {
        int start = i++;
        while(i < c.length() && isNumber(c.charAt(i))) ++i;

        Token.Type type = Token.Type.INTEGER;
        int end = i;
        // Real?
        if(i < c.length() && c.charAt(i) == '.') {
          ++i;
          type = Token.Type.REAL;
          while(i < c.length() && isNumber(c.charAt(i))) ++i;
          end = i;
        }
        if(i < c.length() && (isAlpha(c.charAt(i)) || c.charAt(i) == '.')) {
          String letter = "" + c.charAt(i);
          throw new LexerException("Unexpected symbol in " + (type == Token.Type.INTEGER ? "integer" : "real") + " '" + letter + "'", i);
        }
        ret.add(new Token(type, c.subSequence(start, end), start));
      }
      // STRING
      else if(c.charAt(i) == '"') {
        int start = i++;
        while(i < c.length() && c.charAt(i) != '"') ++i;
        ret.add(new Token(Token.Type.STRING, c.subSequence(start + 1, i), start));
        ++i;
      }
      // DOT
      else if(c.charAt(i) == '.')
      {
        ret.add(new Token(Token.Type.DOT, c.subSequence(i, i + 1), i));
        ++i;
      }
      // OPEN_PAREN
      else if(c.charAt(i) == '(')
      {
        ret.add(new Token(Token.Type.OPEN_PAREN, c.subSequence(i, i + 1), i));
        ++i;
      }
      // CLOSE_PAREN
      else if(c.charAt(i) == ')')
      {
        ret.add(new Token(Token.Type.CLOSE_PAREN, c.subSequence(i, i + 1), i));
        ++i;
      }
      // ADD
      else if(c.charAt(i) == '+')
      {
        ret.add(new Token(Token.Type.ADD, c.subSequence(i, i + 1), i));
        ++i;
      }
      // SUB
      else if(c.charAt(i) == '-')
      {
        ret.add(new Token(Token.Type.SUB, c.subSequence(i, i + 1), i));
        ++i;
      }
      // DIV
      else if(c.charAt(i) == '/')
      {
        ret.add(new Token(Token.Type.DIV, c.subSequence(i, i + 1), i));
        ++i;
      }
      // MUL
      else if(c.charAt(i) == '*')
      {
        ret.add(new Token(Token.Type.MUL, c.subSequence(i, i + 1), i));
        ++i;
      }
      // MOD
      else if(c.charAt(i) == '%')
      {
        ret.add(new Token(Token.Type.MOD, c.subSequence(i, i + 1), i));
        ++i;
      }
      // MOD
      else if(c.charAt(i) == '%')
      {
        ret.add(new Token(Token.Type.MOD, c.subSequence(i, i + 1), i));
        ++i;
      }
      // MOD
      else if(c.charAt(i) == ',')
      {
        ret.add(new Token(Token.Type.COMMA, c.subSequence(i, i + 1), i));
        ++i;
      }
      // MOD
      else if(c.charAt(i) == '=')
      {
        int start = i;
        if(i + 1 < c.length() && c.charAt(i + 1) == '=') ++i;
        ret.add(new Token(i == start ? Token.Type.EQ : Token.Type.EQEQ, c.subSequence(start, i + 1), i));
        ++i;
      }
      // LOGICAL_AND
      else if(c.charAt(i) == '&')
      {
        ret.add(new Token(Token.Type.LOGICAL_AND, c.subSequence(i, i + 1), i));
        ++i;
      }
      // LOGICAL_OR
      else if(c.charAt(i) == '|')
      {
        ret.add(new Token(Token.Type.LOGICAL_OR, c.subSequence(i, i + 1), i));
        ++i;
      }
      // SEMICOLON
      else if(c.charAt(i) == ';')
      {
        ret.add(new Token(Token.Type.SEMICOLON, c.subSequence(i, i + 1), i));
        ++i;
      }
      // LOGICAL_OR
      else if(c.charAt(i) == '!')
      {
        ret.add(new Token(Token.Type.LOGICAL_NOT, c.subSequence(i, i + 1), i));
        ++i;
      }
      // LOGICAL_OR
      else if(c.charAt(i) == '@')
      {
        ret.add(new Token(Token.Type.AT, c.subSequence(i, i + 1), i));
        ++i;
      }
      // GT
      else if(c.charAt(i) == '>')
      {
        int start = i;
        if(i + 1 < c.length() && c.charAt(i + 1) == '=') ++i;
        ret.add(new Token(i == start ? Token.Type.GT : Token.Type.GTE, c.subSequence(start, i + 1), i));
        ++i;
      }
      // LT
      else if(c.charAt(i) == '<')
      {
        int start = i;
        if(i + 1 < c.length() && c.charAt(i + 1) == '=') ++i;
        ret.add(new Token(i == start ? Token.Type.LT : Token.Type.LTE, c.subSequence(start, i + 1), i));
        ++i;
      }
      else if(isWhitespace(c.charAt(i)))
      {
        ++i;
      }
      // ?
      else
      {
        String sym = "" + c.charAt(i);
        throw new LexerException("Unexpected symbol '" + sym + "'", i);
      }
    }
    return ret;
  }

  public class ParserException extends Throwable {
    /**
     *
     */
    private static final long serialVersionUID = 3665927649802697801L;
    private String _what;
    private Token _where;

    ParserException(final String what, final Token where) {
      this._what = what;
      this._where = where;
    }

    public String getWhat() {
      return this._what;
    }

    public Token getWhere() {
      return this._where;
    }

    public String toString() {
      return this._what + " (at " + this._where + ")";
    }
  }

  public static class Node {
    enum Type {
      STMT_LIST,
      STMT,
      EXPR,
      LITERAL,
      ID,
      NEW,
      VARIABLE,
      UNARY_OPERATOR,
      BINARY_OPERATOR,
      PARAM_LIST,
      CALL
    }

    private Node _parent;
    private Type _type;
    private Token _terminal;

    Node(final Type type) {
      this._type = type;
    }

    Node(final Type type, final Token terminal) {
      this._type = type;
      this._terminal = terminal;
    }

    private List<Node> _nodes = new ArrayList<Node>();

    public void appendChild(final Node node) {
      node._parent = this;
      this._nodes.add(node);
    }

    public void prependChild(final Node node) {
      node._parent = this;
      this._nodes.add(0, node);
    }

    public void insertChild(final int i, final Node node) {
      node._parent = this;
      this._nodes.add(i, node);
    }

    public int getChildIndex(final Node node) {
      return this._nodes.indexOf(node);
    }

    public boolean removeChild(final Node node) {
      int beforeSize = this._nodes.size();
      this._nodes.remove(node);
      node._parent = null;
      return beforeSize != this._nodes.size();
    }

    // Performs a tree rewrite for lookahead cases to keep things greedy
    public void adopt(final Node node) {
      final Node parent = this._parent;
      int oldIndex = this._parent.getChildIndex(this);
      this._parent.removeChild(this);
      parent.insertChild(oldIndex, node);
      node.appendChild(this);
    }

    public List<Node> getChildren() {
      return this._nodes;
    }

    public Node getParent() {
      return this._parent;
    }

    public interface Visitor {
      public void visit(final Node node) throws Throwable;
    }

    public void visit(final Visitor visitor) throws Throwable {
      visitor.visit(this);
      for(Node child : _nodes) {
        child.visit(visitor);
      }
    }

    public Token getTerminal() {
      return this._terminal;
    }

    public Type getType() {
      return this._type;
    }

    public static String getTypeString(final Type type) {
      switch(type) {
        case STMT_LIST: return "STMT_LIST";
        case STMT: return "STMT";
        case EXPR: return "EXPR";
        case NEW: return "NEW";
        case LITERAL: return "LITERAL";
        case ID: return "ID";
        case UNARY_OPERATOR: return "UNARY_OPERATOR";
        case BINARY_OPERATOR: return "BINARY_OPERATOR";
        case PARAM_LIST: return "PARAM_LIST";
        case VARIABLE: return "VARIABLE";
        case CALL: return "CALL";
      }
      return "?";
    }

    public String toString() {
      String ret = getTypeString(this._type);
      if(this._terminal != null) ret += "(" + this._terminal.toString() + ")";
      if(_nodes.size() > 0) {
        ret += " [";
        for(Node node : _nodes) {
          ret += node.toString() + ", ";
        }
        ret = ret.substring(0, ret.length() - 2);
        ret += "]";
      }
      return ret;
    }

    public void prettyPrint(PrintStream out) {
      this._prettyPrint(out, 0);
    }

    private String _whitespace(int indent) {
      String ret = "";
      while(indent-- > 0) ret += " ";
      return ret;
    }

    private void _prettyPrint(PrintStream out, int depth) {
      String self = getTypeString(this._type);
      if(this._terminal != null) self += "(" + this._terminal.toString() + ")";
      out.println(_whitespace(depth) + self);
      for(Node child : this._nodes) {
        child._prettyPrint(out, depth + 1);
      }
    }
  }

  private Node paramList(List<Token> tokens) throws ParserException {
    Token open = tokens.remove(0);
    if(open.getType() != Token.Type.OPEN_PAREN) throw new ParserException("Expected (", open);

    Node ret = new Node(Node.Type.PARAM_LIST);

    Token tok = tokens.get(0);
    while(tok.getType() != Token.Type.CLOSE_PAREN) {
      ret.appendChild(expr(tokens, 0));

      if(tokens.size() == 0) {
        throw new ParserException("Unexpected end of parameter list", tok);
      }

      tok = tokens.get(0);
      if(tok.getType() == Token.Type.CLOSE_PAREN) {
        continue;
      }

      if(tok.getType() != Token.Type.COMMA) {
        throw new ParserException("Expected comma or closing parenthesis", tok);
      }
      tokens.remove(0);
      tok = tokens.get(0);
    }

    tokens.remove(0);

    return ret;
  }

  private final static Map<Token.Type, Integer> PRECEDENCE = new HashMap<Token.Type, Integer>();
  static {
    PRECEDENCE.put(Token.Type.LOGICAL_NOT, 2);

    PRECEDENCE.put(Token.Type.ADD, 2);
    PRECEDENCE.put(Token.Type.SUB, 2);

    PRECEDENCE.put(Token.Type.MUL, 3);
    PRECEDENCE.put(Token.Type.DIV, 3);
    PRECEDENCE.put(Token.Type.MOD, 3);

    PRECEDENCE.put(Token.Type.GT, 4);
    PRECEDENCE.put(Token.Type.GTE, 4);
    PRECEDENCE.put(Token.Type.LT, 4);
    PRECEDENCE.put(Token.Type.LTE, 4);

    PRECEDENCE.put(Token.Type.EQEQ, 5);

    PRECEDENCE.put(Token.Type.LOGICAL_AND, 6);
    PRECEDENCE.put(Token.Type.LOGICAL_OR, 6);

    PRECEDENCE.put(Token.Type.EQ, 7);

    PRECEDENCE.put(Token.Type.DOT, 10);
    PRECEDENCE.put(Token.Type.OPEN_PAREN, 10);
  }

  public Node expr(List<Token> tokens, int lastPrecedence) throws ParserException {
    if(tokens.size() == 0) {
      throw new ParserException("Expected expression, but got EOF", null);
    }
    Token first = tokens.remove(0);

    Node ret = new Node(Node.Type.EXPR);

    // Handle sub-expressions
    if(first.getType() == Token.Type.OPEN_PAREN) {
      List<Token> subexpr = new ArrayList<Token>();
      Token tok = tokens.get(0);
      int depth = 1;
      // TODO: This is a mess
      while(depth > 0 && tokens.size() > 0) {
        tokens.remove(0);
        boolean special = false;
        if(tok.getType() == Token.Type.OPEN_PAREN) { ++depth; special = true; }
        else if(tok.getType() == Token.Type.CLOSE_PAREN) { --depth; special = true; }
        if(!special) subexpr.add(tok);
        if(tokens.size() > 0) tok = tokens.get(0);
      }
      if(subexpr.isEmpty()) {
        throw new ParserException("Subexpression is empty", tok);
      }

      ret.appendChild(expr(subexpr, 0));
    } else if(first.getType() == Token.Type.LOGICAL_NOT) {
      ret.appendChild(expr(tokens, PRECEDENCE.get(Token.Type.LOGICAL_NOT)));
      ret.appendChild(new Node(Node.Type.UNARY_OPERATOR, first));
    } else if(first.getType() == Token.Type.REAL
      || first.getType() == Token.Type.INTEGER
      || first.getType() == Token.Type.STRING
      || first.getType() == Token.Type.TRUE
      || first.getType() == Token.Type.FALSE) {
      ret.appendChild(new Node(Node.Type.LITERAL, first));
    } else if(first.getType() == Token.Type.ID) {
      ret.appendChild(new Node(Node.Type.VARIABLE, first));
    } else if(first.getType() == Token.Type.AT) {
      ret.appendChild(new Node(Node.Type.NEW, first));
    }

    Token second = tokens.size() > 0 ? tokens.get(0) : null;
    Node parent = ret;

    while(second != null && (second.getType() == Token.Type.ADD
      || second.getType() == Token.Type.SUB
      || second.getType() == Token.Type.MUL
      || second.getType() == Token.Type.DIV
      || second.getType() == Token.Type.MOD
      || second.getType() == Token.Type.LOGICAL_AND
      || second.getType() == Token.Type.LOGICAL_OR
      || second.getType() == Token.Type.DOT
      || second.getType() == Token.Type.EQ
      || second.getType() == Token.Type.OPEN_PAREN
      || second.getType() == Token.Type.GT
      || second.getType() == Token.Type.LT
      || second.getType() == Token.Type.GTE
      || second.getType() == Token.Type.LTE
      || second.getType() == Token.Type.EQEQ)) {
      int precedence = PRECEDENCE.get(second.getType());
      if(lastPrecedence >= precedence) break;

      tokens.remove(0); // pop off op


      Node binOp = new Node(Node.Type.BINARY_OPERATOR, second);
      Node expr2 = new Node(Node.Type.EXPR);

      List<Token> rhs = tokens;

      if(second.getType() == Token.Type.OPEN_PAREN) {
        List<Token> params = new ArrayList<Token>();
        Token tok = tokens.remove(0);
        params.add(second);
        int depth = 1;
        for(; depth > 0; tok = tokens.remove(0)) {
          if(tok.getType() == Token.Type.OPEN_PAREN) ++depth;
          if(tok.getType() == Token.Type.CLOSE_PAREN) --depth;
          params.add(tok);
          if(tokens.size() == 0) break;
        }
        tokens.add(0, tok);

        if(depth != 0) {
          throw new ParserException("Unexpected end of parameter list", tok);
        }


        expr2.appendChild(parent);
        expr2.appendChild(paramList(params));
        expr2.appendChild(new Node(Node.Type.CALL));

        parent = expr2;
        second = tokens.size() > 0 ? tokens.get(0) : null;
        continue;
      }

      // TODO: Right associativity
      final boolean rightAssoc = binOp.getTerminal().getType() == Token.Type.EQ;

      expr2.appendChild(parent);
      expr2.appendChild(expr(rhs, precedence));
      expr2.appendChild(binOp);

      parent = expr2;

      second = tokens.size() > 0 ? tokens.get(0) : null;
    }

    return parent;
  }

  public Node stmt(List<Token> tokens) throws ParserException {
    Node ret = new Node(Node.Type.STMT);
    Token first = tokens.get(0);
    ret.appendChild(expr(tokens, 0));


    if(tokens.size() == 0 || tokens.get(0).getType() != Token.Type.SEMICOLON) {
      throw new ParserException("Expected semicolon after statement expression", first);
    }
    tokens.remove(0);
    return ret;
  }

  public Node stmtList(List<Token> tokens) throws ParserException {
    Node ret = new Node(Node.Type.STMT_LIST);
    while(tokens.size() > 0) {
      ret.appendChild(stmt(tokens));
    }
    return ret;
  }

  public Node parse(List<Token> tokens) throws ParserException {
    return stmtList(tokens);
  }

  public static class CastException extends Throwable {
    private static final long serialVersionUID = 5889315799038719774L;
    private Instance _instance;
    private Instance.Type _type;

    CastException(final Instance instance, final Instance.Type type) {
      this._instance = instance;
      this._type = type;
    }

    public Instance getInstance() {
      return this._instance;
    }

    public Instance.Type getType() {
      return this._type;
    }

    public String toString() {
      if(this._instance == null) return "?";
      return "Failed to cast " + this._instance.getToken().getText() + " to " + Instance.getTypeString(this._type);
    }
  }

  public static class BinaryOperationException extends Throwable {
    private Instance _lhs;
    private Instance _rhs;
    private Token.Type _op;

    BinaryOperationException(final Instance lhs, final Instance rhs, final Token.Type op) {
      this._lhs = lhs;
      this._rhs = rhs;
      this._op = op;
    }

    public Instance getLhs() {
      return this._lhs;
    }

    public Instance getRhs() {
      return this._rhs;
    }

    public Token.Type getOperation() {
      return this._op;
    }

    public String toString() {
      return "Failed to apply operation " + Token.getTypeString(_op) + " to operands " + _lhs + " and " + _rhs;
    }
  }

  public static class UndefinedException extends Throwable {
    private static final long serialVersionUID = -7762388700371858228L;
    private Token _token;

    UndefinedException(final Token token) {
      this._token = token;
    }

    public Token getToken() {
      return this._token;
    }

    public String toString() {
      return "Undefined " + this._token.getText();
    }
  }

  public static interface Function {
    Instance apply(final Instance callee, final Instance[] params) throws Throwable;
  }

  public static class Instance {
    public enum Type {
      UNDEFINED,
      OBJ,
      FUNCTION,
      INTEGER,
      REAL,
      STRING,
      BOOLEAN,
      INTERNAL
    }

    private Type _type;
    private Object _value;
    private Token _token;
    private Object _internalData;

    Instance() {
      this._type = Type.UNDEFINED;
    }

    Instance(final Token token) {
      this._type = Type.UNDEFINED;
      this._token = token;
    }

    Instance(final Type type, final Object value) {
      this._type = type;
      this._value = value;
    }

    Instance(final Type type, final Object value, final Token token) {
      this._type = type;
      this._value = value;
      this._token = token;
    }

    public static String getTypeString(final Type type) {
      switch(type) {
        case UNDEFINED: return "Undefined";
        case OBJ: return "Object";
        case FUNCTION: return "Function";
        case INTEGER: return "Integer";
        case REAL: return "Real";
        case STRING: return "String";
        case BOOLEAN: return "Boolean";
        case INTERNAL: return "Internal";
      }
      return "?";
    }

    public Type getType() {
      return this._type;
    }

    public void setType(final Type type) {
      this._type = type;
    }

    public Object getValue() {
      return this._value;
    }

    public void setValue(final Object value) {
      this._value = value;
    }

    public Token getToken() {
      return this._token;
    }

    public void setToken(final Token token) {
      this._token = token;
    }

    public void setInternalData(final Object internalData) {
      this._internalData = internalData;
    }

    public Object getInternalData() {
      return this._internalData;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Instance> getObjectValue() throws CastException {
      if(this._type == Type.OBJ) return (Map<String, Instance>)this._value;
      throw new CastException(this, Type.OBJ);
    }

    public boolean getBooleanValue() throws CastException {
      if(this._type == Type.BOOLEAN) return (Boolean)this._value;
      if(this._type == Type.INTEGER) return (Integer)this._value != 0;
      throw new CastException(this, Type.BOOLEAN);
    }

    public int getIntegerValue() throws CastException {
      if(this._type == Type.BOOLEAN) return (Boolean)this._value ? 1 : 0;
      if(this._type == Type.INTEGER) return (Integer)this._value;
      throw new CastException(this, Type.INTEGER);
    }

    public double getRealValue() throws CastException {
      if(this._type == Type.REAL) return (Double)this._value;
      if(this._type == Type.INTEGER) return (Integer)this._value;
      throw new CastException(this, Type.REAL);
    }

    public String getStringValue() throws CastException {
      if(this._type == Type.STRING) return (String)this._value;
      if(this._type == Type.REAL) return Double.toString((Double)this._value);
      if(this._type == Type.INTEGER) return Integer.toString((Integer)this._value);
      if(this._type == Type.BOOLEAN) return Boolean.toString((Boolean)this._value);
      throw new CastException(this, Type.STRING);
    }

    public Function getFunctionValue() throws CastException {
      if(this._type == Type.FUNCTION) return (Function)this._value;
      throw new CastException(this, Type.FUNCTION);
    }

    public Object castTo(final Class<?> clazz) throws CastException {
      final String n = clazz.getCanonicalName();
      if(n.equals("int") || n.equals("java.lang.Integer")) return this.getIntegerValue();
      if(n.equals("float") || n.equals("double") || n.equals("java.lang.Float") || n.equals("java.lang.Double")) return this.getRealValue();
      if(n.equals("boolean") || n.equals("java.lang.Boolean")) return this.getBooleanValue();
      if(n.equals("java.lang.String")) return this.getStringValue();

      throw new CastException(this, Instance.Type.UNDEFINED);
    }

    public static Instance castFrom(final Object obj) throws CastException {
      if(obj == null) return new Instance(Type.UNDEFINED, obj);
      final String n = obj.getClass().getCanonicalName();
      if(n.equals("int") || n.equals("java.lang.Integer")) return new Instance(Type.INTEGER, obj);
      if(n.equals("float") || n.equals("double") || n.equals("java.lang.Float") || n.equals("java.lang.Double")) return new Instance(Type.REAL, obj);
      if(n.equals("boolean") || n.equals("java.lang.Boolean")) return new Instance(Type.BOOLEAN, obj);
      if(n.equals("java.lang.String")) return new Instance(Type.STRING, obj);

      throw new CastException(null, Instance.Type.UNDEFINED);
    }

    public static Instance fromLiteral(final Token terminal) {
      final Token t = terminal;
      if(t.getType() == Token.Type.INTEGER) return new Instance(Instance.Type.INTEGER, Integer.parseInt(t.getText().toString()), terminal);
      if(t.getType() == Token.Type.REAL) return new Instance(Instance.Type.REAL, Double.parseDouble(t.getText().toString()), terminal);
      if(t.getType() == Token.Type.STRING) return new Instance(Instance.Type.STRING, t.getText().toString(), terminal);
      if(t.getType() == Token.Type.TRUE) return new Instance(Instance.Type.BOOLEAN, true, terminal);
      if(t.getType() == Token.Type.FALSE) return new Instance(Instance.Type.BOOLEAN, false, terminal);
      return null;
    }

    public String toString() {
      return this._value == null ? "undefined" : this._value.toString();
    }
  }

  private List<Binder> _bindings = new ArrayList<Binder>();

  public interface Binder {
    String getName();
    Map<String, Instance> getBinds();
  }

  public static class InvalidParameterList extends Throwable {
    private static final long serialVersionUID = -3265382539043397382L;
    private String _what;
    private Instance _where;

    InvalidParameterList(final String what, final Instance where) {
      this._what = what;
      this._where = where;
    }

    public String getWhat() {
      return this._what;
    }

    public Instance getWhere() {
      return this._where;
    }

    @Override
    public String toString() {
      return this._what;
    }
  }

  private static class BindProxy implements Binder {
    private Object _target;
    private String _name;

    private class BindData {
      public Method method;
      public Object that;
    }

    BindProxy(final Object target, final String name) {
      this._target = target;
      this._name = name;
    }

    public Object getTarget() {
      return this._target;
    }

    public String getName() {
      return this._name;
    }

    @Override
    public Map<String, Instance> getBinds() {
      Map<String, Instance> ret = new HashMap<String, Instance>();
      Method[] methods = this._target.getClass().getMethods();
      for(final Method method : methods) {
        BindData data = new BindData();
        data.method = method;
        data.that = this._target;
        Instance i = new Instance(Instance.Type.FUNCTION, new Function() {
          @Override
          public Instance apply(final Instance callee, final Instance[] params) throws Throwable {
            BindData data = (BindData)callee.getInternalData();
            Class<?>[] methodParams = data.method.getParameterTypes();
            if(params.length != methodParams.length) {
              throw new InvalidParameterList(data.method.getName() + " expected " + methodParams.length + " parameters, but got " + params.length, callee);
            }
            Object to[] = new Object[methodParams.length];
            for(int i = 0; i < to.length; ++i) {
              to[i] = params[i].castTo(methodParams[i]);
            }
            Object ret = method.invoke(data.that, to);
            return Instance.castFrom(ret);
          }
        });
        i.setInternalData(data);
        ret.put(method.getName(), i);
      }
      return ret;
    }
  }

  public void addBinding(final Object binding, final String name) {
    this._bindings.add(new BindProxy(binding, name));
  }

  public void execute(final String script) throws Throwable {
    Node ast = parse(lex(script));
    ast.prettyPrint(System.out);

    final Map<String, Instance> heap = new HashMap<String, Instance>();

    for(Binder bind : this._bindings) {
      heap.put(bind.getName(), new Instance(Instance.Type.OBJ, bind.getBinds()));
    }

    final Stack<Instance> stack = new Stack<Instance>();

    ast.visit(new Node.Visitor() {
      private Instance computeBinaryOperation(final Token.Type op) throws CastException, BinaryOperationException {
        Instance rhs = stack.pop();
        Instance lhs = stack.pop();

        if(op == Token.Type.ADD && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.INTEGER, lhs.getIntegerValue() + rhs.getIntegerValue());
        }
        if(op == Token.Type.ADD && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.REAL, lhs.getRealValue() + rhs.getRealValue());
        }
        if(op == Token.Type.ADD && lhs.getType() == Instance.Type.STRING) {
          return new Instance(Instance.Type.STRING, lhs.getStringValue() + rhs.getStringValue());
        }
        if(op == Token.Type.SUB && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.INTEGER, lhs.getIntegerValue() - rhs.getIntegerValue());
        }
        if(op == Token.Type.SUB && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.REAL, lhs.getRealValue() - rhs.getRealValue());
        }
        if(op == Token.Type.MUL && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.INTEGER, lhs.getIntegerValue() * rhs.getIntegerValue());
        }
        if(op == Token.Type.MUL && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.REAL, lhs.getRealValue() * rhs.getRealValue());
        }
        if(op == Token.Type.DIV && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.INTEGER, lhs.getIntegerValue() / rhs.getIntegerValue());
        }
        if(op == Token.Type.DIV && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.REAL, lhs.getRealValue() / rhs.getRealValue());
        }
        if(op == Token.Type.MOD && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.INTEGER, lhs.getIntegerValue() % rhs.getIntegerValue());
        }
        if(op == Token.Type.GT && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getIntegerValue() > rhs.getIntegerValue());
        }
        if(op == Token.Type.LT && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getIntegerValue() < rhs.getIntegerValue());
        }
        if(op == Token.Type.GT && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getRealValue() > rhs.getRealValue());
        }
        if(op == Token.Type.LT && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getRealValue() < rhs.getRealValue());
        }
        if(op == Token.Type.GTE && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getIntegerValue() >= rhs.getIntegerValue());
        }
        if(op == Token.Type.LTE && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getIntegerValue() <= rhs.getIntegerValue());
        }
        if(op == Token.Type.GTE && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getRealValue() >= rhs.getRealValue());
        }
        if(op == Token.Type.LTE && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getRealValue() <= rhs.getRealValue());
        }
        if(op == Token.Type.EQEQ && lhs.getType() == Instance.Type.REAL) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getRealValue() == rhs.getRealValue());
        }
        if(op == Token.Type.EQEQ && lhs.getType() == Instance.Type.INTEGER) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getIntegerValue() == rhs.getIntegerValue());
        }
        if(op == Token.Type.EQEQ && lhs.getType() == Instance.Type.BOOLEAN) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getBooleanValue() == rhs.getBooleanValue());
        }
        if(op == Token.Type.LOGICAL_AND && lhs.getType() == Instance.Type.BOOLEAN) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getBooleanValue() && rhs.getBooleanValue());
        }
        if(op == Token.Type.LOGICAL_OR && lhs.getType() == Instance.Type.BOOLEAN) {
          return new Instance(Instance.Type.BOOLEAN, lhs.getBooleanValue() || rhs.getBooleanValue());
        }
        if(op == Token.Type.DOT) {
          if(lhs.getType() != Instance.Type.OBJ) {
            throw new CastException(lhs, Instance.Type.OBJ);
          }
          Map<String, Instance> map = lhs.getObjectValue();
          Instance i = map.get(rhs.getToken().getText().toString());
          if(i == null) map.put(rhs.getToken().getText().toString(), i = new Instance(rhs.getToken()));
          return i;
        }
        if(op == Token.Type.EQ) {
          lhs.setType(rhs.getType());
          lhs.setValue(rhs.getValue());
          return lhs;
        }

        throw new BinaryOperationException(lhs, rhs, op);

      }

      private Instance computeUnnaryOperation(final Token.Type op) throws CastException {
        Instance rhs = stack.pop();
        if(op == Token.Type.LOGICAL_NOT) {
          return new Instance(Instance.Type.BOOLEAN, !rhs.getBooleanValue());
        }
        return null;
      }

      private Instance call() throws Throwable {
        List<Instance> instances = new ArrayList<Instance>();
        Instance i = null;
        while((i = stack.pop()).getType() != Instance.Type.INTERNAL) instances.add(i);
        Instance func = stack.pop();
        Instance[] params = new Instance[instances.size()];
        for(int it = 0; it < params.length; ++it) {
          params[it] = instances.get(params.length - it - 1);
        }

        return func.getFunctionValue().apply(func, params);
      }

      @Override
      public void visit(final Node node) throws Throwable {
        if(node.getType() == Node.Type.LITERAL) {
          stack.push(Instance.fromLiteral(node.getTerminal()));
        } else if(node.getType() == Node.Type.VARIABLE) {
          final String name = node.getTerminal().getText().toString();
          Instance inst = heap.get(name);
          if(inst == null) heap.put(name, inst = new Instance(node.getTerminal()));
          stack.push(inst);
        } else if(node.getType() == Node.Type.NEW) {
          stack.push(new Instance(Instance.Type.OBJ, new HashMap<String, Instance>(), node.getTerminal()));
        } else if(node.getType() == Node.Type.BINARY_OPERATOR) {
          stack.push(this.computeBinaryOperation(node.getTerminal().getType()));
        } else if(node.getType() == Node.Type.UNARY_OPERATOR) {
          stack.push(this.computeUnnaryOperation(node.getTerminal().getType()));
        } else if(node.getType() == Node.Type.STMT) {
          stack.clear();
        } else if(node.getType() == Node.Type.PARAM_LIST) {
          stack.push(new Instance(Instance.Type.INTERNAL, null));
        } else if(node.getType() == Node.Type.CALL) {
          stack.push(call());
        }
      }
    });

    System.out.println(stack);
  }
}
