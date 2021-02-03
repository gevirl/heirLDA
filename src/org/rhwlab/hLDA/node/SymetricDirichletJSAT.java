/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import java.util.Arrays;
import jsat.distributions.multivariate.Dirichlet;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import org.rhwlab.DAG.Function;
import org.rhwlab.DAG.ScalarParameter;
import org.rhwlab.DAG.distributions.Distribution;
import org.rhwlab.DAG.models.Model;

/**
 *
 * @author gevirl
 */
public class SymetricDirichletJSAT extends Function implements Distribution {
    ScalarParameter aParam;
    int k;
    
    double[] av;
    Vec aVec;
    Dirichlet dirichlet;
    
    public SymetricDirichletJSAT(String name,Model model,ScalarParameter aP,int k){
        super(name,model);
        this.aParam = aP;
        this.k = k;
        av = new double[k];
        double a = (Double)aParam.getValue();
        Arrays.fill(av, a/k); 
        aVec = new DenseVector(av);  
        dirichlet = new Dirichlet(aVec);
    }

    @Override
    public void update() {
        double a = (Double)aParam.getValue();
        Arrays.fill(av, a/k); 
    }

    
    @Override
    public double logDensity(Object o) throws Exception {
        double a = (Double)aParam.getValue();
        Arrays.fill(av, a/k);       
        double logP = 0.0;
        if (o instanceof double[]){
            double[] x = (double[])o;
            logP = dirichlet.logPdf(x);
        }
        else if (o instanceof double[][]){
            double[][] x = (double[][])o;
            for (int i=0 ; i<x.length ; ++i){
                logP = logP + dirichlet.logPdf(x[i]);
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
