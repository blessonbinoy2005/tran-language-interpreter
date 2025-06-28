import AST.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Parser {

    private TokenManager tokenManager;
    private TranNode tranNode;

    public Parser(TranNode top, List<Token> tokens) {
        this.tranNode = top;
        this.tokenManager = new TokenManager(tokens);
    }

    // Tran = { Class | Interface }
    public void Tran() throws SyntaxErrorException {
        tranNode.Interfaces = new LinkedList<>();
        if  (tokenManager.matchAndRemove(Token.TokenTypes.INTERFACE).isPresent()) { //while
            tranNode.Interfaces.add(ParseInterface());
            if (!tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
                throw new SyntaxErrorException("No dedent found", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            while ((!tokenManager.done()) && tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE ).isPresent()) {
                if (tokenManager.matchAndRemove(Token.TokenTypes.INTERFACE).isPresent()) {
                    tranNode.Interfaces.add(ParseInterface());
                    if (!tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
                        throw new SyntaxErrorException("No dedent found", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                    }
                }
            }
        }
        if ((!tokenManager.done()) && tokenManager.matchAndRemove(Token.TokenTypes.CLASS).isPresent()) {
            tranNode.Classes = new LinkedList<>();
            tranNode.Classes.add(ParseClass());
        }
    }

    //TODO
    public ClassNode ParseClass() throws SyntaxErrorException {
        ClassNode classNode = new ClassNode();
        Optional <Token> opt =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (opt.isPresent()) {
            Token token = opt.get();
            classNode.name = token.getValue();
        } else {
            throw new SyntaxErrorException("No word has found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }

        if (tokenManager.matchAndRemove(Token.TokenTypes.IMPLEMENTS).isPresent()) {
            classNode.interfaces = new LinkedList<>();
            Optional <Token> interfaceName =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
            if (interfaceName.isPresent()) {
                Token token = interfaceName.get();
                classNode.interfaces.add(token.getValue());
//                classNode.interfaces.add(interfaceName.get().toString());
            } else {
                throw new SyntaxErrorException("No word has found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
//                if (tokenManager.matchAndRemove(Token.TokenTypes.IMPLEMENTS).isPresent()) {
                    Optional <Token> MoreinterfaceName =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
                    if (MoreinterfaceName.isPresent()) {
                        Token token = MoreinterfaceName.get();
                        classNode.interfaces.add(token.getValue());
//                        classNode.interfaces.add(MoreinterfaceName.get().toString());
                    } else {
                        throw new SyntaxErrorException("No interfaces found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                    }
//                } else {
//                    throw new SyntaxErrorException("No implement identifier found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//                }
            }
        }

        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
            throw new SyntaxErrorException("No indent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }

//        Optional <Token> peekToken = tokenManager.peek(0);
//        Token PeekForWord = peekToken.get();

        while (!tokenManager.done() && tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty() ) {
            Optional <Token> peekToken = tokenManager.peek(0);
            Token PeekForWord = peekToken.get();
            //peekToken.isPresent() && PeekForWord.getType() != Token.TokenTypes.DEDENT
            if (tokenManager.matchAndRemove(Token.TokenTypes.CONSTRUCT).isPresent()) {
//                classNode.constructors = new LinkedList<>();
                classNode.constructors.add(ParseConstructor());
            } else if (PeekForWord.getType() == Token.TokenTypes.SHARED || PeekForWord.getType() == Token.TokenTypes.PRIVATE ||
                    tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)) {
//                classNode.methods = new LinkedList<>();
                classNode.methods.add(ParseMethodDeclaration());
            } else if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
//                classNode.members = new LinkedList<>();
                classNode.members.add(ParseMember());


            } else if (!tokenManager.done()) {
                throw new SyntaxErrorException("No dedent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }

        return classNode;
    }

    public MemberNode ParseMember() throws SyntaxErrorException {
        MemberNode memberNode = new MemberNode();
        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            memberNode.declaration = ParseVariableDeclaration();
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.NEWLINE, Token.TokenTypes.INDENT)) {
                tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE);
                tokenManager.matchAndRemove(Token.TokenTypes.INDENT);
                if (tokenManager.matchAndRemove(Token.TokenTypes.ACCESSOR).isPresent()) {
                    if (!tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
                        throw new SyntaxErrorException("No colon found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                    }
                    //memberNode.accessor = Optional.of(ParseStatements());
                }
                if (tokenManager.matchAndRemove(Token.TokenTypes.MUTATOR).isPresent()) {
                    if (!tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
                        throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                    }
                    //memberNode.mutator = Optional.of(ParseStatements());
                }
                //TODO right here parser2 test after last member it checks for dedent not a newline
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
//                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//                if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent() || !tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
//                    throw new SyntaxErrorException("No dedent or newline found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//                }
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {

            } else {
                throw new SyntaxErrorException("No dedent or newline found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }
        return memberNode;
    }

    //TODO
    public ConstructorNode ParseConstructor() throws SyntaxErrorException {
        ConstructorNode constructorNode = new ConstructorNode();
        if (!tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
            throw new SyntaxErrorException("No left curly found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            constructorNode.parameters.addAll(ParseVariableDeclarations());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
            throw new SyntaxErrorException("No right curly found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            throw new SyntaxErrorException("No new line found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
            throw new SyntaxErrorException("No indent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }

        while (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            constructorNode.locals.add(ParseVariableDeclaration());
            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                throw new SyntaxErrorException("No new line found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }

        constructorNode.statements = new ArrayList<>();
        while (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
//            constructorNode.statements.addAll(ParseStatements());
            constructorNode.statements.add(ParseStatement());
            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }
//        constructorNode.statements.addAll(ParseStatements());
        if (!tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
            throw new SyntaxErrorException("No dedent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }

        return constructorNode;
    }

    //TODO
    public ArrayList<StatementNode> ParseStatements() throws SyntaxErrorException {
        ArrayList<StatementNode> statementNodes = new ArrayList<>();
        Optional <Token> opt = tokenManager.peek(0);
        Token peekOpt = opt.get();
        if (!tokenManager.matchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
            throw new SyntaxErrorException("No indent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        while (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT)) {

            statementNodes.add(ParseStatement());
            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()
                    && tokenManager.peek(0).get().getType() != Token.TokenTypes.DEDENT) {
                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }
        //TODO changed this
        if (!tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
            throw new SyntaxErrorException(";( No dedent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        return statementNodes;
    }

    //TODO
    public StatementNode ParseStatement() throws SyntaxErrorException {
        if (tokenManager.matchAndRemove(Token.TokenTypes.IF).isPresent()) {
            StatementNode statementIfNode = new IfNode();
            statementIfNode = ParseIf();
            return statementIfNode;
        } if (tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP ||
                (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN) && tokenManager.peek(3).get().getType() == Token.TokenTypes.LOOP)) {
            StatementNode statementLoopNode = new LoopNode();
            statementLoopNode = ParseLoop();
            return statementLoopNode;
        }
//        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
//            StatementNode statemenAssignmentNode = new AssignmentNode();
//            statemenAssignmentNode = ParseAssignment();
//            return statemenAssignmentNode;
//        }
        else {
            StatementNode statementNodeNode = disambiguate().get();
            return statementNodeNode;
        }

//        return null;
    }

    public Optional<StatementNode> disambiguate() throws SyntaxErrorException {
        Optional <MethodCallExpressionNode> opt = ParseMethodCallExpression();
        if (opt.isPresent()) {
            MethodCallStatementNode methodCallStatementNode = new MethodCallStatementNode(opt.get());
            return Optional.of(methodCallStatementNode);
        }
        Optional opt2 = ParseMethodCallExpression();
        LinkedList<VariableReferenceNode> returnValueList = new LinkedList<>();
        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
            StatementNode statemenAssignmentNode = new AssignmentNode();
            statemenAssignmentNode = ParseAssignment();
//            if (statemenAssignmentNode instanceof MethodCallExpressionNode) {
//                MethodCallStatemsentNode methodCallStatementNode = new MethodCallStatementNode(statemenAssignmentNode());
//            }
            return Optional.of(statemenAssignmentNode);
        } else if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.COMMA)) {
            MethodCallStatementNode methodCallStatementNode = ParseMethodCall();
            return Optional.of(methodCallStatementNode);
        }
        return Optional.empty();
    }

    public MethodCallStatementNode ParseMethodCall() throws SyntaxErrorException {
        LinkedList<VariableReferenceNode> returnValueList = new LinkedList<>();
//        MethodCallStatementNode methodCallStatementNode = new MethodCallStatementNode();
        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD) {
            returnValueList.add(ParseVariableReferenceNode());
//            methodCallStatementNode.returnValues.add(ParseVariableReferenceNode());
            while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
                returnValueList.add(ParseVariableReferenceNode());
//                methodCallStatementNode.returnValues.add(ParseVariableReferenceNode());
            }
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.ASSIGN).isPresent()) {
            throw new SyntaxErrorException("No assignment found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        MethodCallStatementNode methodCallStatementNode = new MethodCallStatementNode(ParseMethodCallExpression().get());
        methodCallStatementNode.returnValues.addAll(returnValueList);
        return methodCallStatementNode;
    }

    //TODO
    public IfNode ParseIf() throws SyntaxErrorException {
        IfNode ifNode = new IfNode();
//        ifNode.condition = ParseExpression();
        ifNode.condition = ParseBoolExpTerm();
        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        ifNode.statements = new ArrayList<>();
//        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
//            ifNode.statements.addAll(ParseStatements());
//        }

        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
                || tokenManager.peek(0).get().getType() == Token.TokenTypes.INDENT) {
            ifNode.statements.addAll(ParseStatements());
        }

//            ifNode.statements.addAll(ParseStatements());
        if (tokenManager.matchAndRemove(Token.TokenTypes.ELSE).isPresent()) {
            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            ElseNode elseNode = new ElseNode();
            elseNode.statements = new ArrayList<>();

            if (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                    || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                    || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
                    || tokenManager.peek(0).get().getType() == Token.TokenTypes.INDENT) {
                elseNode.statements.addAll(ParseStatements());
            }


            ifNode.elseStatement = Optional.of(elseNode);

        } else {
            ifNode.elseStatement = Optional.empty();
        }
        return ifNode;
    }

    //TODO
    public LoopNode ParseLoop() throws SyntaxErrorException {
        LoopNode loopNode = new LoopNode();

        if (tokenManager.matchAndRemove(Token.TokenTypes.LOOP).isPresent()) {
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
                VariableReferenceNode variableReferenceNode = new VariableReferenceNode();
                variableReferenceNode.name = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
                loopNode.assignment = Optional.of(variableReferenceNode);
                if (!tokenManager.matchAndRemove(Token.TokenTypes.ASSIGN).isPresent()) {
                    throw new SyntaxErrorException("No assignment found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                }
            } else {
                loopNode.assignment = Optional.empty();
            }
            loopNode.expression = ParseBoolExpTerm();
            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }

            //TODO just changed this
            if (tokenManager.peek(0).get().getType() == Token.TokenTypes.INDENT || tokenManager.peek(0).get().getType() == Token.TokenTypes.IF || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP) {
                loopNode.statements.addAll(ParseStatements());
            }

//            if (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
//                    || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
//                    || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
//                    || tokenManager.peek(0).get().getType() == Token.TokenTypes.INDENT) {
//                loopNode.statements.addAll(ParseStatements());
//            }

        }

        return loopNode;
    }

    public AssignmentNode ParseAssignment() throws SyntaxErrorException {
        AssignmentNode assignmentNode = new AssignmentNode();
        assignmentNode.target = ParseVariableReferenceNode();

        if (!tokenManager.matchAndRemove(Token.TokenTypes.ASSIGN).isPresent()) {
            throw new SyntaxErrorException("No assignment found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }

//        assignmentNode.expression = ParseVariableReferenceNode();
        assignmentNode.expression = ParseExpress();

        return assignmentNode;
    }

    public ExpressionNode ParseBoolExpTerm() throws SyntaxErrorException {
        BooleanOpNode booleanOpNode = new BooleanOpNode();
        booleanOpNode.left = ParseBoolExpFactor();

//        while (tokenManager.peek(0).get().getType() == Token.TokenTypes.AND ||
//        tokenManager.peek(0).get().getType() == Token.TokenTypes.OR) {
//            if (tokenManager.matchAndRemove(Token.TokenTypes.AND).isPresent()) {
//                booleanOpNode.op = BooleanOpNode.BooleanOperations.and;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.OR).isPresent()) {
//                booleanOpNode.op = BooleanOpNode.BooleanOperations.or;
//            }
//            booleanOpNode.right = ParseBoolExpTerm();
//            return booleanOpNode;
//        }

        if (tokenManager.matchAndRemove(Token.TokenTypes.AND).isPresent()) {
            BooleanOpNode newBooleanOpNode = new BooleanOpNode();
            newBooleanOpNode.left = booleanOpNode.left;
            newBooleanOpNode.op = BooleanOpNode.BooleanOperations.and;
            newBooleanOpNode.right = ParseBoolExpFactor();
            booleanOpNode.left = newBooleanOpNode;
        }

        if (tokenManager.matchAndRemove(Token.TokenTypes.OR).isPresent()) {
            booleanOpNode.op = BooleanOpNode.BooleanOperations.or;
            booleanOpNode.right = ParseBoolExpTerm();
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.AND).isPresent()) {
            booleanOpNode.op = BooleanOpNode.BooleanOperations.and;
            booleanOpNode.right = ParseBoolExpTerm();
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.NOT).isPresent()) {
            NotOpNode notOpNode = new NotOpNode();
            notOpNode.left = ParseBoolExpTerm();
            return notOpNode;
        } else {
            return booleanOpNode.left;
        }

        return booleanOpNode;
    }


    public ExpressionNode ParseBoolExpFactor() throws SyntaxErrorException {

        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT)
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)) {
            ExpressionNode expressionNode = new MethodCallExpressionNode();
            expressionNode = ParseMethodCallExpression().get();
            return expressionNode;
        }

        CompareNode compareNode = new CompareNode();
        compareNode.left = ParseExpression();
        if (tokenManager.matchAndRemove(Token.TokenTypes.LESSTHAN).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.lt;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.LESSTHANEQUAL).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.le;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.GREATERTHAN).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.gt;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.GREATERTHANEQUAL).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.ge;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.EQUAL).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.eq;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.NOTEQUAL).isPresent()) {
            compareNode.op = CompareNode.CompareOperations.ne;
        } else {
            return compareNode.left;
        }
//        if (tokenManager.peek(0).get().getType() != Token.TokenTypes.WORD
//                || tokenManager.peek(0).get().getType() != Token.TokenTypes.NUMBER) {
//
//        }
        //TODO just changed this
        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.NUMBER) {
            compareNode.right = ParseNumericLiteral();
        } else {
            compareNode.right = ParseExpression();
        }
        //TODO
//        compareNode.right = ParseExpression();
        return compareNode;
//        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.NUMBER
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.EQUAL)
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.NOTEQUAL)
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LESSTHANEQUAL)
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.GREATERTHANEQUAL)
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LESSTHAN)
//                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.GREATERTHAN)) {
//
//            CompareNode compareNode = new CompareNode();
//            compareNode.left = ParseExpression();
//            if (tokenManager.matchAndRemove(Token.TokenTypes.LESSTHAN).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.lt;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.LESSTHANEQUAL).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.le;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.GREATERTHAN).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.gt;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.GREATERTHANEQUAL).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.ge;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.EQUAL).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.eq;
//            } else if (tokenManager.matchAndRemove(Token.TokenTypes.NOTEQUAL).isPresent()) {
//                compareNode.op = CompareNode.CompareOperations.ne;
//            } else {
//                return compareNode.left;
//            }
//            compareNode.right = ParseExpression();
//            return compareNode;
//        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD) {
//            ParseVariableReferenceNode();
//        } else {
//            throw new SyntaxErrorException("No expression found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//
    }

    public ExpressionNode ParseExpress() throws SyntaxErrorException {
        MathOpNode mathOpNode = new MathOpNode();
        ExpressionNode expressionNode = ParseTerm();

        if (tokenManager.matchAndRemove(Token.TokenTypes.PLUS).isPresent()) {
            mathOpNode.op = MathOpNode.MathOperations.add;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.MINUS).isPresent()) {
            mathOpNode.op = MathOpNode.MathOperations.subtract;
        } else {
            return expressionNode;
        }

        mathOpNode.left = expressionNode;
        mathOpNode.right = ParseTerm();

        while (tokenManager.peek(0).get().getType() == Token.TokenTypes.PLUS
                || tokenManager.peek(1).get().getType() == Token.TokenTypes.MINUS) {

            MathOpNode newMathOpNode = new MathOpNode();
            if (tokenManager.matchAndRemove(Token.TokenTypes.PLUS).isPresent()) {
                newMathOpNode.op = MathOpNode.MathOperations.add;
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.MINUS).isPresent()) {
                newMathOpNode.op = MathOpNode.MathOperations.subtract;
            }
            newMathOpNode.left = mathOpNode;
            newMathOpNode.right = ParseTerm();
            return newMathOpNode;
        }

        return mathOpNode;
    }

    public ExpressionNode ParseTerm() throws SyntaxErrorException {
        MathOpNode mathOpNode = new MathOpNode();
        ExpressionNode expressionNode = ParseFactor();

        if (tokenManager.matchAndRemove(Token.TokenTypes.DIVIDE).isPresent()) {
            mathOpNode.op = MathOpNode.MathOperations.divide;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.MODULO).isPresent()) {
            mathOpNode.op = MathOpNode.MathOperations.modulo;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.TIMES).isPresent()) {
            mathOpNode.op = MathOpNode.MathOperations.multiply;
        } else {
            return expressionNode;
        }

        mathOpNode.left = expressionNode;
        mathOpNode.right = ParseFactor();

        while (tokenManager.peek(0).get().getType() == Token.TokenTypes.DIVIDE
                || tokenManager.peek(1).get().getType() == Token.TokenTypes.MODULO
                || tokenManager.peek(2).get().getType() == Token.TokenTypes.TIMES) {

            MathOpNode newMathOpNode = new MathOpNode();
            if (tokenManager.matchAndRemove(Token.TokenTypes.DIVIDE).isPresent()) {
                newMathOpNode.op = MathOpNode.MathOperations.divide;
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.MODULO).isPresent()) {
                newMathOpNode.op = MathOpNode.MathOperations.multiply;
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.TIMES).isPresent()) {
                newMathOpNode.op = MathOpNode.MathOperations.multiply;
            }
            newMathOpNode.left = mathOpNode;
            newMathOpNode.right = ParseFactor();

            return newMathOpNode;

        }


        return mathOpNode;
    }


    public ExpressionNode ParseFactor() throws SyntaxErrorException {

        var opt = ParseMethodCallExpression();
        if (opt.isPresent()) {
            return opt.get();
        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.NUMBER) {
            return ParseNumericLiteral();
        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD) {
            return ParseVariableReferenceNode();
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.TRUE).isPresent()) {
            BooleanLiteralNode booleanLiteralNode = new BooleanLiteralNode(true);
            return booleanLiteralNode;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.FALSE).isPresent()) {
            BooleanLiteralNode booleanLiteralNode = new BooleanLiteralNode(false);
            return booleanLiteralNode;
        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.QUOTEDSTRING) {
            return ParseStringLiteral();
        } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.QUOTEDCHARACTER) {
            return ParseCharLiteral();
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
            ExpressionNode expressionNode = ParseExpress();
            if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
                throw new SyntaxErrorException("No expression found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            return expressionNode;
        } else if (tokenManager.matchAndRemove(Token.TokenTypes.NEW).isPresent()) {
            NewNode newNode = new NewNode();
            Optional <Token> optClass = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
            if (optClass.isPresent()) {
                newNode.className = optClass.get().getValue();
            }
            if (!tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
                throw new SyntaxErrorException("No expression found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            if (tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
                return newNode;
            } else {
//                newNode.parameters.add(ParseExpression());
                newNode.parameters.add(ParseExpress());
                while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
                    newNode.parameters.add(ParseExpress());
                }
                if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
                    throw new SyntaxErrorException("No Right Paren found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                }
            }

            return newNode;
        }

        return null;
    }

    public NumericLiteralNode ParseNumericLiteral() throws SyntaxErrorException {
        NumericLiteralNode numericLiteralNode = new NumericLiteralNode();
        Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.NUMBER);
        if (opt.isPresent()) {
            numericLiteralNode.value = Float.parseFloat(opt.get().getValue());
        }
        return numericLiteralNode;
    }

    public StringLiteralNode ParseStringLiteral() throws SyntaxErrorException {
        StringLiteralNode stringLiteralNode = new StringLiteralNode();
        Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.QUOTEDSTRING);
        if (opt.isPresent()) {
            stringLiteralNode.value = opt.get().getValue();
        }
        return stringLiteralNode;
    }

    public CharLiteralNode ParseCharLiteral() throws SyntaxErrorException {
        CharLiteralNode charLiteralNode = new CharLiteralNode();
        Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.QUOTEDCHARACTER);
        if (opt.isPresent()) {
            charLiteralNode.value = opt.get().getValue().charAt(0);
        }
        return charLiteralNode;
    }

    public VariableReferenceNode ParseIdentifier() throws SyntaxErrorException {
        VariableReferenceNode variableReferenceNode = ParseVariableReferenceNode();
        return variableReferenceNode;
    }

    public ExpressionNode ParseExpression() throws SyntaxErrorException {
//        ExpressionNode expressionNode = new BooleanOpNode();
//        return expressionNode;
        return ParseVariableReferenceNode();
    }
    public Optional<MethodCallExpressionNode> ParseMethodCallExpression() throws SyntaxErrorException {
        MethodCallExpressionNode methodCallExpressionNode = new MethodCallExpressionNode();
        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD,Token.TokenTypes.DOT)
                || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD,Token.TokenTypes.LPAREN)) {
//            MethodCallExpressionNode methodCallExpressionNode = new MethodCallExpressionNode();
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT)) {
                Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
                methodCallExpressionNode.objectName = Optional.of(opt.get().getValue());
                tokenManager.matchAndRemove(Token.TokenTypes.DOT);
            } else {
                methodCallExpressionNode.objectName = Optional.empty(); //newcode
            }
            Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
            if (opt.isPresent()) {
                methodCallExpressionNode.methodName = opt.get().getValue();
            } else {
                throw new SyntaxErrorException("No method name found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            if (!tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
                throw new SyntaxErrorException("No left Paren found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }

            //TODO parameters
            if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
                methodCallExpressionNode.parameters.add(ParseExpression());
                while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
                    methodCallExpressionNode.parameters.add(ParseExpress());
                }
                tokenManager.matchAndRemove(Token.TokenTypes.RPAREN);
            }
            return Optional.of(methodCallExpressionNode);
        }
        return Optional.empty();
    }

    public VariableReferenceNode ParseVariableReferenceNode() throws SyntaxErrorException {
        VariableReferenceNode variableReferenceNode = new VariableReferenceNode();
//        Optional <Token> opt = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD
                || tokenManager.peek(0).get().getType() == Token.TokenTypes.NUMBER) {
            if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD) {
                variableReferenceNode.name = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
            } else if (tokenManager.peek(0).get().getType() == Token.TokenTypes.NUMBER) {
                variableReferenceNode.name = tokenManager.matchAndRemove(Token.TokenTypes.NUMBER).get().getValue();
            }
//            variableReferenceNode.name = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().toString();
        } else {
            throw new SyntaxErrorException("No variable found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        return variableReferenceNode;
    }

    public MethodDeclarationNode ParseMethodDeclaration() throws SyntaxErrorException {
        MethodDeclarationNode methodDeclarationNode = new MethodDeclarationNode();

            if (tokenManager.matchAndRemove(Token.TokenTypes.PRIVATE).isPresent()) {
                methodDeclarationNode.isPrivate = true;
                methodDeclarationNode.isShared = false;
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.SHARED).isPresent()) {
                methodDeclarationNode.isShared = true;
                methodDeclarationNode.isPrivate = false;
            } else {
                methodDeclarationNode.isPrivate = false;
                methodDeclarationNode.isShared = false;
            }

            Optional <Token> opt =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
            if (opt.isPresent()) {
                Token token = opt.get();
                methodDeclarationNode.name = token.getValue();
                methodDeclarationNode.parameters = new ArrayList<>();
            } else {
                throw new SyntaxErrorException("No word found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            if (!tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
                throw new SyntaxErrorException("No left curly found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
                methodDeclarationNode.parameters.addAll(ParseVariableDeclarations());
            }
            if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
                throw new SyntaxErrorException("No RPAREN found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
            if (tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
                if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
                    methodDeclarationNode.returns.addAll(ParseVariableDeclarations());
                }
            }

            if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }

            //MethodBody starts here
            if (!tokenManager.matchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
                throw new SyntaxErrorException("No indent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
//            while (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) { //if
////                methodDeclarationNode.locals.addAll(ParseVariableDeclarations());
//                methodDeclarationNode.locals.add(ParseVariableDeclaration());
//                if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
//                    throw new SyntaxErrorException("No new line found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//                }
//            }

            while (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) { //if
    //          methodDeclarationNode.locals.addAll(ParseVariableDeclarations());
                methodDeclarationNode.locals.add(ParseVariableDeclaration());
                if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                    throw new SyntaxErrorException("No new line found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                }
            }
            methodDeclarationNode.statements = new ArrayList<>();
            if (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                    || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                    || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
                    || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.COMMA)
                    || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT)) {

                while (tokenManager.peek(0).get().getType() == Token.TokenTypes.IF
                        || tokenManager.peek(0).get().getType() == Token.TokenTypes.LOOP
                        || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)
                        || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.COMMA)
                        || tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT)) {
//                methodDeclarationNode.statements.addAll(ParseStatements());

//                while (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
//                    methodDeclarationNode.statements.add(ParseStatement());
//                    if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
//                        throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
//                    }
//                }
                    methodDeclarationNode.statements.add(ParseStatement());
                    if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()
                            && tokenManager.peek(0).get().getType() != Token.TokenTypes.DEDENT) {
                        throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
                    }
                }
            }
            if (!tokenManager.matchAndRemove(Token.TokenTypes.DEDENT).isPresent()) {
                throw new SyntaxErrorException("No dedent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        //}
        return methodDeclarationNode;
    }


    public InterfaceNode ParseInterface() throws SyntaxErrorException {
        InterfaceNode interfaceNode = new InterfaceNode();
        Optional <Token> opt =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (opt.isPresent()) {
            Token tok = opt.get();
            interfaceNode.name = tok.getValue();
            interfaceNode.methods = new LinkedList<>();
        } else {
            throw new SyntaxErrorException("No word has found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            throw new SyntaxErrorException("No new line found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
            throw new SyntaxErrorException("No indent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (tokenManager.peek(0).get().getType() == Token.TokenTypes.WORD) {
            MethodHeaderNode methodNode = ParseMethod();
            interfaceNode.methods.add(methodNode);
            Optional <Token> peekToken = tokenManager.peek(0);
            Token PeekForWord = peekToken.get();
            while (PeekForWord.getType() == Token.TokenTypes.WORD) {
                interfaceNode.methods.add(ParseMethod());
                peekToken = tokenManager.peek(0);
                PeekForWord = peekToken.get();
            }
        } else {
            throw new SyntaxErrorException("No method found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        return interfaceNode;
    }

    public MethodHeaderNode ParseMethod() throws SyntaxErrorException {
        MethodHeaderNode methodNode = new MethodHeaderNode();
        Optional <Token> opt =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (opt.isPresent()) {
            Token token = opt.get();
            methodNode.name = token.getValue();
            methodNode.parameters = new ArrayList<>();
        } else {
            throw new SyntaxErrorException("No word found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()) {
            throw new SyntaxErrorException("No left curly found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
//            methodNode.parameters.add(ParseVariableDeclaration());
//            while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
//                methodNode.parameters.add(ParseVariableDeclaration());
//            }
            methodNode.parameters.addAll(ParseVariableDeclarations());
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()) {
            throw new SyntaxErrorException("No RPAREN found. ", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        if (tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
//                methodNode.parameters.add(ParseVariableDeclaration());
//                while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
//                    methodNode.parameters.add(ParseVariableDeclaration());
//                }
                methodNode.returns.addAll(ParseVariableDeclarations());
            }
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            Optional <Token> peekToken = tokenManager.peek(0);
            Token PeekForDedent = peekToken.get();
            if (PeekForDedent.getType() == Token.TokenTypes.DEDENT) {
                return methodNode;
            } else {
                throw new SyntaxErrorException("No dedent found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
            }
        }
        return methodNode;
    }

    public VariableDeclarationNode ParseVariableDeclaration() throws SyntaxErrorException {
        if (!tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            throw new SyntaxErrorException("No word found.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        VariableDeclarationNode variableDeclarationNode = new VariableDeclarationNode();
        Optional <Token> optWord =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (optWord.isPresent()) {
            Token token = optWord.get();
            variableDeclarationNode.type = token.getValue();
        }
        Optional <Token> optWord2 =  tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if (optWord2.isPresent()) {
            Token token = optWord2.get();
            variableDeclarationNode.name = token.getValue();
        }
        return variableDeclarationNode;
    }

    //For 1 or more Parameters or return types.
    public ArrayList<VariableDeclarationNode> ParseVariableDeclarations() throws SyntaxErrorException {
        ArrayList<VariableDeclarationNode> variableDeclarationNodes = new ArrayList<>();
            if (tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
                variableDeclarationNodes.add(ParseVariableDeclaration());
                while (tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
                    variableDeclarationNodes.add(ParseVariableDeclaration());
                }
            }
        return variableDeclarationNodes;
    }

    //TODO check for 1 or multiple newlines
    public void RequireNewLine() throws SyntaxErrorException {
        if (tokenManager.done()) {
            return;
        }
        if (!tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            throw new SyntaxErrorException("No new line found", tokenManager.getCurrentLine(), tokenManager.getCurrentColumn());
        }
        while (tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
        }

    }

}