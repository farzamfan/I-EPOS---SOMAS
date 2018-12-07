package agent.planselection;

import java.util.HashMap;
import java.util.Spliterator;

import agent.Optimization;
import org.apache.commons.math3.exception.NotANumberException;


/**
 * Contains collection of PlanGenerator Selection Optimization Functions
 * 
 * @author farzam
 *
 */
public class PlanSelectionOptimizationFunctionCollection {
	
	/**
	 * cost + lambda * i/|P| * std
	 */
	public static PlanSelectionOptimizationFunction simpleFunction1 = (HashMap<OptimizationFactor, Object> map) -> {
		double lambda 	= 	(double)	map.get(OptimizationFactor.LAMBDA);
		double cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double score	=	(double)	map.get(OptimizationFactor.LOCAL_COST);
		double std		=	(double)	map.get(OptimizationFactor.STD);		
		return cost + lambda * score * std;
	};
	
	/**
	 * (1 - lambda) * cost + lambda * i/|P| * std
	 */
	public static PlanSelectionOptimizationFunction simpleFunction2 = (HashMap<OptimizationFactor, Object> map) -> {
		double lambda 	= 	(double)	map.get(OptimizationFactor.LAMBDA);
		double cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double score	=	(double)	map.get(OptimizationFactor.LOCAL_COST);
		double std		=	(double)	map.get(OptimizationFactor.STD);		
		return (1 - lambda) * cost + lambda * score * std;
	};
	
	/**
	 * (1 - lambda) * cost + lambda * i/|P| * mean
	 */
	public static PlanSelectionOptimizationFunction simpleFunction3 = (HashMap<OptimizationFactor, Object> map) -> {
		double lambda 	= 	(double)	map.get(OptimizationFactor.LAMBDA);
		double cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double score	=	(double)	map.get(OptimizationFactor.LOCAL_COST);
		double mean		=	(double)	map.get(OptimizationFactor.MEAN);		
		return (1 - lambda) * cost + lambda * score * mean;
	};
	
	/**
	 * (1 - lambda) * cost + lambda * i/|P|
	 */
	public static PlanSelectionOptimizationFunction simpleFunction4 = (HashMap<OptimizationFactor, Object> map) -> {
		double lambda 	= 	(double)	map.get(OptimizationFactor.LAMBDA);
		double cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double score	=	(double)	map.get(OptimizationFactor.LOCAL_COST);	
		return (1 - lambda) * cost + lambda * score;
	};
	
	
	public static double unfairness(double discomfortSum, double discomfortSumSqr, double numAgents) {
		double sumOfSquares 	=	discomfortSumSqr/numAgents;
		double squaredSum		=	Math.pow(discomfortSum/numAgents, 2);
		// note that sometimes sumOfSquares can be lower than squaredSum
		double diff = sumOfSquares - squaredSum;
//		System.out.println("discomfortSum = " + discomfortSum + ", discomfortSumSqr = " + discomfortSumSqr);
//		System.out.println("sumOfSquares = " + sumOfSquares + ", squaredSum = " + squaredSum + ", diff = " + diff);
		if(Math.abs(diff) < 1e-9) {
			// some numerical instabilities at 1e-16 can lead to negative number in square root
			diff = 0;
		}
		double discomfortStd 	=	Math.sqrt(diff);
		return discomfortStd;
	}
	
	
	public static double localCost(double discomfortSum, double numAgents) {
		return discomfortSum/numAgents;
	}
	
	
	public static PlanSelectionOptimizationFunction complexFunction1 = (HashMap<OptimizationFactor, Object> map) -> {
		double alpha			=	(double)	map.get(OptimizationFactor.ALPHA);
		double beta				=	(double)	map.get(OptimizationFactor.BETA);
		double discomfortSum	=	(double)	map.get(OptimizationFactor.DISCOMFORT_SUM);
		double discomfortSumSqr =	(double)	map.get(OptimizationFactor.DISCOMFORT_SUM_SQR);
		double global_cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double numAgents		=	(double)	map.get(OptimizationFactor.NUM_AGENTS);
		
		double local_cost		=	localCost(discomfortSum, numAgents);
		double unfairness		=	unfairness(discomfortSum, discomfortSumSqr, numAgents); 
		
		if(Double.isNaN(unfairness)) {
			throw new NotANumberException();
		}
//		System.out.println("discomfortSum = " + discomfortSum + ", discomfortSumSqr = " + discomfortSumSqr);
//		System.out.println("global cost = " + global_cost + ", unfairness = " + unfairness + ", local cost = " + local_cost + ", num agents = " + numAgents);
//		System.out.println("alpha = " + alpha + ", beta = " + beta + ", returning: " + ((1 - alpha - beta) * global_cost + alpha * unfairness + beta * local_cost));
		return (1 - alpha - beta) * global_cost + alpha * unfairness + beta * local_cost;
	};

	public static double incentivizedLocalCost(double localcost, double incentiveSignal, double w_m, double w_p, double w_t, double w_i, double pref, double queue)
	{
//        System.out.println("LC:"+localcost+" IS:"+incentiveSignal+" pref:"+pref+" w_m,w_t,w_p,w_t,w_i:"+w_m+" "+w_t+" "+w_p+" "+w_i);
//        System.out.println(w_m * (localcost - w_i*incentiveSignal*localcost)/(2*localcost) - w_p * (pref) + w_t * (0));
//        System.out.println("--");
	    return w_m * (localcost - w_i*incentiveSignal*localcost)/2*localcost - w_p * (pref) + w_t * (queue);
	}

	public static PlanSelectionOptimizationFunction incentiveFunction = (HashMap<OptimizationFactor, Object> map) -> {
		double alpha			=	(double)	map.get(OptimizationFactor.ALPHA);
		double beta				=	(double)	map.get(OptimizationFactor.BETA);
		double w_m              =   (double)    map.get(OptimizationFactor.W_M);
        double w_p              =   (double)    map.get(OptimizationFactor.W_P);
        double w_t              =   (double)    map.get(OptimizationFactor.W_T);
        double w_i              =   (double)    map.get(OptimizationFactor.W_I);
		double discomfortSum	=	(double)	map.get(OptimizationFactor.DISCOMFORT_SUM);
		double discomfortSumSqr =	(double)	map.get(OptimizationFactor.DISCOMFORT_SUM_SQR);
		double localCost		= 	(double)	map.get(OptimizationFactor.LOCAL_COST);
		double pref				= 	(double)	map.get(OptimizationFactor.PREFERENCE);
		double incentiveSignal	=	(double)	map.get(OptimizationFactor.INCENTIVE_SIGNAL);
		double global_cost		=	(double)	map.get(OptimizationFactor.GLOBAL_COST);
		double numAgents		=	(double)	map.get(OptimizationFactor.NUM_AGENTS);
		double queue            =   (double)    map.get(OptimizationFactor.QUEUE);

		double local_cost		=	incentivizedLocalCost(localCost, incentiveSignal, w_m, w_p, w_t, w_i, pref, queue);
		double unfairness		=	unfairness(discomfortSum, discomfortSumSqr, numAgents);

		if(Double.isNaN(unfairness)) {
			throw new NotANumberException();
		}
		return (1 - alpha - beta) * global_cost + alpha * unfairness + beta * local_cost;
	};

}
