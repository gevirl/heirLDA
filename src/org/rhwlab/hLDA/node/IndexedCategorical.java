/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import org.rhwlab.DAG.Node;
import org.rhwlab.DAG.distributions.Distribution;

/**
 *
 * @author gevirl
 */
public class IndexedCategorical implements Distribution {
    Node theta;
    
    public IndexedCategorical(Node p){
        this.theta = p;
    }
    @Override
    public Object sample() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double logDensity(Object o) throws Exception {
        double[][] thetaVal = (double[][])theta.getValue();
        int[][] z = (int[][])o;
        double logP = 0.0;
        for (int i=0 ; i<z.length ; ++i){
            int[] topics = z[i];
            for (int j=0 ; j<topics.length ; ++j){
                int topic = topics[j];
                logP = logP + Math.log(thetaVal[i][topic]);
            }
        }
        return logP;
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
