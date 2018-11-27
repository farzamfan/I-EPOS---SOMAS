package agent.planselection;

/**
 * PlanGenerator selection function uses various constants, variables and functions.
 * These enumeration types are used only as keys to hashmap when passing all arguments
 * to optimization function
 * 
 * @author jovan
 *
 */
public enum OptimizationFactor {
	LAMBDA,
	GLOBAL_COST,
	LOCAL_COST,
	STD,
	MEAN,
	ALPHA,
	BETA,
	W_M,
	W_P,
	W_T,
	W_I,
	DISCOMFORT_SUM,
	DISCOMFORT_SUM_SQR,
	NUM_AGENTS,
	INCENTIVE_SIGNAL
}
