package net.jr.cpreproc.procs;

import net.jr.collection.CollectionsUtil;
import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.cpreproc.lexer.PreprocToken;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.WithArgsMacroDefinition;
import net.jr.lexer.Terminal;

import java.util.*;
import java.util.stream.Collectors;

public class MacroExpander {

    private static final String VA_ARGS_KEY = "__VA_ARGS__";

    private static final PreprocToken DoubleSharp = new PreprocToken(PreprocToken.ConcatOperator, "##");

    private static final List<T> EmptyTList = Collections.emptyList();

    private static final List<String> EmptyStringList = Collections.emptyList();

    private static class T {

        PreprocToken token;

        Set<String> hideSet = new TreeSet<>();

        T(PreprocToken token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return token.getText();
        }

        public Set<String> getHideSet() {
            return hideSet;
        }

        public PreprocToken getToken() {
            return token;
        }
    }

    public static PreprocessorLine expand(Map<String, MacroDefinition> macroDefs, PreprocessorLine line) {
        List<PreprocToken> tokens = PreprocLexer.tokenize(line.getText());
        List<T> ts = tokens.stream().map(t -> new T(t)).collect(Collectors.toList());

        PreprocessorLine preprocLine = new PreprocessorLine(line.getPosition(), "");

        for (T t : expand(macroDefs, ts)) {
            PreprocToken token = t.getToken();
            preprocLine.extend(token.getPosition(), token.getText());
        }

        return preprocLine;
    }

    private static List<T> expand(Map<String, MacroDefinition> macroDefs, List<T> ts) {

        if (ts.isEmpty()) {
            return ts;
        }

        T t = ts.get(0);
        Set<String> hs = t.getHideSet();

        //TS is T HS • TS’ and T is in HS
        if (hs.contains(t.getToken().getText())) {
            //return T HS • expand(TS’ )
            List<T> tsPrime = ts.subList(1, ts.size());
            return CollectionsUtil.prependedList(t, expand(macroDefs, tsPrime));
        }

        //TS is T HS • TS’ and T is a "()-less macro"
        if (isNoArgMacro(macroDefs, t)) {
            List<T> tsPrime = ts.subList(1, ts.size());
            Set<String> hsUnionT = new TreeSet<>();
            hsUnionT.add(t.getToken().getText());
            List<T> substResult = subst(macroDefs, ts(macroDefs, t), EmptyStringList, EmptyTList, hsUnionT, new ArrayList<>());
            substResult.addAll(tsPrime);
            return expand(macroDefs, substResult);
        }


        List<T> nws;
        //TS is T HS • ( • TS’ and T is a "()’d macro"
        if (isWithArgsMacro(macroDefs, t) && !(nws = skipWhiteSpace(ts.subList(1, ts.size()))).isEmpty() && nws.get(0).getToken().getText().equals("(")) {
            WithArgsMacroDefinition macroDef = (WithArgsMacroDefinition) macroDefs.get(t.getToken().getText());
            List<T> tsPrime = nws.subList(1, nws.size());
            List<T> actuals = findActuals(tsPrime, macroDef);
            //TODO check TS’ is actuals • ) HS’ • TS’’ and actuals are "correct for T "
            if (!checkActuals(macroDefs, t, actuals)) {
                throw new RuntimeException(String.format("%s : wrong number of arguments", t.token.getText()));
            }
            List<T> tsPrimePrime = afterRightParenthesis(tsPrime);
            // (HS ∩ HS’) ∪{T }  (∩ is intersection operator)
            Set<String> hsPrime = actuals.stream().map(x -> x.hideSet).reduce(new TreeSet<>(), (a, b) -> {
                a.addAll(b);
                return a;
            });
            Set<String> tmpHs = new HashSet<>(t.hideSet);
            tmpHs.retainAll(hsPrime);
            tmpHs.add(t.token.getText());
            //return expand(subst(ts(T ),fp(T ),actuals,(HS ∩ HS’) ∪{T },{}) • TS’’ )
            List<T> substResult = subst(macroDefs, ts(macroDefs, t), fp(macroDefs, t), actuals, tmpHs, new ArrayList<>());
            substResult.addAll(tsPrimePrime);
            return expand(macroDefs, substResult);
        }

        //note TS must be T HS • TS’
        List<T> expandResult = expand(macroDefs, ts.subList(1, ts.size()));
        expandResult.add(0, t);
        return expandResult;
    }

    /*Given a macro-name token, ts returns the replacement token sequence from the macro’s
        definition.*/
    private static List<T> ts(Map<String, MacroDefinition> macroDefs, T t) {
        MacroDefinition def = macroDefs.get(t.getToken().getText());
        if (def == null) {
            return Collections.emptyList();
        } else {
            return def.getReplacement(t.getToken())
                    .stream()
                    .map(x -> new T(x))
                    .collect(Collectors.toList());
        }
    }


    private static List<T> subst(Map<String, MacroDefinition> macroDefs, List<T> is, List<String> fp, List<T> ap, Set<String> hs, List<T> os) {

        if (is.isEmpty()) {
            return hsadd(hs, os);
        }

        //IS is # • T • IS’ and T is FP[i ]
        if (is.get(0).token.getText().equals("#") && is.size() > 1) {
            T t = is.get(1);
            int i = getArgIndex(t, fp);
            if (i != -1) {
                List<T> isPrime = is.subList(2, is.size());
                //return subst(IS’,FP,AP,HS,OS • stringize(select(i,AP )))
                os.add(stringize(select(i, ap)));
                return subst(macroDefs, isPrime, fp, ap, hs, os);
            }
        }

        boolean startsWithConcat = is.get(0).token.getText().equals("##");
        if (startsWithConcat) {
            T t = is.get(1);
            List<T> isPrime = is.subList(2, is.size());
            int i = getArgIndex(t, fp);

            //IS is ## • T • IS’ and T is FP[i ]
            if (i != -1) {
                //is select(i,AP ) is {}
                if (select(i, ap).isEmpty()) {
                    //return subst(IS’,FP,AP,HS,OS );
                    return subst(macroDefs, isPrime, fp, ap, hs, os);
                } else {
                    return subst(macroDefs, isPrime, fp, ap, hs, glue(os, select(i, ap)));
                }
            }

            //IS is ## • T HS’ • IS’
            //return subst(IS’,FP,AP,HS,glue(OS,T HS’ ));
            return subst(macroDefs, isPrime, fp, ap, hs, glue(os, Arrays.asList(t)));

        }

        //IS is T • ## HS’ • IS’ and T is FP[i ]
        List<T> nws = skipWhiteSpace(is.subList(1, is.size()));
        if (!nws.isEmpty() && nws.get(0).token.getText().equals("##")) {
            T t = is.get(0);
            Set<String> hsPrime = t.hideSet;
            List<T> isPrime = nws.subList(1, nws.size());
            int i = getArgIndex(t, fp);
            List<T> selected = select(i, ap);
            if (selected.isEmpty()) {
                //if IS’ is T’ • IS’’ and T’ is FP[ j ]
                if (!isPrime.isEmpty()) {
                    T tPrime = isPrime.get(0);
                    List<T> isPrimePrime = isPrime.subList(1, isPrime.size());
                    int j = getArgIndex(tPrime, fp);
                    os.addAll(select(j, ap));
                    return subst(macroDefs, isPrimePrime, fp, ap, hs, os);
                } else {

                }
            } else {
                //return subst(## HS’ • IS’,FP,AP,HS,OS • select(i,AP ));
                T doubleSharp = new T(DoubleSharp);
                doubleSharp.hideSet = hsPrime;
                isPrime.add(0, doubleSharp);
                os.addAll(selected);
                return subst(macroDefs, isPrime, fp, ap, hs, os);
            }
        }

        //IS is T • IS’ and T is FP[i ]
        T t = is.get(0);
        List<T> isPrime = is.subList(1, is.size());
        int i = getArgIndex(t, fp);
        if (i != -1) {
            //return subst(IS’,FP,AP,HS,OS • expand(select(i,AP )));
            os.addAll(expand(macroDefs, select(i, ap)));
            return subst(macroDefs, isPrime, fp, ap, hs, os);
        }

        //note IS must be T HS’ • IS’
        //Set<String> hsPrime = t.hideSet;

        //return subst(IS’,FP,AP,HS,OS • T HS’ );
        os.add(t);
        return subst(macroDefs, isPrime, fp, ap, hs, os);
    }


    private static int getArgIndex(T t, List<String> args) {
        return args.indexOf(t.token.getText());
    }

    /**
     * add to token sequence’s hide sets
     */
    private static List<T> hsadd(Set<String> hideSet, List<T> ts) {
        for (T t : ts) {
            t.hideSet.addAll(hideSet);
        }
        return ts;
    }

    private static boolean isNoArgMacro(Map<String, MacroDefinition> macroDefs, T t) {
        MacroDefinition def = macroDefs.get(t.getToken().getText());
        return def != null && !def.hasArgs();
    }


    private static boolean isWithArgsMacro(Map<String, MacroDefinition> macroDefs, T t) {
        MacroDefinition def = macroDefs.get(t.token.getText());
        return def != null && def.hasArgs();
    }


    private static List<T> skipWhiteSpace(List<T> ts) {
        int i = 0;
        for (T t : ts) {
            if (t.getToken().getTokenType() != PreprocToken.WhiteSpace) {
                break;
            }
            i++;
        }
        return ts.subList(i, ts.size());
    }

    private static List<T> afterRightParenthesis(List<T> list) {
        int index = 0;
        for (T t : list) {
            if (t.getToken().getTokenType() == PreprocToken.RightParen) {
                return list.subList(index + 1, list.size());
            }
            index++;
        }
        return list;
    }


    private static List<T> findActuals(List<T> tsPrime, WithArgsMacroDefinition definition) {
        List<T> params = new ArrayList<>();
        T lastParam = null;
        for (T t : tsPrime) {
            Terminal type = t.getToken().getTokenType();
            if (type == PreprocToken.WhiteSpace) {
                continue;
            }
            if (type == PreprocToken.Comma || type == PreprocToken.RightParen) {
                if (lastParam != null) {
                    params.add(lastParam);
                }
            }
            if (type == PreprocToken.RightParen) {
                lastParam = null;
                break;
            }
            lastParam = t;
        }
        if (lastParam != null) {
            params.add(lastParam);
        }

        if (definition.isVariadic()) {
            int formalSize = definition.getFormalParameters().size();
            int variadicSize = params.size() - formalSize;
            if (variadicSize > 0) {
                List<T> variadic = params.subList(formalSize, params.size());
                String val = String.join(",", variadic.stream().map(t -> t.token.getText()).collect(Collectors.toList()));
                T varT = new T(new PreprocToken(PreprocToken.NoMeaning, val));
                while (params.size() != formalSize) {
                    params.remove(params.size() - 1);
                }
                params.add(varT);
            }
        }

        return params;
    }


    /**
     * Given a token sequence and an index i, select returns the i-th token sequence using the
     * comma tokens (not between nested parenthesis pairs) in the original token sequence as
     * delimiters.
     */
    protected static List<T> select(int index, List<T> ts) {
        if (index < 0 || index >= ts.size()) {
            return ts;
        } else {
            String txt = ts.get(index).getToken().getText();
            List<PreprocToken> reparsed = PreprocLexer.tokenize(txt);
            return reparsed.stream().map(t -> new T(t)).collect(Collectors.toList());
        }
    }

    /**
     * Given a token sequence, stringize returns a single string literal token containing the
     * concatenated spellings of the tokens.
     */
    private static T stringize(Collection<T> tokens) {
        String txt = tokens.stream()
                .map(t -> t.getToken().getText())
                .reduce("", (s1, s2) -> s1 + s2);
        PreprocToken tok = new PreprocToken(PreprocToken.StringLiteral, txt);
        return new T(tok);
    }


    /**
     * Given a macro-name token, fp returns the (ordered) list of formal parameters from the
     * macro’s definition.
     */
    private static List<String> fp(Map<String, MacroDefinition> macroDefs, T t) {
        MacroDefinition def = macroDefs.get(t.token.getText());
        List<String> params = def.getFormalParameters();
        if (def.isVariadic()) {
            params = new ArrayList<>(params);
            params.add(VA_ARGS_KEY);
        }
        return params;
    }


    private static boolean checkActuals(Map<String, MacroDefinition> macroDefs, T t, List<T> actuals) {
        WithArgsMacroDefinition def = (WithArgsMacroDefinition) macroDefs.get(t.token.getText());
        int argCount = def.getFormalParameters().size();
        if (def.isVariadic()) {
            return actuals.size() >= argCount;
        } else {
            return actuals.size() == argCount;
        }
    }


    /**
     * paste last of left side with first of right side
     */
    private static List<T> glue(List<T> ls, List<T> rs) {
        T l = ls.get(0);
        if (ls.size() == 1 && rs.size() >= 1) {
            T r = rs.remove(0);
            List<PreprocToken> pasteResult = PreprocLexer.tokenize(l.getToken().getText() + r.getToken().getText());
            List<T> ts = pasteResult.stream().map(T::new).collect(Collectors.toList());
            ts.addAll(rs);
            return ts;
        }

        List<T> lsPrime = ls.subList(1, ls.size());
        List<T> gs = glue(lsPrime, rs);
        gs.add(0, l);
        return gs;
    }
}
