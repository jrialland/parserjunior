package net.jr.cpreproc.procs;

public class ExpressionEval {

    private static boolean isLegalConstant(String val) {
        return !val.isEmpty() && val.matches("^[_a-zA-Z][_a-zA-Z0-9]*$");
    }

    public boolean eval(String expression) {

        //'defined' may appear as keyword an not a function (i.e 'defined FOO' instead of 'defined(FOO)')
        //so we shamelessly use a regex to force the function-style syntax
        expression = expression.replaceAll("defined\\p{Blank}+([_a-zA-Z][_a-zA-Z0-9]*)", "defined($1)");


    }


}
