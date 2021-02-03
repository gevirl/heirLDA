/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import jsat.math.SpecialMath;
import org.rhwlab.DAG.Data;

/**
 *
 * @author gevirl
 */
public class W extends Data {

    Z z;
    Phi phi;
    Beta beta;

    public W(Z z, Phi phi, Beta beta) {
        this.z = z;
        this.phi = phi;
        this.beta = beta;

        this.addParent(beta);
        //    this.addParent(z);

        setValue(z.getDocuments());
        this.setLikelihood(new W_Likelihood(z, phi));

    }

    @Override
    public double logConditionalProb() throws Exception {
        double ret = 0.0;
        double a0 = (Double) beta.getValue();
        double gammaA0 = SpecialMath.lnGamma(a0);
        double ak = a0 / z.getNvocab();
        double gammaAk = SpecialMath.lnGamma(ak);
        
        int[][] nw = z.getVocabCounts();
        int[] nwT = z.getVocabTotals();

        for (int t = 0; t < z.getNtopics(); ++t) {
            double logP = gammaA0 - SpecialMath.lnGamma(nwT[t] + a0);
            for (int v = 0; v < z.getNvocab(); ++v) {
                logP = logP + SpecialMath.lnGamma(nw[v][t]+ak) - gammaAk;
            }
            ret = ret + logP;
        }
        System.out.printf("W logProb: %e\n", ret);
        return ret;
    }
/*    
    @Override
    public double logConditionalProb() throws Exception {
        double ret = 0.0;
        double a0 = (Double) beta.getValue();
        double ak = a0 / z.getNvocab();

        int[][] nw = z.getVocabCounts();
        int[] nwT = z.getVocabTotals();

        for (int t = 0; t < z.getNtopics(); ++t) {
            double logP = Math.log(nwT[t]) + SpecialMath.lnBeta(a0, nwT[t]);
            for (int v = 0; v < z.getNvocab(); ++v) {
                if (nw[v][t] > 0) {
                    double den = Math.log(nw[v][t]) + SpecialMath.lnBeta(ak, nw[v][t]);
                    logP = logP - den;
                }
            }
            ret = ret + logP;
        }
        System.out.printf("W logProb: %e\n", ret);
        return ret;
    }
*/
}
