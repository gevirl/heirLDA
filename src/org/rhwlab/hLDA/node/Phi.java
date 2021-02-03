/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import javax.swing.event.ChangeEvent;
import org.rhwlab.DAG.Function;
import org.rhwlab.DAG.ScalarParameter;
import org.rhwlab.DAG.distributions.Distribution;
import org.rhwlab.DAG.models.Model;

/**
 *
 * @author gevirl
 */
public class Phi extends Function {

    ScalarParameter beta;
    Z z;


    public Phi(ScalarParameter beta, int nTopics, int nVocab, Distribution prior,Model model) {
        super("Phi", model);
        this.beta = beta;
    //    this.addParent(beta);
        this.setPrior(prior);


        setValue(new double[nTopics][nVocab]);

    }

    public void setZ(Z z) {
        this.z = z;
        update();
    }

    @Override
    public void stateChanged(ChangeEvent event) {

            update();
    }

    @Override
    public void update() {
        double b = (Double) beta.getValue();

        double[][] phis = (double[][]) getValue();
        int[][] nw = z.getWordCounts();
        int[] nwT = z.getWordTotals();

        double bV = b/z.getNvocab() ;

        for (int t = 0; t < z.getNtopics(); ++t) {
            for (int w = 0; w < z.getNvocab(); ++w) {
                phis[t][w] = (nw[w][t] + bV) / (nwT[t] + b);
            }
        }

        setValue(phis);

    }
    @Override
    public double logConditionalProb() throws Exception{
        double p = super.logConditionalProb();
        System.out.printf("Phi logProb: %e\n",p);
        return p;
    } 
}
