package experiment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import agent.Agent;
import agent.DynamicIncentiveIEPOSAgent;
import agent.ModifiableIeposAgent;
import agent.PlanSelector;
import agent.logging.AgentLogger;
import agent.logging.AgentLoggingProvider;
import agent.logging.LoggingProvider;
import agent.logging.instrumentation.CustomFormatter;
import agent.planselection.DynamicIncentivePlanSelector;
import cern.colt.matrix.tdouble.algo.solver.DoubleGivensRotation;
import config.Configuration;
import data.Plan;
import data.Vector;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;
import treestructure.ModifiableTreeArchitecture;

/**
 * 
 * 
 * @author Jovan N., Thomas Asikis
 *
 */
public class IEPOSExperiment {

	public static void runSimulation(int numChildren, // number of children for each middle node
			int numIterations, // total number of iterations to run for
			int numAgents, // total number of nodes in the network
			Function<Integer, Agent> createAgent, // lambda expression that creates an agent
			Configuration config) {

		SimulatedExperiment experiment = new SimulatedExperiment() {
		};
		ModifiableTreeArchitecture architecture = new ModifiableTreeArchitecture(config);

		SimulatedExperiment.initEnvironment();
		experiment.init();
		PeerFactory peerFactory = new PeerFactory() {
			@Override
			public Peer createPeer(int peerIndex, Experiment e) {
				Agent newAgent = createAgent.apply(peerIndex);
				Peer newPeer = new Peer(peerIndex);

				architecture.addPeerlets(newPeer, newAgent, peerIndex, numAgents);

				return newPeer;
			}
		};

		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(config.loggingLevel);
		for (Handler h : rootLogger.getHandlers()) {
			h.setLevel(config.loggingLevel);
			h.setFormatter(new CustomFormatter());
		}

		experiment.initPeers(0, numAgents, peerFactory);
		experiment.startPeers(0, numAgents);
		experiment.runSimulation(Time.inSeconds(3 + numIterations));
	}

	private static void runOneSimulation(Configuration config, Function<Integer, Agent> createAgent) {
		long timeBefore = System.currentTimeMillis();
		IEPOSExperiment.runSimulation(Configuration.numChildren, Configuration.numIterations,
				Configuration.numAgents, createAgent, config);
		long timeAfter = System.currentTimeMillis();
		System.out.println("IEPOS Finished! It took: " + ((timeAfter - timeBefore) / 1000) + " seconds.");
	}


	public static void main(String[] args) {
		Logger log = Logger.getLogger(IEPOSExperiment.class.getName());

		String confPath = null;

		if (args.length > 0) {
			Path pathURI = Paths.get(args[0]);

			if (!Files.notExists(pathURI) && Files.isRegularFile(pathURI)) {
				log.log(Level.INFO, "Configuration file path provided from command line, "
						+ "overriding default conf location and using file in: \n" + pathURI.toString());
				confPath = pathURI.toString();
			}

		}

		String rootPath = System.getProperty("user.dir");

		confPath = confPath == null ? rootPath + File.separator + "conf" + File.separator + "epos.properties"
				: confPath;


		Configuration config = Configuration.fromFile(confPath);
		config.printConfiguration();


		LoggingProvider<DynamicIncentiveIEPOSAgent<Vector>> loggingProvider = new LoggingProvider<>();
		
		for (AgentLogger logger : config.loggers) {
			loggingProvider.add(logger);
		}

		List<Double> preference = new ArrayList<Double>() {{
			add(1.0);
			add(2.0);
			add(3.0);
			add(4.0);
			add(5.0);
			add(6.0);
			add(7.0);
			add(8.0);
		}};

		Random r = new Random();
		double[] priceWeights = new double[926];
		double[] preferenceWeights = new double[926];
		double[] queueWeights = new double[926];
		for (int p=0;p<926;p++){
			priceWeights[p] = (r.nextGaussian()*2.291173+6.772727)/10;
			preferenceWeights[p] = (r.nextGaussian()*2.041198+7.204545)/10;
			queueWeights[p] = (r.nextGaussian()*2.697093+6.568182)/10;
		}

		for (int sim = 0; sim < Configuration.numSimulations; sim++) {

			System.out.println("Simulation " + (sim + 1));

			final int simulationId = sim;
			config.permutationSeed = sim;

			for (AgentLogger al : loggingProvider.getLoggers()) {
				al.setRun(sim);
			}

			if (Configuration.numSimulations > 1 && sim > 0) {
				Configuration.mapping = config.generateMappingForRepetitiveExperiments.apply(config);
			}

			PlanSelector<DynamicIncentiveIEPOSAgent<Vector>, Vector> planSelector = new DynamicIncentivePlanSelector<Vector>();

			/**
			 * Function that creates an Agent given the id of it's vertex in tree graph.
			 * First type is input argument, second type is type of return value.
			 */
			Function<Integer, Agent> createAgent = agentIdx -> {

				List<Plan<Vector>> possiblePlans = config.getDataset(Configuration.dataset).getPlans(Configuration.mapping.get(agentIdx));
				AgentLoggingProvider<ModifiableIeposAgent<Vector>> agentLP = loggingProvider.getAgentLoggingProvider(agentIdx, simulationId);
				ModifiableIeposAgent<Vector> newAgent = new ModifiableIeposAgent<Vector>(config, possiblePlans, agentLP);

				double[] preferenceArray = new double[8];
				int k =0;
				Collections.shuffle(preference);
				for (Double aDouble : preference) {
					preferenceArray[k] = aDouble;
					k++;
				}

				newAgent.setUnfairnessWeight(Double.parseDouble(config.weights[0]));
				newAgent.setLocalCostWeight(Double.parseDouble(config.weights[1]));
				newAgent.setIncentiveRate(Double.parseDouble(config.weights[2]));

				newAgent.setPriceWeight(priceWeights[agentIdx]);
				newAgent.setPreferenceWeight(preferenceWeights[agentIdx]);
				newAgent.setQueueWeight(queueWeights[agentIdx]);
				newAgent.setPreference(preferenceArray);
				newAgent.setPlanSelector(planSelector);
				return newAgent;

			};

			IEPOSExperiment.runOneSimulation(config, createAgent);
		}

		loggingProvider.print();

	}

}
