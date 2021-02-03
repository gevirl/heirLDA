/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.rhwlab.DAG.distributions.Distribution;
import org.rhwlab.DAG.proposal.ProposalDistribution;
import org.rhwlab.hLDA.WorkerLDA;

/**
 *
 * @author gevirl
 */
public class ZProposal implements ProposalDistribution{
    int[][] nw;  //  V x K ,vocab by topic
    int[] nwsum;  // K , sum over vocab

    Collection<Callable<Object>> workers = new ArrayList<>();
    WorkerLDA[] workersArray;
    
    public ZProposal(int[][] documents, int V, int K, Alpha alpha, Beta beta, int nThreads, long seed){
        // build all the workers
        int nDocs = documents.length / nThreads + 1;  // number of documents per thread
        int start = 0;
        for (int t = 0; t < nThreads; ++t) {
            System.out.printf("Constructing worker %d\n",t);
            int n = Math.min(nDocs, documents.length - start);
            int[][] workerDocs = new int[n][];
            for (int i = 0; i < workerDocs.length; ++i) {
                workerDocs[i] = documents[i + start];
            }
            int[][] nwW = new int[V][K];  // each worker will have a local place for storing the global word counts
            int[] nwsumW = new int[K];
            long nextSeed = seed + 10 * t;
            WorkerLDA worker = new WorkerLDA(String.format("Worker%d", t), workerDocs, V, K, alpha, beta, nextSeed);
            worker.setGlobalWordCounts(nwsumW);
            worker.setGlobalWordTopicCounts(nwW);
            workers.add(worker);
            start = start + n;
        }
        workersArray = workers.toArray(new WorkerLDA[0]);      
        nw = new int[V][K];
        nwsum = new int[K];

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
    public double logDensity(Object from, Object to) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object sample(Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSamplingDistribution(Distribution dist) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean adapt(int i, int a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProposalDistribution copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean adapt(int a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
