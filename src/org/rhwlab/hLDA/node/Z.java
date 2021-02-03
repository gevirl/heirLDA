/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jsat.math.SpecialMath;
import org.rhwlab.DAG.FullConditional;
import org.rhwlab.DAG.Parameter;
import org.rhwlab.DAG.models.Model;
import org.rhwlab.hLDA.WorkerLDA;

/**
 *
 * @author gevirl
 */
public class Z extends FullConditional {

    int[][] docs;
    int nTopics;
    int nVocab;
    int[] ndTotal; // document total counts (length = nDocs)
    int[][] nw; // vocab counts (nVocab X nTopics)
    int[] nwsum;  // K , sum over vocab
    Alpha a;

    Collection<Callable<Object>> workers = new ArrayList<>();
    WorkerLDA[] workersArray;
    ExecutorService service;

    public Z(Model model, Theta theta, int[][] documents, int nTopics, int nVocab, Alpha a, Beta b, int nThreads, long seed) {
        super("z", model);
        this.a = a;
        docs = documents;
        ndTotal = new int[docs.length];
        for (int d = 0; d < ndTotal.length; ++d) {
            ndTotal[d] = ndTotal[d] + docs[d].length;
        }
        this.nTopics = nTopics;
        this.nVocab = nVocab;

        addParent(a);
 //       this.setPrior(new IndexedCategorical(theta));

        service = Executors.newWorkStealingPool();

        // build all the workers
        int nDocs = documents.length / nThreads + 1;  // number of documents per thread
        int start = 0;
        for (int t = 0; t < nThreads; ++t) {
            System.out.printf("Constructing worker %d\n", t);
            int n = Math.min(nDocs, documents.length - start);
            int[][] workerDocs = new int[n][];
            for (int i = 0; i < workerDocs.length; ++i) {
                workerDocs[i] = documents[i + start];
            }
            int[][] nwW = new int[nVocab][nTopics];  // each worker will have a local place for storing the global word counts
            int[] nwsumW = new int[nTopics];
            long nextSeed = seed + 10 * t;
            WorkerLDA worker = new WorkerLDA(String.format("Worker%d", t), workerDocs, nVocab, nTopics, a, b, nextSeed);
            worker.setGlobalWordCounts(nwsumW);
            worker.setGlobalWordTopicCounts(nwW);
            workers.add(worker);
            start = start + n;
        }
        workersArray = workers.toArray(new WorkerLDA[0]);
        nw = new int[nVocab][nTopics];
        nwsum = new int[nTopics];
        setValue(getZ());

        System.out.println("Accumulating initial counts");
        accumCounts();
    }

    // accumulate the word counts from the workers and pass the accumulated counts back to the workers
    private void accumCounts() {

        accumCounts(nw);  // calculate the word counts from all the workers

        // copy the new word counts to all the workers
        // each worker has a local area for these counts that it modifies during an iteraton
        for (int w = 0; w < workers.size(); ++w) {
            //           System.out.printf("copy counts to worker %d\n",w);
            int[][] c = workersArray[w].getGlobalWordTopicCounts();
            for (int i = 0; i < c.length; ++i) {
                for (int j = 0; j < c[i].length; ++j) {
                    c[i][j] = nw[i][j];

                }
            }
        }

        accumCounts(nwsum);
        for (int w = 0; w < workers.size(); ++w) {
            int[] c = workersArray[w].getGlobalWordCounts();
            for (int i = 0; i < c.length; ++i) {
                c[i] = nwsum[i];
            }
        }
    }

    private void accumCounts(int[][] result) {
        for (int i = 0; i < result.length; ++i) {
            Arrays.fill(result[i], 0);
        }

        for (WorkerLDA worker : workersArray) {
            //           System.out.printf("Accum count from worker %s\n", worker.workID);
            int[][] c = worker.getWordTopicCounts();
            for (int i = 0; i < c.length; ++i) {
                for (int j = 0; j < c[i].length; ++j) {
                    result[i][j] = result[i][j] + c[i][j];
                }
            }
        }
    }

    // adds up the word counts from all the workers individual counts
    private void accumCounts(int[] result) {
        Arrays.fill(result, 0);
        for (WorkerLDA worker : workersArray) {
            int[] c = worker.getWordCounts();
            for (int i = 0; i < c.length; ++i) {
                result[i] = result[i] + c[i];
            }
        }
    }

    @Override
    public void sample() throws Exception {

        service.invokeAll(workers);
        this.setValue(getZ());
        accumCounts();

    }

    public int[][] getZ() {
        int[][] ret = new int[docs.length][];
        int d = 0;
        for (int w = 0; w < this.workersArray.length; ++w) {
            int[][] workerZ = this.workersArray[w].getZ();
            for (int i = 0; i < workerZ.length; ++i) {
                ret[d] = workerZ[i];
                ++d;
            }
        }
        return ret;
    }

    public int[][] getDocumentCounts() {
        int[][] ret = new int[docs.length][];
        int d = 0;
        for (int w = 0; w < this.workersArray.length; ++w) {
            int[][] nd = this.workersArray[w].getDocumentTopicCounts();
            for (int i = 0; i < nd.length; ++i) {
                ret[d] = nd[i];
                ++d;
            }
        }
        return ret;
    }

    public int[] getDocumentTotals() {
        return ndTotal;
    }

    public int[][] getWordCounts() {
        return this.nw;
    }

    public int[] getWordTotals() {
        return this.nwsum;
    }

    public int[][] getVocabCounts() {
        return nw;
    }

    public int[] getVocabTotals() {
        return nwsum;
    }

    public int getNdocs() {
        return docs.length;
    }

    public int getNtopics() {
        return nTopics;
    }

    public int getNvocab() {
        return this.nVocab;
    }

    public int[][] getDocuments() {
        return docs;
    }

    @Override
    public Parameter copy(Model model) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reportValue(PrintStream stream, boolean first) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dump(PrintStream stream) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public double logConditionalProb() throws Exception {
        int[][] nd = getDocumentCounts();
        double a0 = (Double) a.getValue();
        double gammaA0 = SpecialMath.lnGamma(a0);
        double ak = a0 / nTopics;
        double gammaAk = SpecialMath.lnGamma(ak);
        
        double ret = 0.0;
        for (int d = 0; d < nd.length; ++d) {
            double logP = gammaA0 - SpecialMath.lnGamma(ndTotal[d]+a0);
            for (int t = 0; t < nTopics; ++t) {
                logP = logP + SpecialMath.lnGamma(nd[d][t]+ak) - gammaAk;
            }
            ret = ret + logP;
        }
        System.out.printf("Z logProb: %e\n", ret);
        return ret;
    }
    
/*
    @Override
    public double logConditionalProb() throws Exception {
        int[][] nd = getDocumentCounts();
        double a0 = (Double) a.getValue();
        double ak = a0 / nTopics;
        double ret = 0.0;
        for (int d = 0; d < nd.length; ++d) {
            double logP = Math.log(this.ndTotal[d]) + SpecialMath.lnBeta(a0, ndTotal[d]);
            for (int t = 0; t < nTopics; ++t) {
                if (nd[d][t] > 0) {
                    double den = Math.log(nd[d][t]) + SpecialMath.lnBeta(ak, nd[d][t]);
                    logP = logP - den;
                }
            }
            ret = ret + logP;
        }
        System.out.printf("Z logProb: %e\n", ret);
        return ret;
    }
*/
    
}
