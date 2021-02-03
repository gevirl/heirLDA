/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA;

import org.rhwlab.DAG.Function;
import org.rhwlab.DAG.models.Model;
import org.rhwlab.hLDA.node.Alpha;
import org.rhwlab.hLDA.node.Beta;
import org.rhwlab.hLDA.node.Phi;
import org.rhwlab.hLDA.node.SymetricDirichletJSAT;
import org.rhwlab.hLDA.node.Theta;
import org.rhwlab.hLDA.node.W;
import org.rhwlab.hLDA.node.Z;
import org.rhwlab.lda.BagOfWords;

/**
 *
 * @author gevirl
 */
public class HeirLDAModel extends Model {
    int nThreads;
    int nTopics;
    int nDocs;
    int nVocab;
    long seed = 1000;
 //   BagOfWords bow;
    int[][] docs;
    
    double initAlpha;
    double initBeta;
    double alphaSD;
    double betaSD;
    
    Alpha alpha;
    Beta beta ;
    Theta theta;
    Phi phi;
    Z z;
    W w;
    
    public HeirLDAModel(int[][] docs,int nVocab,int nTopics,int nThreads,double initA,double initB,double aSD,double bSD)throws Exception {
        this.docs = docs;
        this.nTopics = nTopics;
        this.nThreads = nThreads;
        initAlpha = initA;
        initBeta = initB;
        alphaSD = aSD;
        betaSD = bSD;
        
        nDocs = docs.length;
        this.nVocab = nVocab;
        
        alpha = new Alpha(initAlpha,this,alphaSD);
        this.addReportNode(alpha);
        
        beta = new Beta(initBeta,this,betaSD);
        this.addReportNode(beta);
        
        SymetricDirichletJSAT thetaPrior = new SymetricDirichletJSAT("ThetaPrior",this,alpha,nTopics);       
        theta = new Theta(alpha,nDocs,nTopics,thetaPrior,this);
        alpha.addListener(theta);
        
        SymetricDirichletJSAT phiPrior = new SymetricDirichletJSAT("BetaPrior",this,beta, nVocab);      
        phi = new Phi(beta,nTopics,nVocab,phiPrior,this);
        beta.addListener(phi);
        
        z = new Z(this,theta,docs,nTopics,nVocab,alpha,beta,nThreads,seed);
        z.addListener(theta);
        z.addListener(phi);
        
        w = new W(z,phi,beta);
        
        phi.setZ(z);
        theta.setZ(z);
    }
}
