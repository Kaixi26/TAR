package pt.haslab.util;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4TupleSet;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;
import kodkod.ast.Expression;
import kodkod.ast.Formula;
import pt.haslab.mutation.Candidate;
import pt.haslab.mutation.Location;
import pt.haslab.mutation.MutationStepper;
import pt.haslab.mutation.PruneReason;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Repairer {
    static final A4Reporter rep = new A4Reporter();
    static final A4Options opts = new A4Options();
    final Module module;
    Command command;
    ArrayList<Func> repairTargets;
    public MutationStepper mutationStepper;

    Map<Expr, Func> locationCorrespondentFunc = new HashMap<>();
    Map<Func, Expr> funcOriginalBody = new HashMap<>();
    List<Location> repairTargetLocations = new ArrayList<>();

    public Optional<Candidate> solution = Optional.empty();

    ArrayList<CounterExample> counterexamples = new ArrayList<>();

    private static class CounterExample {
        public final A4Solution cex;
        public int ocurrences;

        CounterExample(A4Solution cex, int ocurrences) {
            this.cex = cex;
            this.ocurrences = ocurrences;
        }

        boolean eval(Expr formula, int state){
            return (boolean) cex.eval(formula, state);
        }

        boolean evalAllStates(Expr formula) {
            for (int i = 0; i<cex.getTraceLength(); i++) {
                if(this.eval(formula, i)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return ocurrences + "";
        }
    }

    private RepairStatus repairStatus = RepairStatus.NOT_STARTED;

    public static enum RepairStatus {
        NOT_STARTED,
        FAIL,
        TIMEOUT,
        SUCCESS
    }

    private Repairer(Module module, Command command, ArrayList<Func> repairTargets) {
        this.module = module;
        this.command = command;
        this.repairTargets = repairTargets;
    }

    public static Repairer make(Module module, Command command, Collection<Func> repairTargets, int maxDepth) {
        Repairer ret = new Repairer(module, command, new ArrayList<>(repairTargets));

        for (Func repairTarget : repairTargets) {
            ret.funcOriginalBody.put(repairTarget, repairTarget.getBody());

            Collection<Location> repairTargetLocations = LocationAggregator.BreadthBottomUp(repairTarget.getBody());
            ret.repairTargetLocations.addAll(repairTargetLocations);

            for (Location repairTargetLocation : repairTargetLocations) {
                ret.locationCorrespondentFunc.put(repairTargetLocation.expr, repairTarget);
            }
        }

        ret.mutationStepper = MutationStepper.make(ret.repairTargetLocations, module.getAllReachableUserDefinedSigs(), maxDepth);

        return ret;
    }

    /* If true it means there might be a solution in the location of the current candidate */
    public boolean canPruneWithVariabilization(Candidate candidate, A4Solution ans) {
        for (Map.Entry<Func, Expr> e : funcOriginalBody.entrySet()) {
            Optional<Expr> variabilized = candidate.variabilize(e.getValue());
            if (variabilized.isPresent()) {
                e.getKey().setBody(variabilized.get());
            } else {
                return false;
            }
            //System.out.println(e.getKey().getBody());
        }
        return false;//(boolean) ans.eval(command.formula);
    }

    public boolean attemptPruneWithPreviousCounterexample() {
        for (int i = 0; i < counterexamples.size(); i++) {
            CounterExample counterExample = counterexamples.get(i);
            if (counterExample.evalAllStates(command.formula)) {
                counterExample.ocurrences++;
                if (i > 0 && counterExample.ocurrences > counterexamples.get(i - 1).ocurrences) {
                    Collections.swap(counterexamples, i, i - 1);
                }
                return true;
            }
        }
        return false;
    }


    public Optional<Candidate> repair() {
        return repair(0);
    }

    public Optional<Candidate> repair(long ms_timeout) {

        Instant time_begin = Instant.now();

        while (mutationStepper.next()) {
            if (ms_timeout != 0 && Duration.between(time_begin, Instant.now()).toMillis() > ms_timeout) {
                this.repairStatus = RepairStatus.TIMEOUT;
                return Optional.empty();
            }
            Candidate candidate = mutationStepper.getCurrent();

            for (Map.Entry<Func, Expr> e : funcOriginalBody.entrySet()) {
                e.getKey().setBody(candidate.apply(e.getValue()));
            }

            if (attemptPruneWithPreviousCounterexample()) {
                candidate.prunned = Optional.of(PruneReason.PREVIOUS_CEX);
                continue;
            }

            A4Solution ans =
                    TranslateAlloyToKodkod.execute_command(rep, module.getAllReachableSigs(), command, opts);

            if (!ans.satisfiable()) {
                //System.out.println("---------------");
                //System.out.println(candidate);
                //System.out.println(candidate.variabilizationID);
                //System.out.println("Found!");
                solution = Optional.of(candidate);
                repairStatus = RepairStatus.SUCCESS;
                return Optional.of(candidate);
            }

            counterexamples.add(new CounterExample(ans, 1));

            boolean prune = canPruneWithVariabilization(candidate, ans);
            if (prune) {
                mutationStepper.addVariabilization(candidate);
            }

            //System.out.println(candidate);
            //System.out.println("---------------");
            //System.out.println(candidate.variabilizationID);
            //System.out.println(prune);
            //for(Map.Entry<Func, Expr> e : funcOriginalBody.entrySet()){
            //    System.out.println(e.getKey().getBody());
            //}
            //System.out.println(ans);
            //System.exit(69);

        }

        repairStatus = RepairStatus.FAIL;
        return Optional.empty();
    }

    public long getGeneratedTotal() {
        return mutationStepper.candidates.size();
    }

    public long getPrunnedTotal() {
        return mutationStepper.candidates.stream().filter(c -> c.prunned.isPresent()).count();
    }

    public long getPrunnedBy(PruneReason pruneReason) {
        return mutationStepper.candidates.stream()
                .filter(c -> c.prunned.isPresent() && c.prunned.get().equals(pruneReason))
                .count();
    }

    public String generatedJSON() {
        return mutationStepper.candidates.get(0).toJSON();
    }

    public RepairStatus getRepairStatus() {
        return repairStatus;
    }
}