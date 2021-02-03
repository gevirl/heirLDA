/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.rhwlab.DAG.models.GibbsMetropolis;
import org.rhwlab.command.CommandLine;
import org.rhwlab.lda.BagOfWords;
import org.rhwlab.lda.MatrixMarket;
import org.rhwlab.lda.OriginalBOW;

/**
 *
 * @author gevirl
 */
public class HeirLDACommandLine extends CommandLine {

    BagOfWords[] bows;
    String outFile;
    int nTopics=50;
    int nThreads=1;
    double initA = 1.0;
    double initB = 20000.0;
    double aSD =  0.1;
    double bSD = 100.0;

    int burnIn = 0;
    int iterations = 10000;
    int thinning = 1;

    int nVocab;
    int[][] docs;
    
    @Override
    public void init() {

    }

    @Override
    public String post() {
        String ret = null;
        try {
            docs = BagOfWords.toDocumentFormat(bows);
            HeirLDAModel model = new HeirLDAModel(docs,bows[0].getVocabSize(), nTopics, nThreads, initA, initB, aSD, bSD);
            model.setOutFile(outFile);
            GibbsMetropolis alg = new GibbsMetropolis();
            alg.setModel(model);
            alg.setBurnIn(burnIn);
            alg.setIterations(iterations);
            alg.setThining(thinning);

            alg.run();

        } catch (Exception exc) {
            exc.printStackTrace();
            ret = exc.getMessage();
        }
        return ret;
    }

    public String out(String s){
        this.outFile = s;
        return null;
    }
    
    static public ArrayList<String> readFile(String s)throws Exception {
        ArrayList<String> files = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(s));
        String file = reader.readLine();
        while (file != null) {
            files.add(file.trim());
            file = reader.readLine();
        }
        reader.close();
        return files;        
    }
    
    public boolean checkBOWS(){
        int nV = bows[0].getVocabSize();
        for (int i=1 ; i<bows.length ; ++i){
            if (bows[i].getVocabSize() != nV){
                return false;
            }
        }
        return true;
    }
    public String fromFileListOrig(String s,boolean bin){
        try{
            ArrayList<String> files = readFile(s);
            
            bows = new OriginalBOW[files.size()];
            for (int i=0 ; i<bows.length ; ++i){
                bows[i] = new OriginalBOW(files.get(i),bin);
            }
        } catch (Exception exc){
            return exc.getMessage();
        }
        if (!checkBOWS()){
            return "Not all bow files have the same vocabulary size";
        }
        return null;
    }
   
    public String bowFiles(String s){
        return fromFileListOrig(s,false);
    }
    public String bowFilesBin(String s){
        return fromFileListOrig(s,true);
    }
       
    public String bow(String s) {
        try {
            bows = new OriginalBOW[1];
            bows[0] = new OriginalBOW(new File(s), false);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String bowBin(String s) {
        try {
            bows = new OriginalBOW[1];
            bows[0] = new OriginalBOW(new File(s), true);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

      
    public String fromFileListMM(String s,boolean bin){
        try{
            ArrayList<String> files = readFile(s);
            
            bows = new MatrixMarket[files.size()];
            for (int i=0 ; i<bows.length ; ++i){
                bows[i] = new MatrixMarket(files.get(i),bin);
            }
        } catch (Exception exc){
            return exc.getMessage();
        }
        if (!checkBOWS()){
            return "Not all mm files have the same vocabulary size";
        }        
        return null;
    } 
    public String mmFiles(String s){
        return fromFileListMM(s,false);
    }
    public String mmFilesBin(String s){
        return fromFileListMM(s,true);
    } 
    
    public String mm(String s) {
        try {
            bows = new MatrixMarket[1];
            bows[0] = new MatrixMarket(new File(s), false);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String mmBin(String s) {
        try {
            bows = new MatrixMarket[1];
            bows[0] = new MatrixMarket(new File(s), true);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String burnin(String s) {
        try {
            this.burnIn = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    public String iterations(String s) {
        try {
            this.iterations = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    public String thinning(String s) {
        try {
            thinning = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    public String alpha(String s) {
        try {
            initA = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String alphaSD(String s) {
        try {
            this.aSD = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String beta(String s) {
        try {
            initB = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String betaSD(String s) {
        try {
            this.bSD = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String topics(String s) {
        try {
            this.nTopics = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String threads(String s) {
        try {
            nThreads = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    @Override
    public String noOption(String s) {
        return s;
    }

    @Override
    public void usage() {
        System.out.println("\n\nDescription - Heiracrchical Latent Dirichlet Allocation ");
        System.out.println("Finding optimal values for symetric Dirichlet hyperparameters for the standard LDA model");
        System.out.println("\nOptions:");       
        System.out.println("\t-alpha (float)\n\t\t initial value of symmetric Dirichlet parameter for document-topic distribution, default=1.0");
        System.out.println("\t-alphaSD (float)\n\t\t standard deviation for alpha proposal distribution, default=0.1");
        System.out.println("\t-beta (float)\n\t\t initial value of symmetric Dirichlet parameter for topic-word distribution, default=20000.0");
        System.out.println("\t-betaSD (float)\n\t\t standard deviation for beta proposal distribution, default=100.0");
        System.out.println("\t-bow input bag of words file path, no default");
        System.out.println("\t-iterations (integer) number of iterations, default=10000");
        System.out.println("\t-out output file path, no default");        
        System.out.println("\t-threads (integer) number of threads for multiprocessing, default=1");
        System.out.println("\t-topics (integer) the number of topics, default=50");        
    }
    static public void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime starting = LocalDateTime.now();
        System.out.println(dtf.format(starting));
        HeirLDACommandLine lda = new HeirLDACommandLine();
        lda.process(args, true);
        LocalDateTime finished = LocalDateTime.now();
        System.out.printf("Starting %s - Finished %s\n",dtf.format(starting),dtf.format(finished));        
    }
}
