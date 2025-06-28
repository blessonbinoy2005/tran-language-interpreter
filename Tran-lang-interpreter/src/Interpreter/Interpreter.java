package Interpreter;

import AST.*;

import java.lang.reflect.Member;
import java.util.*;

public class Interpreter {
    private TranNode top;

    /** Constructor - get the interpreter ready to run. Set members from parameters and "prepare" the class.
     *
     * Store the tran node.
     * Add any built-in methods to the AST
     * @param top - the head of the AST
     */
    public Interpreter(TranNode top) {
        this.top = top;
        ClassNode classNode = new ClassNode();
        classNode.name = "console";
        ConsoleWrite print = new ConsoleWrite();
        print.name = "write";
        print.isPrivate = false;
        print.isShared = true;
        classNode.methods.add(print);
        top.Classes.add(classNode);
        print.isVariadic = true;

        //loop stuff
//        InterfaceNode Iterator = new InterfaceNode();

        ClassNode newClassNode = new ClassNode();
        newClassNode.name = "iterator";
        newClassNode.interfaces.add("iterator");
        VariableDeclarationNode max = new VariableDeclarationNode();
        VariableDeclarationNode current = new VariableDeclarationNode();
        max.name = "max";
        max.type = "number";
        MemberNode memberMax = new MemberNode();
        memberMax.declaration = max;

        current.name = "current";
        current.type = "number";
        MemberNode memberCurrent = new MemberNode();

        newClassNode.members.add(memberMax);
        newClassNode.members.add(memberCurrent);

        VariableDeclarationNode variableMax = new VariableDeclarationNode();
        ConstructorNode constructor = new ConstructorNode();
        constructor.parameters.add(variableMax);
        VariableReferenceNode reference = new VariableReferenceNode();
        reference.name = variableMax.name;
    }

    /**
     * This is the public interface to the interpreter. After parsing, we will create an interpreter and call start to
     * start interpreting the code.
     *
     * Search the classes in Tran for a method that is "isShared", named "start", that is not private and has no parameters
     * Call "InterpretMethodCall" on that method, then return.
     * Throw an exception if no such method exists.
     */
    public void start() {
        LinkedList<InterpreterDataType> locals = new LinkedList<InterpreterDataType>();
        for (int i = 0; i < top.Classes.size(); i++) {
            for (int j = 0; j < top.Classes.get(i).methods.size(); j++) {
                if (top.Classes.get(i).methods.get(j).name.equals("start") && top.Classes.get(i).methods.get(j).isShared) {
                    interpretMethodCall(Optional.empty(),top.Classes.get(i).methods.get(j),locals);
                }
            }
        }
    }

    //              Running Methods

    /**
     * Find the method (local to this class, shared (like Java's system.out.print), or a method on another class)
     * Evaluate the parameters to have a list of values
     * Use interpretMethodCall() to actually run the method.
     *
     * Call GetParameters() to get the parameter value list
     * Find the method. This is tricky - there are several cases:
     * someLocalMethod() - has NO object name. Look in "object"
     * console.write() - the objectName is a CLASS and the method is shared
     * bestStudent.getGPA() - the objectName is a local or a member
     *
     * Once you find the method, call InterpretMethodCall() on it. Return the list that it returns.
     * Throw an exception if we can't find a match.
     * @param object - the object we are inside right now (might be empty)
     * @param locals - the current local variables
     * @param mc - the method call
     * @return - the return values
     */
    private List<InterpreterDataType> findMethodForMethodCallAndRunIt(Optional<ObjectIDT> object, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc) {
        List<InterpreterDataType> result = null;
        result = getParameters(object, locals, mc);
        if (mc.objectName.isPresent()) {
            //for console.print()
            for (int i = 0; i < top.Classes.size(); i++) {
                if (top.Classes.get(i).name.equals(mc.objectName.get())) {
                    for (int j = 0; j < top.Classes.get(i).methods.size(); j++) {
                        if (top.Classes.get(i).methods.get(j).name.equals(mc.methodName)) {
                            return interpretMethodCall(object,top.Classes.get(i).methods.get(j),result);
                        }
                    }
                }
            }
            //for bestStudent.getGPA()

            //TODO when im calling interpretMethodCall for the t.add method, im not passing in the current locals,
            //TODO so in the method it has z = x + y, since im not passing in any locals, it doesn't know what x and y is.
            if (locals.containsKey(mc.objectName.get())) {
                InterpreterDataType type = locals.get(mc.objectName.get());
                ObjectIDT obj = ((ReferenceIDT) type).refersTo.get();
                for (int i = 0; i < top.Classes.size(); i++) {
                    for (int j = 0; j < top.Classes.get(i).methods.size(); j++) {
                        if (top.Classes.get(i).methods.get(j).name.equals(mc.methodName)) {
                            return interpretMethodCall(Optional.of(obj),top.Classes.get(i).methods.get(j),result);
                        }

                    }

                }
//                for (int i = 0; i < ((ObjectIDT) type).astNode.methods.size(); i++) {
//                    if (doesMatch(((ObjectIDT) type).astNode.methods.get(i), mc, result)) {
//                        return interpretMethodCall(object,((ObjectIDT) type).astNode.methods.get(i),result);
//                    }
//                }
            }
        }
        //for someLocalMethod() in start class
        for (int i = 0; i < object.get().astNode.methods.size(); i++) {
            if (doesMatch(object.get().astNode.methods.get(i), mc, result)) {
                return interpretMethodCall(object,object.get().astNode.methods.get(i),result);
            }
        }
//        if (mc.objectName.isPresent()) {
//            for (int i = 0; i < top.Classes.size(); i++) {
//                if (top.Classes.get(i).name.equals(mc.objectName.get())) {
//                    for (int j = 0; j < top.Classes.get(i).methods.size(); j++) {
//                        if (top.Classes.get(i).methods.get(j).name.equals(mc.methodName)) {
//                            return interpretMethodCall(object,top.Classes.get(i).methods.get(j),result);
//                        }
//                    }
//                }
//            }
//            if (locals.containsKey(mc.objectName.get())) {
//                InterpreterDataType type = locals.get(mc.objectName.get());
//                for (int i = 0; i < ((ObjectIDT) type).astNode.methods.size(); i++) {
//                    if (doesMatch(((ObjectIDT) type).astNode.methods.get(i), mc, result)) {
//                        return interpretMethodCall(object,((ObjectIDT) type).astNode.methods.get(i),result);
//                    }
//                }
//            }
//        }
        return result;
    }

    /**
     * Run a "prepared" method (found, parameters evaluated)
     * This is split from findMethodForMethodCallAndRunIt() because there are a few cases where we don't need to do the finding:
     * in start() and dealing with loops with iterator objects, for example.
     *
     * Check to see if "m" is a built-in. If so, call Execute() on it and return
     * Make local variables, per "m"
     * If the number of passed in values doesn't match m's "expectations", throw
     * Add the parameters by name to locals.
     * Call InterpretStatementBlock
     * Build the return list - find the names from "m", then get the values for those names and add them to the list.
     * @param object - The object this method is being called on (might be empty for shared)
     * @param m - Which method is being called
     * @param values - The values to be passed in
     * @return the returned values from the method
     */
    private List<InterpreterDataType> interpretMethodCall(Optional<ObjectIDT> object, MethodDeclarationNode m, List<InterpreterDataType> values) {
        var retVal = new LinkedList<InterpreterDataType>();
        if (m instanceof BuiltInMethodDeclarationNode) {
            ((BuiltInMethodDeclarationNode) m).Execute(values);
        } else {

            HashMap<String, InterpreterDataType> locals = new HashMap<>();
            for (int i = 0; i < m.locals.size(); i++) {
                locals.put(m.locals.get(i).name,instantiate(m.locals.get(i).type));
            }
            for (int i = 0; i < m.returns.size(); i++) {
                locals.put(m.returns.get(i).name,instantiate(m.returns.get(i).type));
            }
            if (m.parameters.size() == values.size()) {
                interpretStatementBlock(object,m.statements,locals);
                for (int i = 0; i < m.returns.size(); i++) {
                    if (locals.containsKey(m.returns.get(i).name)) {
                        retVal.add(locals.get(m.returns.get(i).name));
                    }
                }
            } else {
                throw new RuntimeException("Incorrect amount of parameters found.");
            }
        }
        return retVal;
    }

    //              Running Constructors

    /**
     * This is a special case of the code for methods. Just different enough to make it worthwhile to split it out.
     *
     * Call GetParameters() to populate a list of IDT's
     * Call GetClassByName() to find the class for the constructor
     * If we didn't find the class, throw an exception
     * Find a constructor that is a good match - use DoesConstructorMatch()
     * Call InterpretConstructorCall() on the good match
     * @param callerObj - the object that we are inside when we called the constructor
     * @param locals - the current local variables (used to fill parameters)
     * @param mc  - the method call for this construction
     * @param newOne - the object that we just created that we are calling the constructor for
     */
    private void findConstructorAndRunIt(Optional<ObjectIDT> callerObj, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc, ObjectIDT newOne) {
        LinkedList<InterpreterDataType> parameters = new LinkedList<InterpreterDataType>();
        parameters.addAll(getParameters(Optional.of(newOne), locals, mc));
        Optional <ClassNode> opt = getClassByName(mc.methodName);
        if (opt.isPresent()) {
            for (int i = 0; i < opt.get().constructors.size(); i++) {
                if (doesConstructorMatch(opt.get().constructors.get(i),mc,parameters)) {
                    interpretConstructorCall(newOne, opt.get().constructors.get(i),parameters);
                }
            }
//            throw new RuntimeException("No matching constructor found");
        } else {
            throw new RuntimeException("Unable to find the class");

        }
    }

    /**
     * Similar to interpretMethodCall, but "just different enough" - for example, constructors don't return anything.
     *
     * Creates local variables (as defined by the ConstructorNode), calls Instantiate() to do the creation
     * Checks to ensure that the right number of parameters were passed in, if not throw.
     * Adds the parameters (with the names from the ConstructorNode) to the locals.
     * Calls InterpretStatementBlock
     * @param object - the object that we allocated
     * @param c - which constructor is being called
     * @param values - the parameter values being passed to the constructor
     */
    private void interpretConstructorCall(ObjectIDT object, ConstructorNode c, List<InterpreterDataType> values) {
//        LinkedList<InterpreterDataType> constructLocals = new LinkedList<InterpreterDataType>();
//        for (int i = 0; i < c.locals.size(); i++) {
//            constructLocals.add(instantiate(c.locals.get(i).type));
//        }
        if (c.parameters.size() == values.size()) {
            HashMap<String, InterpreterDataType> locals = new HashMap<String, InterpreterDataType>();
            for (int i = 0; i < c.parameters.size(); i++) {
                locals.put(c.parameters.get(i).name, instantiate(c.parameters.get(i).type));
            }
            for (int i = 0; i < values.size(); i++) {
                String paramName = c.parameters.get(i).name;
                InterpreterDataType passedValue = values.get(i);
                if (locals.containsKey(paramName)) {
                    locals.put(paramName, passedValue);
                }
            }

            interpretStatementBlock(Optional.of(object),c.statements,locals);

        } else {
            throw new RuntimeException("Constructor parameters don't match");
        }
    }

    //              Running Instructions

    /**
     * Given a block (which could be from a method or an "if" or "loop" block, run each statement.
     * Blocks, by definition, do ever statement, so iterating over the statements makes sense.
     *
     * For each statement in statements:
     * check the type:
     *      For AssignmentNode, FindVariable() to get the target. Evaluate() the expression. Call Assign() on the target with the result of Evaluate()
     *      For MethodCallStatementNode, call doMethodCall(). Loop over the returned values and copy them into our local variables
     *      For LoopNode - there are 2 kinds.
     *          Setup:
     *          If this is a Loop over an iterator (an Object node whose class has "iterator" as an interface)
     *              Find the "getNext()" method; throw an exception if there isn't one
     *          Loop:
     *          While we are not done:
     *              if this is a boolean loop, Evaluate() to get true or false.
     *              if this is an iterator, call "getNext()" - it has 2 return values. The first is a boolean (was there another?), the second is a value
     *              If the loop has an assignment variable, populate it: for boolean loops, the true/false. For iterators, the "second value"
     *              If our answer from above is "true", InterpretStatementBlock() on the body of the loop.
     *       For If - Evaluate() the condition. If true, InterpretStatementBlock() on the if's statements. If not AND there is an else, InterpretStatementBlock on the else body.
     * @param object - the object that this statement block belongs to (used to get member variables and any members without an object)
     * @param statements - the statements to run
     * @param locals - the local variables
     */
    private void interpretStatementBlock(Optional<ObjectIDT> object, List<StatementNode> statements, HashMap<String, InterpreterDataType> locals) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof AssignmentNode) {
                InterpreterDataType target = findVariable((((AssignmentNode) statements.get(i)).target.name), locals, object);
//                target = evaluate(locals, object, (((AssignmentNode) statements.get(i)).expression));
                target.Assign(evaluate(locals, object, (((AssignmentNode) statements.get(i)).expression)));
            } else if (statements.get(i) instanceof MethodCallStatementNode) {


                LinkedList <InterpreterDataType> EvalutedParameterList = new LinkedList<InterpreterDataType>();
                for (int j = 0; j < ((MethodCallStatementNode)statements.get(i)).parameters.size(); j++) {
                    EvalutedParameterList.add(evaluate(locals, object, ((MethodCallStatementNode)statements.get(i)).parameters.get(j)));
                }

                LinkedList <InterpreterDataType> evalutedReturnList = new LinkedList<InterpreterDataType>();
                evalutedReturnList.addAll(findMethodForMethodCallAndRunIt(object,locals,((MethodCallStatementNode)statements.get(i))));
                for (int k = 0; k < evalutedReturnList.size(); k++) {
                    if (locals.containsKey(((MethodCallStatementNode) statements.get(i)).returnValues.get(k).name)) {
                         InterpreterDataType returns = locals.get(k);
                         returns.Assign(evalutedReturnList.get(k));
                    }
                }

            } else if (statements.get(i) instanceof LoopNode) {
                if (((LoopNode) statements.get(i)).assignment.isPresent()) {
                    InterpreterDataType exp = evaluate(locals, object, ((LoopNode) statements.get(i)).expression);
                    if (exp instanceof BooleanIDT) {
                        while (((BooleanIDT) exp).Value) {
                            interpretStatementBlock(object, ((LoopNode) statements.get(i)).statements, locals);
                            exp = evaluate(locals, object, ((LoopNode) statements.get(i)).expression);
                        }
                    } else if (exp instanceof ObjectIDT) {
                        for (int j = 0; j < ((ObjectIDT) exp).astNode.interfaces.size(); j++) {
                            if (((ObjectIDT) exp).astNode.interfaces.get(j).equals("iterator")) {
                                for (int k = 0; k < ((ObjectIDT) exp).astNode.methods.size(); k++) {
                                    if (((ObjectIDT) exp).astNode.methods.get(k).name.equals("getNext")) {
//                                        findMethodForMethodCallAndRunIt(object, locals, ((ObjectIDT) exp).astNode.methods.get(k));
                                    }
                                }

//                                findMethodForMethodCallAndRunIt(object, locals, ((ObjectIDT) exp).astNode.methods);
                            }
                        }
                    }

                } else {
                    InterpreterDataType exp = evaluate(locals, object, ((LoopNode) statements.get(i)).expression);
                    if (exp instanceof BooleanIDT) {
                        while (((BooleanIDT) exp).Value) {
                            interpretStatementBlock(object, ((LoopNode) statements.get(i)).statements, locals);
                            exp = evaluate(locals, object, ((LoopNode) statements.get(i)).expression);
                        }
                    }
                }
            } else if (statements.get(i) instanceof IfNode) {
                InterpreterDataType exp = evaluate(locals, object, ((IfNode) statements.get(i)).condition);
                if (((BooleanIDT) exp).Value) {
                    interpretStatementBlock(object, ((IfNode) statements.get(i)).statements, locals);
                } else {
                    if ((((IfNode) statements.get(i)).elseStatement.isPresent())) {
                        interpretStatementBlock(object, ((IfNode) statements.get(i)).elseStatement.get().statements, locals);
                    }
                }
            }
        }
    }

    /**
     *  evaluate() processes everything that is an expression - math, variables, boolean expressions.
     *  There is a good bit of recursion in here, since math and comparisons have left and right sides that need to be evaluated.
     *
     * See the How To Write an Interpreter document for examples
     * For each possible ExpressionNode, do the work to resolve it:
     * BooleanLiteralNode - create a new BooleanLiteralNode with the same value  //booleanidt
     *      - Same for all of the basic data types
     * BooleanOpNode - Evaluate() left and right, then perform either and/or on the results.
     * CompareNode - Evaluate() both sides. Do good comparison for each data type
     * MathOpNode - Evaluate() both sides. If they are both numbers, do the math using the built-in operators. Also handle String + String as concatenation (like Java) //numberidt
     * MethodCallExpression - call doMethodCall() and return the first value
     * VariableReferenceNode - call findVariable()
     * @param locals the local variables
     * @param object - the current object we are running
     * @param expression - some expression to evaluate
     * @return a value
     */
    private InterpreterDataType evaluate(HashMap<String, InterpreterDataType> locals, Optional<ObjectIDT> object, ExpressionNode expression) {
//        BooleanLiteralNode b = (BooleanLiteralNode) expression;
        if (expression instanceof BooleanLiteralNode) {
            BooleanIDT booleanIDT = new BooleanIDT(((BooleanLiteralNode) expression).value);
            return booleanIDT;
        } else if (expression instanceof BooleanOpNode) {
            InterpreterDataType left = evaluate(locals, object, ((BooleanOpNode) expression).left);
            InterpreterDataType right = evaluate(locals, object, ((BooleanOpNode) expression).right);
            if (((BooleanOpNode) expression).op.equals(BooleanOpNode.BooleanOperations.and)) {
                if (((BooleanIDT) (left)).Value && ((BooleanIDT) (right)).Value) {
                    return new BooleanIDT(true);
                }
            } else if (((BooleanOpNode) expression).op.equals(BooleanOpNode.BooleanOperations.or)) {
                if (((BooleanIDT) (left)).Value || ((BooleanIDT) (right)).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            }
        } else if (expression instanceof CompareNode) {
            InterpreterDataType left = evaluate(locals, object, ((CompareNode) expression).left);
            InterpreterDataType right = evaluate(locals, object, ((CompareNode) expression).right);
            if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.lt)) {
                if (((NumberIDT) left).Value < ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            } else if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.le)) {
                if (((NumberIDT) left).Value <= ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            } else if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.gt)) {
                if (((NumberIDT) left).Value > ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            } else if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.ge)) {
                if (((NumberIDT) left).Value >= ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            } else if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.eq)) {
                if (((NumberIDT) left).Value == ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            } else if (((CompareNode) expression).op.equals(CompareNode.CompareOperations.ne)) {
                if (((NumberIDT) left).Value != ((NumberIDT) right).Value) {
                    return new BooleanIDT(true);
                } else {
                    return new BooleanIDT(false);
                }
            }
        } else if (expression instanceof MathOpNode) {
            InterpreterDataType left = evaluate(locals, object, ((MathOpNode) expression).left);
            InterpreterDataType right = evaluate(locals, object, ((MathOpNode) expression).right);
            if (((MathOpNode) expression).op.equals(MathOpNode.MathOperations.add)) {
                return new NumberIDT((((NumberIDT)left)).Value + (((NumberIDT)right)).Value);
            } else if (((MathOpNode) expression).op.equals(MathOpNode.MathOperations.subtract)) {
                return new NumberIDT((((NumberIDT) left)).Value - (((NumberIDT) right)).Value);
            } else if (((MathOpNode) expression).op.equals(MathOpNode.MathOperations.multiply)) {
                return new NumberIDT((((NumberIDT) left)).Value * (((NumberIDT) right)).Value);
            } else if (((MathOpNode) expression).op.equals(MathOpNode.MathOperations.divide)) {
                return new NumberIDT((((NumberIDT) left)).Value / (((NumberIDT) right)).Value);
            } else if (((MathOpNode) expression).op.equals(MathOpNode.MathOperations.modulo)) {
                return new NumberIDT((((NumberIDT) left)).Value % (((NumberIDT) right)).Value);
            }
            //TODO check about methodcallstatementNode
        } else if (expression instanceof MethodCallExpressionNode) {
            MethodCallStatementNode mc = new MethodCallStatementNode((MethodCallExpressionNode) expression);
            VariableReferenceNode varRef = new VariableReferenceNode();
            mc.returnValues.add(varRef);
            return findMethodForMethodCallAndRunIt(object, locals, mc).getFirst();
        } else if (expression instanceof VariableReferenceNode) {
            return findVariable(((VariableReferenceNode) expression).name,locals, object);
        } else if (expression instanceof NumericLiteralNode) {
            return new NumberIDT(((NumericLiteralNode) expression).value);
        } else if (expression instanceof StringLiteralNode) {
            return new StringIDT(((StringLiteralNode) expression).value);
        } else if (expression instanceof CharLiteralNode) {
            return new CharIDT(((CharLiteralNode) expression).value);
        } else if (expression instanceof NewNode) {
//            NewNode newNode = (NewNode) expression;
            String ClassName = ((NewNode) expression).className;
//            ObjectIDT obj = null;
            for (int i = 0 ; i < top.Classes.size(); i++) {
                if (top.Classes.get(i).name.equals(ClassName)) {
                    ObjectIDT obj = new ObjectIDT(top.Classes.get(i));
                    for (int j = 0; j < top.Classes.get(i).members.size(); j++) {
                        obj.members.put(top.Classes.get(i).members.get(j).declaration.name, instantiate(top.Classes.get(i).members.get(j).declaration.type));
                        //putting x and y into locals
//                        locals.put(top.Classes.get(i).members.get(j).declaration.name, instantiate(top.Classes.get(i).members.get(j).declaration.type));
                    }
//                    obj.astNode = top.Classes.get(i);
                    ReferenceIDT ref = new ReferenceIDT();
                    ref.refersTo = Optional.of(obj);
//            InterpreterDataType val = instantiate(newNode.className);

                    MethodCallStatementNode mc = new MethodCallStatementNode();
//                    mc.objectName = Optional.of(((NewNode) expression).className);
                    mc.methodName = ((NewNode) expression).className;
                    mc.parameters.addAll(((NewNode) expression).parameters);

                    findConstructorAndRunIt(object,locals,(mc), obj);

                    return ref;
                }
            }
//            ReferenceIDT ref = new ReferenceIDT();
//            ref.refersTo = Optional.of(obj);
////            InterpreterDataType val = instantiate(newNode.className);
//
//            MethodCallStatementNode mc = new MethodCallStatementNode();
//            mc.objectName = Optional.of(((NewNode) expression).className);
//            mc.parameters.addAll(((NewNode) expression).parameters);
//            findConstructorAndRunIt(object,locals,(mc), obj);

        }
        throw new IllegalArgumentException();
    }

    //              Utility Methods

    /**
     * Used when trying to find a match to a method call. Given a method declaration, does it match this method call?
     * We double check with the parameters, too, although in theory JUST checking the declaration to the call should be enough.
     *
     * Match names, parameter counts (both declared count vs method call and declared count vs value list), return counts.
     * If all of those match, consider the types (use TypeMatchToIDT).
     * If everything is OK, return true, else return false.
     * Note - if m is a built-in and isVariadic is true, skip all of the parameter validation.
     * @param m - the method declaration we are considering
     * @param mc - the method call we are trying to match
     * @param parameters - the parameter values for this method call
     * @return does this method match the method call?
     */
    private boolean doesMatch(MethodDeclarationNode m, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if (m.name.equals(mc.methodName)) {
            if (mc.parameters.size() == m.parameters.size() && mc.returnValues.size() == m.returns.size()) {
//                for (int i = 0; i < m.parameters.size(); i++) {
//                    if (!m.parameters.get(i).type.equals(m.parameters.get(i).type)) {
//                        return false;
//                    }
//                }
                for (int i = 0; i < parameters.size(); i++) {
                    if (!typeMatchToIDT(m.parameters.get(i).type, parameters.get(i))) {
                        return false;
                    }
                }
//                for (int i = 0; i < m.returns.size(); i++) {
//                    if (!m.returns.get(i).type.equals(m.returns.get(i).type)) {
//                        return false;
//                    }
//                }
                return true;
            }
        }
        return false;
    }

    /**
     * Very similar to DoesMatch() except simpler - there are no return values, the name will always match.
     * @param c - a particular constructor
     * @param mc - the method call
     * @param parameters - the parameter values
     * @return does this constructor match the method call?
     */
    private boolean doesConstructorMatch(ConstructorNode c, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if (c.parameters.size() == parameters.size() && mc.parameters.size() == parameters.size()) {
            for (int i = 0; i < parameters.size(); i++) {
                if (!typeMatchToIDT(c.parameters.get(i).type, parameters.get(i))) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Used when we call a method to get the list of values for the parameters.
     *
     * for each parameter in the method call, call Evaluate() on the parameter to get an IDT and add it to a list
     * @param object - the current object
     * @param locals - the local variables
     * @param mc - a method call
     * @return the list of method values
     */
    private List<InterpreterDataType> getParameters(Optional<ObjectIDT> object, HashMap<String,InterpreterDataType> locals, MethodCallStatementNode mc) {
        List<InterpreterDataType> result = new LinkedList<InterpreterDataType>();
        for (int i = 0; i < mc.parameters.size(); i++) {
            result.add(evaluate(locals, object, mc.parameters.get(i)));
        }
        return result;
    }

    /**
     * Used when we have an IDT and we want to see if it matches a type definition
     * Commonly, when someone is making a function call - do the parameter values match the method declaration?
     *
     * If the IDT is a simple type (boolean, number, etc) - does the string type match the name of that IDT ("boolean", etc)
     * If the IDT is an object, check to see if the name matches OR the class has an interface that matches
     * If the IDT is a reference, check the inner (refered to) type
     * @param type the name of a data type (parameter to a method)
     * @param idt the IDT someone is trying to pass to this method
     * @return is this OK?
     */
    private boolean typeMatchToIDT(String type, InterpreterDataType idt) {
        if (idt instanceof BooleanIDT && type.equals("boolean")) {
            return true;
        } else if (idt instanceof StringIDT && type.equals("string")) {
            return true;
        } else if (idt instanceof CharIDT && type.equals("character")) {
            return true;
        } else if (idt instanceof NumberIDT && type.equals("number")) {
            return true;
        } else if (idt instanceof ObjectIDT) {
            if (((ObjectIDT) idt).astNode.name.equals(type)) {
                return true;
            }
        } else if (idt instanceof ReferenceIDT) {
            if (((ReferenceIDT) idt).refersTo.isPresent()) {
                if (((ObjectIDT) idt).astNode.name.equals(type)) {
                    return true;
                }
            } else {
                throw new RuntimeException("Unable to resolve type " + type);
            }
        }
        return false;
    }

    /**
     * Find a method in an object that is the right match for a method call (same name, parameters match, etc. Uses doesMatch() to do most of the work)
     *
     * Given a method call, we want to loop over the methods for that class, looking for a method that matches (use DoesMatch) or throw
     * @param object - an object that we want to find a method on
     * @param mc - the method call
     * @param parameters - the parameter value list
     * @return a method or throws an exception
     */
    private MethodDeclarationNode getMethodFromObject(ObjectIDT object, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
//        for (int i = 0; i < object.astNode.methods.size(); i++) {
//            if (object.astNode.methods.get(i).name.equals(mc.methodName)) {
//                if (object.astNode.methods.get(i).parameters.size() == parameters.size()) {
//                    for (int j = 0; j < object.astNode.methods.get(i).parameters.size(); j++) {
//                        if (!object.astNode.methods.get(i).parameters.get(j).type.equals(parameters.get(j))) {
//                            break;
//                        }
//                    }
//                    return object.astNode.methods.get(i);
//                }
//            }
//        }

        for (int i = 0; i < object.astNode.methods.size(); i++) {
            if (doesMatch(object.astNode.methods.get(i), mc, parameters)) {
                return object.astNode.methods.get(i);
            }
        }
        throw new RuntimeException("Unable to resolve method call " + mc);
    }

    /**
     * Find a class, given the name. Just loops over the TranNode's classes member, matching by name.
     *
     * Loop over each class in the top node, comparing names to find a match.
     * @param name Name of the class to find
     * @return either a class node or empty if that class doesn't exist
     */
    private Optional<ClassNode> getClassByName(String name) {
        for (int i = 0; i < top.Classes.size(); i++) {
            if (top.Classes.get(i).name.equals(name)) {
                return Optional.of(top.Classes.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Given an execution environment (the current object, the current local variables), find a variable by name.
     *
     * @param name  - the variable that we are looking for
     * @param locals - the current method's local variables
     * @param object - the current object (so we can find members)
     * @return the IDT that we are looking for or throw an exception
     */
    private InterpreterDataType findVariable(String name, HashMap<String,InterpreterDataType> locals, Optional<ObjectIDT> object) {
//         if (object.isPresent()) {
//            ObjectIDT objectID = object.get();
//            if (objectID.members.containsKey(name)) {
//                return objectID.members.get(name);
//            }
////            throw new RuntimeException("Unable to find variable " + name);
//         } else if (locals.containsKey(name)) {
//             return locals.get(name);
//        }

        if (locals.containsKey(name)) {
            return locals.get(name);
//            throw new RuntimeException("Unable to find variable " + name);
        } else if (object.isPresent()) {
            ObjectIDT objectID = object.get();
            if (objectID.members.containsKey(name)) {
                return objectID.members.get(name);
            }
        }

        throw new RuntimeException("Unable to find variable " + name);
    }

    /**
     * Given a string (the type name), make an IDT for it.
     *
     * @param type The name of the type (string, number, boolean, character). Defaults to ReferenceIDT if not one of those.
     * @return an IDT with default values (0 for number, "" for string, false for boolean, ' ' for character)
     */
    private InterpreterDataType instantiate(String type) {
        if (type.equals("string")) {
            StringIDT stringidt = new StringIDT("");
//            stringidt.Value = type;
            return stringidt;
        } else if (type.equals("number")) {
            NumberIDT numberIDT = new NumberIDT(0);
            return numberIDT;
        } else if (type.equals("boolean")) {
            BooleanIDT booleanIDT = new BooleanIDT(false);
            return booleanIDT;
        } else if (type.equals("character")) {
            CharIDT charIDT = new CharIDT(' ');
            return charIDT;
        } else {
            ReferenceIDT referenceIDT = new ReferenceIDT();
            return referenceIDT;
        }
    }

}
