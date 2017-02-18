package com.fiskkit.tos.darkbot;

import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.math3.special.Beta;

import com.fiskkit.tos.darkbot.models.Tag;
import com.fiskkit.tos.darkbot.models.TagParam;
import com.fiskkit.tos.darkbot.network.MYSQLAccess;

public class TestOfSignificance { // NB This used to be Main

//    
   private static final Logger LOGGER = Logger.getLogger("");
	

	
   public static void runTestOfSignificance(){
       Deque<Integer> prioritylist;
       Deque<Tag> klist;
       Map<Integer, TagParam> TagParamsList;
       int n;
       double a, b;
       Tag tempTag;
       TagParam tempTagParams;
       TagParamsList = MYSQLAccess.getTagParameters();
       HashSet<String> articleIDSet = new HashSet<String>();
      
       
       
       // use priority list to determine which articles are of interest
       // then process every sentence id in those articles
       prioritylist = MYSQLAccess.getPriorityList();
       
       for (int ID: prioritylist){
    	   articleIDSet.add(MYSQLAccess.getArticleID(ID));
       }
       
       // use article IDs to get ordered list of sentence ids
       List<Integer> sentenceIDList = MYSQLAccess.getSentenceIDList(articleIDSet);
       
       
       MYSQLAccess.initTagIDMap();
       
       String nValuePad = "";
       while (nValuePad.length() < "Overly Simplistic".length() + 2) {
    	   nValuePad = " " + nValuePad;
		}


       // use System.out to avoid formatting
       System.out.println("       Date: " + new Date());
       System.out.println("------------------------------");
       for (int sentenceID: sentenceIDList){     
           
           String[] titleAndBody = MYSQLAccess.getTitleAndBody(sentenceID, articleIDSet);
           String title = titleAndBody[0];
           String body = titleAndBody[1];
           //System.out.println("Sentence ID: " + sentenceID);
           System.out.println("------------------------------");
           System.out.println("    Article: " + title);
           System.out.println("       Text: " + body);
           
           
           
           n = MYSQLAccess.getN(sentenceID);
           
           System.out.println(nValuePad + " | N-Value: " + n);
           klist = MYSQLAccess.getKList(sentenceID);

           while (!klist.isEmpty()) {
               double MU, NU, P, Z;
               int currentTagID, k;
               tempTag = klist.pop(); //Get Tag object from the top of the stack
               currentTagID = tempTag.getId();//
               tempTagParams = TagParamsList.get(currentTagID);
               k = tempTag.getCount();

               MU = tempTagParams.mu;
               NU = tempTagParams.nu;
               P = tempTagParams.p;
               Z = tempTagParams.z;

               String tagName = MYSQLAccess.getTagName(currentTagID);
               System.out.println(tagName + " | K-Value: " + k);
               a = MU * NU;
               b = (1 - MU) * NU;
               if (applyTag(a, b, Z, P, n, k)) {
                   MYSQLAccess.applyTag(sentenceID, currentTagID);
               }

           }
           System.out.println(""); // Space between sentence data

       }
       /* for(Map.Entry<Integer, TagParam> entry : TagParamsList.entrySet()){
            LOGGER.info(entry.getKey().toString() + " - " + entry.getValue().mu);
        }*/
       
       
       
       
       
       MYSQLAccess.closeConnection();

   }

    public static boolean applyTag(double a, double b, double z, double p, int n, int k) {
        double rBeta = Beta.regularizedBeta(z, a + k, b + n - k);

        if ((1 - rBeta) > p) {
            return true;
        }

        return false;

    }

}
