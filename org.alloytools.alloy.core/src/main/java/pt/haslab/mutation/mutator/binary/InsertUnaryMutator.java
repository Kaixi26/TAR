package pt.haslab.mutation.mutator.binary;

import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.Sig;
import pt.haslab.mutation.Location;
import pt.haslab.mutation.mutator.Mutator;
import pt.haslab.mutation.mutator.UnaryMutator;
import pt.haslab.util.ExprMaker;

import java.util.List;

public class InsertUnaryMutator extends Mutator {
    private InsertUnaryMutator(Location original, ExprUnary expr) {
        this.original = original;
        this.mutant = expr;
        this.name = expr.op.name();
    }

    public static void generate(List<Mutator> accumulator, Location original) {

        if (original.expr.type().is_bool) {
            for (ExprUnary.Op op : Mutator.uops_bool2bool) {
                accumulator.add(new InsertUnaryMutator(original, ExprMaker.make(original.expr, op)));
            }
        }
    }
}