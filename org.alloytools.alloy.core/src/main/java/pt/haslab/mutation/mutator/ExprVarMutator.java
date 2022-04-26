package pt.haslab.mutation.mutator;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.Sig;
import pt.haslab.util.ExprMaker;

import java.util.List;

public class ExprVarMutator {

    /*
        B subset of A
        C subset of A
        B ~> C
     */
    static private class ReplaceMutator extends Mutator {
        private ReplaceMutator(Expr expr, Expr original) {
            this.original = original;
            this.mutant = expr;
            this.name = original + "->" + expr;
        }

        public static void generate(List<Mutator> accumulator, Expr expr, ConstList<Sig> sigs) {
            for (Sig sig : sigs) {
                if (expr.type() == sig.type()) {
                    accumulator.add(new ReplaceMutator(ExprMaker.make(sig, ExprUnary.Op.NOOP), expr));
                }
            }
        }

        public static void generate(List<Mutator> accumulator, Expr expr, List<ExprHasName> vars) {
            for (ExprHasName var : vars) {
                if (expr.type() == var.type() && !expr.equals(var)) {
                    accumulator.add(new ReplaceMutator(ExprMaker.make(var, ExprUnary.Op.NOOP), expr));
                }
            }
        }
    }

    public static void generate(List<Mutator> accumulator, Expr expr, ConstList<Sig> sigs, List<ExprHasName> vars) {
        ReplaceMutator.generate(accumulator, expr, sigs);
        ReplaceMutator.generate(accumulator, expr, vars);
    }
}
