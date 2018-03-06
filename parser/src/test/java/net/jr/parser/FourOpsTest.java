package net.jr.parser;

import net.jr.lexer.Terminal;
import net.jr.lexer.Lexemes;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;
import net.jr.parser.ast.annotations.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Stack;

public class FourOpsTest {

    static class FourOps extends Grammar {

        private static final Terminal Number = Lexemes.cInteger();

        private static final Terminal Plus = Lexemes.singleChar('+');

        private static final Terminal Minus = Lexemes.singleChar('-');

        private static final Terminal Mult = Lexemes.singleChar('*');

        private static final Terminal Div = Lexemes.singleChar('/');

        private static final NonTerminal Expr = new NonTerminal("Expr");

        public FourOps() {

            //an expression can be just a number
            target(Expr)
                    .def(Number).withName("number");

            //an expression can be an addition, or a substraction
            target(Expr)
                    .def(Expr, oneOf(Plus, Minus), Expr)
                    .withAssociativity(Associativity.Left)
                    .withName("additiveExpression");

            //an expression can also be a multiplication or a division
            target(Expr)
                    .def(Expr, oneOf(Mult, Div), Expr)
                    .withAssociativity(Associativity.Left)
                    .withName("multiplicativeExpression");

            //multiplications are always computed before additions
            setPrecedenceLevel(20, Mult, Div);
            setPrecedenceLevel(10, Plus, Minus);
        }

        public int compute(String expression) {
            final AstNode rootNode = createParser().parse(expression);

            Stack<Integer> stack = new Stack<>();

            VisitorHelper.visit(rootNode, new Object() {

                @After("additiveExpression")
                public void additiveExpression(AstNode node) {
                    int topOfStack = stack.pop();
                    int nextInStack = stack.pop();
                    String operation = node.getChildren().get(1).asToken().getText();
                    int result = operation.equals("+") ? nextInStack + topOfStack : nextInStack - topOfStack;
                    stack.push(result);
                }

                @After("multiplicativeExpression")
                public void multiplicativeExpression(AstNode node) {
                    int topOfStack = stack.pop();
                    int nextInStack = stack.pop();
                    String operation = node.getChildren().get(1).asToken().getText();
                    int result = operation.equals("*") ? nextInStack * topOfStack : nextInStack / topOfStack;
                    stack.push(result);
                }

                @After("number")
                public void number(AstNode node) {
                    stack.push(Integer.parseInt(node.asToken().getText()));
                }

            });

            return stack.pop();
        }
    }

    @Test
    public void test() {
        FourOps fourOps = new FourOps();
        Assert.assertEquals(8, fourOps.compute("15-7"));
        Assert.assertEquals(18, fourOps.compute("3*6"));
        Assert.assertEquals(5, fourOps.compute("50/10"));
        Assert.assertEquals(32, fourOps.compute("3*6+2*7"));
        Assert.assertEquals(4, fourOps.compute("1-2+3-4+5-6+7"));
    }


}
