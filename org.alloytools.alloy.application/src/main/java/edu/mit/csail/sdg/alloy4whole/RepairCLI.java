/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4whole;

import edu.mit.csail.sdg.alloy4.Err;
import pt.haslab.mutation.Candidate;
import pt.haslab.mutation.mutator.Mutator;
import pt.haslab.util.JSON;
import pt.haslab.util.RepairChecker;
import pt.haslab.util.Repairer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


public final class RepairCLI {
    static int depth = 3;
    static long timeout = 60;
    static String filepath = null;
    static String output_filepath = null;
    static boolean debug = false;
    static boolean enableVariabilization = false;

    private static void usage() {
        System.err.println("usage: repairer [OPTION]...");
        Stream.of("--depth [INT]", "--timeout [SECS]", "--file [FILE]", "--out [FILE] (Optional)", "--enable-variabilization")
                .forEach(option -> System.err.println("\t" + option));
        System.exit(-1);
    }

    private static void usageError(String errorMessage) {
        System.err.println(errorMessage);
        usage();
    }


    private static void parseArgument(Queue<String> args) {
        String arg = args.remove();
        switch (arg) {
            case "--depth":
                try {
                    depth = Integer.parseInt(args.remove());
                } catch (NumberFormatException ignored) {
                    usageError("Invalid natural given for depth.");
                }
                break;
            case "--timeout":
                try {
                    timeout = Integer.parseInt(args.remove());
                } catch (NumberFormatException ignored) {
                    usageError("Invalid integer given for timeout.");
                }
                break;
            case "--file":
                filepath = args.remove();
                break;
            case "--out":
                output_filepath = args.remove();
                break;
            case "--debug":
                debug = true;
                break;
            case "--enable-variabilization":
                enableVariabilization = true;
                break;
            default:
                usageError("Unknown option '" + arg + "'.");
        }
    }

    private static void parseArguments(String[] args) {
        Queue<String> argQueue = new ArrayDeque<>(Arrays.asList(args));
        while (argQueue.size() > 0) {
            parseArgument(argQueue);
        }
        if (depth < 0) {
            usageError("Given depth is below 0.");
        } else if (timeout < 0) {
            usageError("Given timeout is below 0.");
        } else if (filepath == null) {
            usageError("filepath is required");
        }

    }

    public static void main(String[] args) throws Err, IOException {
        parseArguments(args);

        Repairer repairer = RepairChecker.attemptRepair(filepath, depth, 1000 * timeout, enableVariabilization);

        Map<String, String> json = new HashMap<>();
        json.put("elapsed", "" + repairer.getElapsedMillis());
        json.put("depth", "" + depth);
        json.put("timeout", "" + timeout);
        json.put("file", "\"" + filepath + "\"");
        json.put("solved", "" + repairer.solution.isPresent());
        json.put("timed_out", "" + repairer.getRepairStatus().equals(pt.haslab.util.Repairer.RepairStatus.TIMEOUT));
        System.out.println(JSON.toJSON(json));

        if (debug) {
            for (Mutator baseMutator : repairer.mutationStepper.baseMutators) {
                System.out.println(baseMutator);
                for (Mutator mutator : baseMutator.getGeneratedMutators()) {
                    System.out.println("\t" + mutator);
                }
            }
        }

        switch (repairer.getRepairStatus()) {
            case ALREADY_CORRECT:
            case SUCCESS:
                System.exit(0);
            case TIMEOUT:
                System.exit(1);
            case FAIL:
                System.exit(2);
            default:
                System.exit(-1);
        }
    }
}