package net.jr.cpreproc.procs;

import net.jr.cpreproc.macrodefs.MacroDefinition;

import javax.script.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ExpressionEval {


    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private static final Bindings engineBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);

    private static boolean isLegalConstant(String val) {
        return !val.isEmpty() && val.matches("^[_a-zA-Z][_a-zA-Z0-9]*$");
    }

    public static boolean eval(String expression, Map<String, MacroDefinition> macroDefs) {

        //'defined' may appear as keyword an not a function (i.e 'defined FOO' instead of 'defined(FOO)')
        //so we shamelessly use a regex to force the function-style syntax
        expression = expression.replaceAll("defined\\p{Blank}+([_a-zA-Z][_a-zA-Z0-9]*)", "defined(\"$1\")");
        expression = expression.replaceAll("defined\\p{Blank}*\\(\\p{Blank}*([_a-zA-Z][_a-zA-Z0-9]*)\\p{Blank}*\\)", "defined(\"$1\")");

        try {

            scriptEngine.getContext().setAttribute("defined", new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    return macroDefs.containsKey(s);
                }
            }, ScriptContext.ENGINE_SCOPE);


            Object obj = scriptEngine.eval("!!function(){return(" + expression + ");}();");
            return obj != null && ((Boolean) obj).booleanValue();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


}
