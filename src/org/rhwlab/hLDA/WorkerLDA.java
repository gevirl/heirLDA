/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import org.rhwlab.hLDA.node.Alpha;
import org.rhwlab.hLDA.node.Beta;

/**
 *
 * @author gevirl
 */
public class WorkerLDA extends WorkerXML implements Callable {

    int thinning = 1;

    int[][] nwGlobal;  // a local copy of the global word counts (all documents of all the workers)
    int[] nwsumGlobal;

    int iter = 0;
    int iterOffset = 0;

    File dir;
    Alpha alpha;  // M by K -  Documents by Topics
    Beta beta;   // K by V - Topics by Vocab

    double b;
    double bV;
    double a;
    double aK;

    public WorkerLDA(String id, int[][] documents, int V, int K, Alpha alpha, Beta beta, long seed) {

        this.workID = id;
        this.documents = documents;
        this.V = V;
        this.K = K;
        this.alpha = alpha;
        this.beta = beta;
        this.rand = new Random(seed);
        initialState();
    }

    public WorkerLDA(File xml) throws Exception {
        super(xml);
        super.call();  // this reads the worker xml
    }

    final void initialState() {

        int M = documents.length;

        // initialise count variables.
        nw = new int[V][K];
        nd = new int[M][K];
        nwsum = new int[K];
        ndsum = new int[M];

        // The z_i are are initialised to values in [1,K] to determine the
        // initial state of the Markov chain.
        z = new int[M][];
        for (int m = 0; m < M; m++) {

            int N = documents[m].length;
            z[m] = new int[N];
            for (int n = 0; n < N; n++) {
                int topic = (int) (rand.nextDouble() * K);
                z[m][n] = topic;
                // number of instances of word i assigned to topic j
                nw[documents[m][n]][topic]++;
                // number of words in document i assigned to topic j.
                nd[m][topic]++;
                // total number of words assigned to topic j.
                nwsum[topic]++;
            }
            // total number of words in document i
            ndsum[m] = N;
        }
    }

    @Override
    public Object call() throws Exception {
        b = (Double) beta.getValue();
        bV = b / V;
        a = (Double) alpha.getValue();
        aK = a / K;
        ++iter;
        for (int m = 0; m < z.length; m++) {
            for (int n = 0; n < z[m].length; n++) {
                int topic = sampleFullConditional(m, n);
                z[m][n] = topic;
            }
        }
        return this.workID;
    }

    private int sampleFullConditional(int m, int n) {

        // remove z_i from the count variables
        int topic = z[m][n];
        nw[documents[m][n]][topic]--;
        nwGlobal[documents[m][n]][topic]--;
        nd[m][topic]--;
        nwsum[topic]--;
        nwsumGlobal[topic]--;
        ndsum[m]--;

        // do multinomial sampling via cumulative method:
        double[] p = new double[K];
        for (int k = 0; k < K; k++) {
            int w = documents[m][n];
            p[k] = (nwGlobal[w][k] + bV) / (nwsumGlobal[k] + b)
                    * (nd[m][k] + aK) / (ndsum[m] + a);
        }
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = rand.nextDouble() * p[K - 1];
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic]) {
                break;
            }
        }

        // add newly estimated z_i to count variables
        nw[documents[m][n]][topic]++;
        nwGlobal[documents[m][n]][topic]++;
        nd[m][topic]++;
        nwsum[topic]++;
        nwsumGlobal[topic]++;
        ndsum[m]++;

        return topic;
    }

    public int[][] getGlobalWordTopicCounts() {
        return this.nwGlobal;
    }

    public int[] getGlobalWordCounts() {
        return this.nwsumGlobal;
    }

    public void setDirectory(File dir) {
        this.dir = dir;
    }

    public void setCacheSize(int s) {
        this.cacheSize = s;
    }

    public void setThinning(int th) {
        this.thinning = th;
    }

    public void setOffset(int off) {
        this.iterOffset = off;
    }

    public void setGlobalWordTopicCounts(int[][] nwGlobal) {
        this.nwGlobal = nwGlobal;
    }

    public void setGlobalWordCounts(int[] nwsumGlobal) {
        this.nwsumGlobal = nwsumGlobal;
    }
}
