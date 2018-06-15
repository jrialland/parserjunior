package net.jr.cpreproc.macrodefs;

import net.jr.common.Position;
import net.jr.cpreproc.procs.PreprocessorLine;
import net.jr.grammar.c.CStringUtil;
import net.jr.lexer.Terminal;
import net.jr.lexer.Token;

import java.util.*;
import java.util.stream.Collectors;


/**
 * https://monoinfinito.wordpress.com/series/c-preprocessor-internals/
 * <p>
 * (quoted from http://elias.rhi.hi.is/cppinternals/cppinternals_8.html) :
 * <p>
 * Macro expansion is a tricky operation, fraught with nasty corner cases and situations that render what you thought was a nifty way to optimize the preprocessor's expansion algorithm wrong in quite subtle ways.
 * <p>
 * <p>I strongly recommend you have a good grasp of how the C and C++ standards require macros to be expanded before diving into this section, let alone the code!. If you don't have a clear mental picture of how things like nested macro expansion, stringification and token pasting are supposed to work, damage to your sanity can quickly result.</p>
 */
public class MacroExpander {

    private Map<String, MacroDefinition> definitions;

    public MacroExpander(Map<String, MacroDefinition> definitions) {
        this.definitions = definitions;
    }

    public String expand(String text) {
        PreprocessorLine line = new PreprocessorLine(new Position(1, 1), text);
        PreprocessorLine out = expand(line);
        return out.getText();
    }

    public PreprocessorLine expand(PreprocessorLine line) {
        List<Token> tokens = PreprocLexer.tokenize(line);
        String txt = "";
        SortedMap<Integer, Position> positions = new TreeMap<>();

        List<T> ts = tokens.stream().map(t -> new T(t)).collect(Collectors.toList());
        for (T t : expand(ts)) {
            txt += t.token.getText();
            positions.put(t.token.getStartIndex(), t.token.getPosition());
        }

        return new PreprocessorLine(positions, txt);
    }

    static class T {
        Token token;
        Set<String> hideSet = new TreeSet<>();

        public T(Token token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return token.getText();
        }
    }


    private static final List<T> EmptyTList = Collections.emptyList();

    private static final List<String> EmptyStringList = Collections.emptyList();

    private List<T> expand(List<T> ts) {

        if (ts.isEmpty()) {
            return ts;
        }

        T t = ts.get(0);
        Set<String> hs = t.hideSet;

        //TS is T HS • TS’ and T is in HS
        if (hs.contains(t.token.getText())) {
            //return T HS • expand(TS’ )
            List<T> tsPrime = ts.subList(1, ts.size());
            List<T> result = new ArrayList<>();
            result.add(t);
            result.addAll(expand(tsPrime));
            return result;
        }

        //TS is T HS • TS’ and T is a "()-less macro"
        if (isNoArgMacro(t)) {
            List<T> tsPrime = ts.subList(1, ts.size());
            //return expand(subst(ts(T ),{},{},HS ∪{T },{}) • TS’ )
            Set<String> hsUnionT = new TreeSet<>(hs);
            hsUnionT.add(t.token.getText());
            List<T> substResult = subst(ts(t), EmptyStringList, EmptyTList, hsUnionT, new ArrayList<>());
            substResult.addAll(tsPrime);
            return expand(substResult);
        }

        List<T> nws;
        //TS is T HS • ( • TS’ and T is a "()’d macro"
        if (isWithArgsMacro(t) && !(nws = skipWhiteSpace(ts.subList(1, ts.size()))).isEmpty() && nws.get(0).token.getText().equals("(")) {
            WithArgsMacroDefinition macroDef = (WithArgsMacroDefinition) definitions.get(t.token.getText());
            List<T> tsPrime = nws.subList(1, nws.size());
            List<T> actuals = findActuals(tsPrime, macroDef);
            //TODO check TS’ is actuals • ) HS’ • TS’’ and actuals are "correct for T "
            if (!checkActuals(t, actuals)) {
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
            List<T> substResult = subst(ts(t), fp(t), actuals, tmpHs, new ArrayList<>());
            substResult.addAll(tsPrimePrime);
            return expand(substResult);
        }

        //note TS must be T HS • TS’
        List<T> expandResult = expand(ts.subList(1, ts.size()));
        expandResult.add(0, t);
        return expandResult;
    }

    private List<T> afterRightParenthesis(List<T> list) {
        int index = 0;
        for (T t : list) {
            if (t.token.getTokenType() == PreprocLexer.TokenType.RightParen) {
                return list.subList(index + 1, list.size());
            }
            index++;
        }
        return list;
    }

    private List<T> skipWhiteSpace(List<T> ts) {
        int i = 0;
        for (T t : ts) {
            if (t.token.getTokenType() != PreprocLexer.TokenType.WhiteSpace) {
                break;
            }
            i++;
        }
        return ts.subList(i, ts.size());
    }

    private boolean checkActuals(T t, List<T> actuals) {
        WithArgsMacroDefinition def = (WithArgsMacroDefinition) definitions.get(t.token.getText());
        int argCount = def.getFormalParameters().size();
        if (def.isVariadic()) {
            return actuals.size() >= argCount;
        } else {
            return actuals.size() == argCount;
        }
    }

    private List<T> findActuals(List<T> tsPrime, WithArgsMacroDefinition definition) {
        List<T> params = new ArrayList<>();
        T lastParam = null;
        for (T t : tsPrime) {
            Terminal type = t.token.getTokenType();
            if (type == PreprocLexer.TokenType.WhiteSpace) {
                continue;
            }
            if (type == PreprocLexer.TokenType.Comma || type == PreprocLexer.TokenType.RightParen) {
                if (lastParam != null) {
                    params.add(lastParam);
                }
            }
            if (type == PreprocLexer.TokenType.RightParen) {
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
                T varT = new T(new Token(val));
                while (params.size() != formalSize) {
                    params.remove(params.size() - 1);
                }
                params.add(varT);
            }
        }

        return params;
    }

    private List<T> subst(List<T> is, List<String> fp, List<T> ap, Set<String> hs, List<T> os) {

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
                return subst(isPrime, fp, ap, hs, os);
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
                    return subst(isPrime, fp, ap, hs, os);
                } else {
                    return subst(isPrime, fp, ap, hs, glue(os, select(i, ap)));
                }
            }

            //IS is ## • T HS’ • IS’
            //return subst(IS’,FP,AP,HS,glue(OS,T HS’ ));
            return subst(isPrime, fp, ap, hs, glue(os, Arrays.asList(t)));

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
                    return subst(isPrimePrime, fp, ap, hs, os);
                } else {

                }
            } else {
                //return subst(## HS’ • IS’,FP,AP,HS,OS • select(i,AP ));
                T doubleSharp = new T(new Token("##"));
                doubleSharp.hideSet = hsPrime;
                isPrime.add(0, doubleSharp);
                os.addAll(selected);
                return subst(isPrime, fp, ap, hs, os);
            }
        }

        //IS is T • IS’ and T is FP[i ]
        T t = is.get(0);
        List<T> isPrime = is.subList(1, is.size());
        int i = getArgIndex(t, fp);
        if (i != -1) {
            //return subst(IS’,FP,AP,HS,OS • expand(select(i,AP )));
            os.addAll(expand(select(i, ap)));
            return subst(isPrime, fp, ap, hs, os);
        }

        //note IS must be T HS’ • IS’
        //Set<String> hsPrime = t.hideSet;

        //return subst(IS’,FP,AP,HS,OS • T HS’ );
        os.add(t);
        return subst(isPrime, fp, ap, hs, os);
    }

    /*paste last of left side with first of right side*/
    private List<T> glue(List<T> ls, List<T> rs) {
        T l = ls.get(0);
        if (ls.size() == 1 && rs.size() >= 1) {
            T r = rs.remove(0);
            List<Token> pasteResult = Tokenizer.tokenize(l.token.getText() + r.token.getText());
            List<T> ts = pasteResult.stream().map(t -> new T(t)).collect(Collectors.toList());
            ts.addAll(rs);
            return ts;
        }

        List<T> lsPrime = ls.subList(1, ls.size());
        List<T> gs = glue(lsPrime, rs);
        gs.add(0, l);
        return gs;
    }

    /**
     * add to token sequence’s hide sets
     */
    private List<T> hsadd(Set<String> hideSet, List<T> ts) {
        for (T t : ts) {
            t.hideSet.addAll(hideSet);
        }
        return ts;
    }

    /*Given a macro-name token, ts returns the replacement token sequence from the macro’s
    definition.*/
    private List<T> ts(T t) {
        MacroDefinition def = definitions.get(t.token.getText());
        if (def == null) {
            return Collections.emptyList();
        } else {
            return def.getReplacement().stream().map(x -> new T(x)).collect(Collectors.toList());
        }
    }

    /**
     * Given a macro-name token, fp returns the (ordered) list of formal parameters from the
     * macro’s definition.
     */
    private List<String> fp(T t) {
        MacroDefinition def = definitions.get(t.token.getText());
        List<String> params = def.getFormalParameters();
        if (def.isVariadic()) {
            params = new ArrayList<>(params);
            params.add("__VA_ARGS__");
        }
        return params;
    }

    /**
     * Given a token sequence, stringize returns a single string literal token containing the
     * concatenated spellings of the tokens.
     */
    protected T stringize(Collection<T> tokens) {
        String raw = Token.toString(tokens.stream().map(t -> t.token).collect(Collectors.toList()));
        Token token = new Token(PreprocLexer.TokenType.StringLiteral, token.getPosition(), CStringUtil.escapeC(raw.getBytes()));
        return new T(token);
    }

    protected boolean isNoArgMacro(T t) {
        MacroDefinition def = definitions.get(t.token.getText());
        return def != null && !def.hasArgs();
    }

    protected boolean isWithArgsMacro(T t) {
        MacroDefinition def = definitions.get(t.token.getText());
        return def != null && def.hasArgs();
    }

    /**
     * Given a token sequence and an index i, select returns the i-th token sequence using the
     * comma tokens (not between nested parenthesis pairs) in the original token sequence as
     * delimiters.
     */
    protected List<T> select(int index, List<T> ts) {
        if (index < 0 || index >= ts.size()) {
            return ts;
        } else {
            return PreprocLexer.tokenize(ts.get(index).token.getText()).stream().map(t -> new T(t)).collect(Collectors.toList());
        }
    }

    protected int getArgIndex(T t, List<String> args) {
        return args.indexOf(t.token.getText());
    }
}
