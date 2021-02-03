/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import org.rhwlab.DAG.ScalarParameter;
import org.rhwlab.DAG.proposal.GaussianRandomWalk;

/**
 *
 * @author gevirl
 */
public class ScaledGaussianRandomWalk extends GaussianRandomWalk{
    ScalarParameter base;
    
    public ScaledGaussianRandomWalk(String name,ScalarParameter p,double proposalSD){
        super(name,proposalSD);
        base = p;
    }
    
        @Override
    public Object sample(Object from) {
        double sig = this.getSigma();
        double mu = (Double)base.getValue();
        double mu3 = mu/3.0;
        if (sig > mu3){
            setSigma(mu3);
        }
        return super.sample(from);
    }
}
