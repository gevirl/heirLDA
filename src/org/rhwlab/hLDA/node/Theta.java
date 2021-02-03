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
public class Theta extends Function {
    ScalarParameter alpha;
    Z z;

    
    public Theta(ScalarParameter alpha,int nDocs,int nTopics,Distribution prior,Model model){
        super("Theta",model);
        this.alpha = alpha;
//        this.addParent(alpha);
        this.setPrior(prior);
        

        
        setValue(new double[nDocs][nTopics]);

    }
    
    public void setZ(Z z){
        this.z = z;
        update();        
    }
    
    @Override
    public void stateChanged(ChangeEvent event){

            update();
        
    }
    
    @Override
    public void update() {
        double a = (Double)alpha.getValue();
        
        double[][] thetas = (double[][])getValue();
        int[][] nd = z.getDocumentCounts();
        int[] ndT = z.getDocumentTotals();
        
        double aT = a/z.getNtopics();
        for (int d=0 ; d<nd.length ; ++d){
            for (int t=0 ; t<z.getNtopics() ; ++t){
                thetas[d][t] = (nd[d][t] + aT)/(ndT[d] + a);
            }
        }
        setValue(thetas);

    }

    public ScalarParameter getAlpha(){
        return this.alpha;
    }
    @Override
    public double logConditionalProb() throws Exception{
        double p = super.logConditionalProb();
        System.out.printf("Theta logProb: %e\n",p);
        return p;
    }     
}
