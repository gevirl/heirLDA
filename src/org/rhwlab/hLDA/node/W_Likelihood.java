/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import org.rhwlab.DAG.distributions.Distribution;

/**
 *
 * @author gevirl
 */
public class W_Likelihood implements Distribution {
    Z z;
    Phi phi;
    
    public W_Likelihood(Z z,Phi phi){
        this.z = z;
        this.phi = phi;
    }

    @Override
    public double logDensity(Object o) throws Exception {
        int[][] topics = (int[][])z.getValue();
        double[][] phiVal = (double[][])phi.getValue();
        
        double logP =0.0;
        int[][] docs = (int[][])o;
        for (int d=0 ; d<docs.length ; ++d){
            int[] words = docs[d];
            for (int w=0 ; w<words.length ; ++w){
                int topic = topics[d][w];
                logP = logP + Math.log(phiVal[topic][words[w]]);
            }
        }
        return logP;
    }
    @Override
    public Object sample() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Object getVariance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getMean() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
