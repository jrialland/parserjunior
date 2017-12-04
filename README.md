ParserJunior
============

[![Build Status](https://travis-ci.org/jrialland/parserjunior.svg)](https://travis-ci.org/jrialland/parserjunior)
[![jitpack](https://jitpack.io/v/jrialland/parserjunior.svg)](https://jitpack.io/#jrialland/parserjunior)
[![codecov](https://codecov.io/gh/jrialland/parserjunior/branch/master/graph/badge.svg)](https://codecov.io/gh/jrialland/parserjunior)



![Logo](src/junior.png)


This a lexer/parser library for java, using an approach for creating an executing grammar that do not involve code generation.

One might define a grammar using the library's dsl, and use its definition to parse any text.

All the documentation can be found on the project's [wiki](https://github.com/jrialland/parserjunior/wiki)

TL;DR, give me an example
---

If you are fluent with the concept of parsers and language recognition, here is a short example on how a grammar is defined :


```java

import net.jr.lexer.*;
import net.jr.parser.*;

public class FourOps extends Grammar {
    
        private static final Lexeme Number = Lexemes.cInteger();

        private static final Lexeme Plus = Lexemes.singleChar('+');

        private static final Lexeme Minus = Lexemes.singleChar('-');

        private static final Lexeme Mult = Lexemes.singleChar('*');

        private static final Lexeme Div = Lexemes.singleChar('/');

        private static final Forward Expr = new Forward("Expr");

        public FourOps() {

            //an expression can be just a number
            target(Expr)
                .def(Number)
                .withName("number");

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

}
```

The full FourOps example can be found here : [FourOpsTest](parser/src/test/java/net/jr/parser/FourOpsTest.java)
