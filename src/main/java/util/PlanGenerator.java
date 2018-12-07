package util;

import com.opencsv.CSVReader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author farzam
 */

public class PlanGenerator {

    public static List<String> plans;

    public static void main(String[] args) throws Exception {
        PlanGenerator planGenerator = new PlanGenerator();

        File folder = new File("plans/");
        File[] listOfFiles = folder.listFiles();

        int j=0;
        for (File file : listOfFiles) {

            if (file.getName().contains(".csv")) {
                plans = new ArrayList<String>();
                planGenerator.readPlans(file.getAbsolutePath());
                planGenerator.addLocalCost();
                planGenerator.writePlans(j);
                j++;
            }
        }
    }

    public void readPlans(String baseAddress) throws Exception {

        String line = "";
        String cvsSplitBy = ",";

        BufferedReader reader = new BufferedReader(new FileReader(baseAddress));
        while((line = reader.readLine()) != null)
        {
            plans.add(line);
        }
    }

    public void addLocalCost(){
        for (int i=0;i<plans.size();i++){
            String plan = plans.get(i);
            int mealOption  = plan.indexOf("1");

            String planCost;
            if (mealOption >= 0 && mealOption <= 71)
            {
                //polyMensa, garden
                planCost = "6.20:";
                plan = planCost + plan;
            }
            if (mealOption >= 72 && mealOption <= 143)
            {
                //polyMensa, home
                planCost = "6.20:";
                plan = planCost + plan;
            }
            if (mealOption >= 144 && mealOption <= 215)
            {
                //polyMensa, street
                planCost = "9.90:";
                plan = planCost + plan;
            }
            if (mealOption >= 216 && mealOption <= 287)
            {
                //polyMensa, local
                planCost = "11.50:";
                plan = planCost + plan;
            }
            if (mealOption >= 288 && mealOption <= 359)
            {
                //DGESS, salad
                planCost = "7.50:";
                plan = planCost + plan;
            }
            if (mealOption >= 360 && mealOption <= 431)
            {
                //DGESS, traditional
                planCost = "9.00:";
                plan = planCost + plan;
            }
            if (mealOption >= 432 && mealOption <= 503)
            {
                //DGESS, vegetarian
                planCost = "9.00:";
                plan = planCost + plan;
            }
            if (mealOption >= 503 && mealOption <= 575)
            {
                //DGESS, quiche
                planCost = "4.00:";
                plan = planCost + plan;
            }
            plans.set(i,plan);
        }
    }

    public void writePlans(int j) throws IOException {
        Path file = Paths.get("datasets/mensa/agent_"+j+".plans");
        Files.write(file, plans, Charset.forName("UTF-8"));
    }

}
