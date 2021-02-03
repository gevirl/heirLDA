/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.hLDA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.rhwlab.lda.UtilsXML;

/**
 *
 * @author gevirl
 */
public class WorkerXML implements Callable {
    File xml;
    
    protected String workID;
    protected int[][] documents; //document data (term lists)
    protected int V; //vocabulary size
    protected int K; //number of topics
    protected double alpha; //Dirichlet parameter (document--topic associations)
    protected double beta; //Dirichlet parameter (topic--term associations)
    protected int z[][]; //topic assignments for each word.
    protected int[][] nw; // nw[i][j] number of instances of word i (term?) assigned to topic j.  V by K (for the documents in this worker)
    protected int[][] nd; // nd[i][j] number of words in document i assigned to topic j.    M by K 
    protected int[] nwsum; // nwsum[j] total number of words assigned to topic j.(for the documents in this worker)
    protected int[] ndsum; // ndsum[i] total number of words in document i.
    protected Random rand;
    protected List<File> iterationFiles = new ArrayList<>();
    protected List<Integer> iterFileSizes = new ArrayList<>();
    protected int cacheSize = 10;
    
    public WorkerXML(){
        
    }
    
    public WorkerXML(File xml)throws Exception {
        this.xml = xml;
    }
    
    @Override
    public Object call() throws Exception {
        System.out.printf("Reading xml file %s\n", xml.getPath());
        File dir = xml.getParentFile();
        
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(xml);
        Element root = doc.getRootElement();
        workID = root.getAttributeValue("workID");
        V = Integer.valueOf(root.getAttributeValue("V"));
        K = Integer.valueOf(root.getAttributeValue("K"));
        cacheSize = Integer.valueOf(root.getAttributeValue("cacheSize"));
        alpha = Double.valueOf(root.getAttributeValue("alpha"));
        beta = Double.valueOf(root.getAttributeValue("beta"));
        documents = UtilsXML.intArray(root.getChild("documents"));
        nw = UtilsXML.intArray(root.getChild("nw"));
        nwsum = UtilsXML.intVector(root.getChild("nwsum"));
        z = UtilsXML.intArray(root.getChild("z"));
        nd = UtilsXML.intArray(root.getChild("nd"));
        ndsum = UtilsXML.intVector(root.getChild("ndsum"));
        String fName = root.getAttributeValue("randFile");
        
        List<Element> eleList = root.getChildren("IterationFile");
        for (Element ele : eleList){
            int index = Integer.valueOf(ele.getAttributeValue("index"));
            String iterName = ele.getAttributeValue("name");
            int nrecs = Integer.valueOf(ele.getAttributeValue("records"));
            iterationFiles.add(index, new File(dir,iterName));
            iterFileSizes.add(index,nrecs);
        }
        
        
        FileInputStream fileIn = new FileInputStream(new File(dir,fName));
        ObjectInputStream in = new ObjectInputStream(fileIn);
        rand = (Random) in.readObject();
        in.close();
        
        fileIn.close(); 
        System.out.printf("Closing xml file %s\n", xml.getPath());
        return documents;
    }    
    public File saveAsXML(File outDir) throws Exception {
        Element ret = new Element("WorkerLDA");
        ret.setAttribute("workID", workID);
        ret.setAttribute("cacheSize", Integer.toString(cacheSize));
        ret.setAttribute("V", Integer.toString(V));
        ret.setAttribute("K", Integer.toString(K));
        ret.setAttribute("D", Integer.toString(documents.length));
        ret.setAttribute("alpha", Double.toString(alpha));
        ret.setAttribute("beta", Double.toString(beta));
        ret.addContent(UtilsXML.asElement("documents", documents));
        ret.addContent(UtilsXML.asElement("nw", nw));
        ret.addContent(UtilsXML.asElement("nwsum", nwsum));
        ret.addContent(UtilsXML.asElement("z", z));
        ret.addContent(UtilsXML.asElement("nd", nd));
        ret.addContent(UtilsXML.asElement("ndsum", ndsum));

        File randFile = new File(outDir,String.format("%s.random",this.workID));
        ret.setAttribute("randFile", randFile.getName());

        FileOutputStream fileOut = new FileOutputStream(randFile);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(rand);
        out.close();
        fileOut.close();

        for (int i=0 ; i<iterationFiles.size() ; ++i){
            Element fileEle = new Element("IterationFile");
            fileEle.setAttribute("index", Integer.toString(i));
            fileEle.setAttribute("name", iterationFiles.get(i).getName());
            fileEle.setAttribute("records", Integer.toString(iterFileSizes.get(i)));           
            ret.addContent(fileEle);
        }
        
        // save the xml
        File xmlFile = new File(outDir, workID + ".xml");
        System.out.printf("Saving xml file: %s\n",xmlFile.getPath());
        OutputStream stream = new FileOutputStream(xmlFile);
        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
        xmlOut.output(ret, stream);
        stream.close();
        return xmlFile;
    }
    
    public Integer getRecordCount(File file){
        for (int i=0 ; i<iterationFiles.size() ; ++i){
            if (iterationFiles.get(i).getPath().equals(file.getPath())){
                return iterFileSizes.get(i);
            }
        }
        return null;
    }
    public int[][] getDocuments(){
        return documents;
    }
    public int getDocumentsSize(){
        return documents.length;
    }
    public int getTopicCount(){
        return K;
    }
    public int getVocabSize(){
        return V;
    }
    public int[][] getDocumentTopicCounts() {
        return this.nd;
    }

    public int[] getDocumentCounts() {
        return this.ndsum;
    }  
    public int[][] getZ(){
        return z;
    }

    public int[][] getWordTopicCounts() {
        return this.nw;
    }

    public int[] getWordCounts() {
        return this.nwsum;
    }    
int iuhsdifuhs=0;

}
