/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA.node;

import org.rhwlab.DAG.ScalarParameter;
import org.rhwlab.DAG.distributions.InverseGamma;
import org.rhwlab.DAG.models.Model;

/**
 *
 * @author gevirl
 */
public class Alpha extends ScalarParameter {
    public Alpha(double initValue,Model model,double proposalSD){
        super("Alpha",model);
        setValue(new Double(initValue));
        setPrior(new InverseGamma(1.0,1.0));
        ScaledGaussianRandomWalk prop = new ScaledGaussianRandomWalk("AlphaProposal",this,proposalSD);
       
        this.setProposal(prop);
    }
    @Override
    public double logConditionalProb() throws Exception{
        double p = super.logConditionalProb();
        System.out.printf("Alpha logProb: %e\n",p);
        return p;
    }     
}
