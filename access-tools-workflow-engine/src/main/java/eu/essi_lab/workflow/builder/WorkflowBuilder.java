package eu.essi_lab.workflow.builder;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.workflow.processor.DescriptorUtils;
import eu.essi_lab.workflow.processor.IdentityProcessor;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.SubsettingCapability;

/**
 * Builds a set of available {@link Workflow}s of minimum length from a supplied
 * input {@link DataDescriptor} to a supplied target {@link DataDescriptor}
 *
 * @author Fabrizio
 */
public class WorkflowBuilder {

	private static boolean logsEnabled = false;
	private static boolean deepLogsEnabled = false;

	private LinkedList<Workblock> blockList;
	private int maxWorkflowLength;

	public WorkflowBuilder() {

		blockList = new LinkedList<>();
		setMaxWorkflowLength(10);
	}

	/**
	 * Creates an instance of loaded with the blocks of all the available
	 * {@link Workblock}s
	 */
	public static WorkflowBuilder createLoadedBuilder() {

		WorkflowBuilder builder = new WorkflowBuilder();

		ServiceLoader<WorkblockBuilder> loader = ServiceLoader.load(WorkblockBuilder.class);
		Iterator<WorkblockBuilder> iterator = loader.iterator();
		while (iterator.hasNext()) {
			WorkblockBuilder b = iterator.next();
			try {
				builder.add(b);
			} catch (Exception e) {

				GSLoggerFactory.getLogger(WorkblockBuilder.class).error(e.getMessage(), e);
			}
		}

		return builder;
	}

	public static void enableLogs(boolean enable) {

		logsEnabled = enable;
	}

	public static void enableDeepLogs(boolean enable) {

		deepLogsEnabled = enable;
	}

	/**
	 * Empty the blocks list
	 */
	public void clear() {

		blockList = new LinkedList<>();
	}

	/**
	 * Adds the supplied block to the blocks list
	 *
	 * @param block
	 * @see #clear()
	 * @see #create(InputDescriptor, OutputDescriptor)
	 * @see #buildPreferred(InputDescriptor, OutputDescriptor)
	 */
	public void add(Workblock block) {

		add(Arrays.asList(block));
	}

	/**
	 * Adds the supplied blocks to the blocks list
	 *
	 * @param blocks
	 * @see #clear()
	 * @see #create(InputDescriptor, OutputDescriptor)
	 * @see #buildPreferred(InputDescriptor, OutputDescriptor)
	 */
	public void add(List<Workblock> blocks) {

		blockList.addAll(blocks);
	}

	/**
	 * @param builder
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @see #clear()
	 * @see #create(InputDescriptor, OutputDescriptor)
	 * @see #buildPreferred(InputDescriptor, OutputDescriptor)
	 */
	public void add(WorkblockBuilder builder) {

		add(builder.build(Workblock.MINIMUM_PREFERENCE));
	}

	/**
	 * @param builder
	 * @param preference
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @see #clear()
	 * @see #create(InputDescriptor, OutputDescriptor)
	 * @see #buildPreferred(InputDescriptor, OutputDescriptor)
	 */
	public void add(WorkblockBuilder builder, int preference) {

		add(builder.build());
	}

	/**
	 * Get the maximum allowed length of the workflows (default: 10)
	 *
	 * @return
	 */
	public int getMaxWorkflowLength() {

		return maxWorkflowLength;
	}

	/**
	 * Set the maximum allowed length of the workflows (default: 10)
	 *
	 * @param length
	 */
	public void setMaxWorkflowLength(int length) {

		this.maxWorkflowLength = length;
	}

	/**
	 * Builds the preferred workflow available which start from the supplied
	 * <code>inputDescriptor</code> and arrive to the supplied
	 * <code>outputDescriptor</code>. The returned {@link Optional} is empty if:
	 * <ul>
	 * <li>there is no initial {@link ProcessorCapabilities} which suitable for the
	 * supplied <code>inputDescriptor</code></li>
	 * <li>there is no terminal {@link ProcessorCapabilities} which suitable for the
	 * supplied <code>outputDescriptor</code></li>
	 * <li>there is no path which starting from the initial
	 * {@link ProcessorCapabilities}/s arrives to the terminal
	 * {@link ProcessorCapabilities}/s</li>
	 * </ul>
	 * The preference is given according to the preferences of the all the workflows
	 * built by the {@link #create(InputDescriptor, OutputDescriptor)} method<br>
	 *
	 * @param initDescriptor
	 * @param targetDescriptor
	 * @return
	 * @see Workflow#getPreference()
	 * @see #create(InputDescriptor, OutputDescriptor)
	 */
	public Optional<Workflow> buildPreferred(DataDescriptor initDescriptor, DataDescriptor targetDescriptor) {
		return buildPreferred(null, initDescriptor, targetDescriptor);
	}

	public Optional<Workflow> buildPreferred(GSResource resource, DataDescriptor initDescriptor,
			DataDescriptor targetDescriptor) {

		List<Workflow> workflows = build(resource, initDescriptor, targetDescriptor);

		for (Workflow workflow : workflows) {
			Workblock block0 = workflow.getWorkblocks().get(0);
			SubsettingCapability subCap = block0.getOutput().getSubsettingCapability();
			if (subCap != null && subCap.getTemporalSubsetting().getFirstValue().equals(Boolean.TRUE)) {
				return Optional.of(workflow);
			}

		}

		return workflows.stream().//
				max((w1, w2) -> Integer.compare(w1.getPreference(), w2.getPreference()));
	}

	/**
	 * Builds the available workflows which start from the supplied
	 * <code>inputDescriptor</code> and arrive to the supplied
	 * <code>outputDescriptor</code>. The returned list is empty if:
	 * <ul>
	 * <li>there is no initial {@link ProcessorCapabilities} suitable for the
	 * supplied <code>inputDescriptor</code></li>
	 * <li>there is no terminal {@link ProcessorCapabilities} suitable for the
	 * supplied <code>outputDescriptor</code></li>
	 * <li>there is no path which starting from the initial
	 * {@link ProcessorCapabilities}/s arrives to the terminal
	 * {@link ProcessorCapabilities}/s</li>
	 * <li>there are paths which starting from the initial
	 * {@link ProcessorCapabilities}/s and arrive to the terminal
	 * {@link ProcessorCapabilities}/s, but the length is greater than
	 * {@link #getMaxWorkflowLength()}</li>
	 * </ul>
	 * <h3>Building stages</h3> The workflows building operates on 5 stages:<br>
	 * <br>
	 * <ol>
	 * <li>Initial and terminal block detection</li> In this stage the blocks
	 * suitable with the supplied supplied <code>inputDescriptor</code> and
	 * <code>outputDescriptor</code> are marked as <i>initial</i> and/or
	 * <i>terminal</i>. Each initial block represents the root of the correspondent
	 * tree, and each terminal block a leaf (see 3.)
	 * <li>Graph creation</li> In this stage a cartesian power of second degree
	 * (excluding the identical couples) is applied on the builder blocks. Distinct
	 * couples of blocks are linked according to their input and output
	 * <li>Trees traversal</li> The graph is split by selecting a sub-graph for each
	 * initial block, thus creating trees with a root (the initial block) and one or
	 * more leafs (terminal blocks). Each tree is traversed using the "breadth first
	 * traversal" algorithm in order to create list of adjacentes for the different
	 * level of the tree. During this stage, after reaching a terminal block at
	 * level l, the search continues until the level l+1 is reached in order to
	 * reach all the remaining (if any) terminal blocks of level l, thus discard
	 * longer workflows
	 * <li>Workflows building</li> In this stage, the list of adjacentes are
	 * traversed from the list of terminal level tl containing one or more terminal
	 * blocks, until the list of level 0 containing the root block. Each walk from a
	 * terminal block to the root generates a workflow which is then checked; only
	 * valid workflows are maintained
	 * <li>Shortest workflows selection</li> At the end of the previous stage, there
	 * can be several workflows with different lengths generated by different trees
	 * (each tree generates workflows of the same length). In this stage only the
	 * shortest are selected and returned
	 * </ol>
	 * <h3>Number of buildable workflows</h3> If not empty, the returned workflows
	 * list contains workflows of the same, minimum length. The workflows length is
	 * always less than the {@link #getMaxWorkflowLength()}.<br>
	 * <br>
	 * The exact number of buildable workflows is given by the sum of all the
	 * distinct couples (Initial Block, Terminal Block), selecting only:
	 * <ol>
	 * <li>connected couples</li>
	 * <li>one <i>preferred</i> workflow for each connected couple (*)</li>
	 * <li>workflows with the shortest length</li>
	 * <li>valid workflows</li>
	 * </ol>
	 * As consequence of (2), built workflows having the same IB (that is owning to
	 * the same tree), have always different TB
	 * <h5>Examples</h5><br>
	 * Given the triples (Initial Block, Terminal Block, Workflow Length) where each
	 * couple (IB, TB) is connected<br>
	 * <b>Example 1</b>:<br>
	 * <ul>
	 * <li>Triples: (0,3,7),(0,5,7) - (1,3,7),(1,5,7)</li>
	 * <li>4 distinct couples. Since all the workflows have the same length 7, all
	 * the 4 workflows are built
	 * </ul>
	 * <b>Example 2</b>:<br>
	 * <ul>
	 * <li>Triples: (0,3,5),(0,5,5) - (1,3,7),(1,5,7)</li>
	 * <li>4 distinct couples. Since there are 2 workflows of minimum length 5, 2
	 * workflows are built and the others are discarded
	 * </ul>
	 * <b>Example 3</b>:<br>
	 * <ul>
	 * <li>Triples: (0,3,5),(0,5,7) - (1,3,7),(1,5,7)</li>
	 * <li>4 distinct couples. Since there is a workflow with minimum length 5, one
	 * workflow is built and the others are discarded
	 * </ul>
	 * <b>Example 4</b>:<br>
	 * <ul>
	 * <li>Triples: (0,3,5),(0,3,5),(0,3,5)</li>
	 * <li>1 distinct couple. The same couple (0,3) is connected by 3 different
	 * paths of the same length. The sole workflow is selected according to the
	 * <i>preference system</i>
	 * </ul>
	 * <h4>The preference system</h4> The preference system works in two
	 * circumstances (not mutually exclusive)<br>
	 * <br>
	 * <ol>
	 * <li>(*) Multiple predecessors</li> A couple (IB, TB) is connected by multiple
	 * paths of same length. Considering that the workflow building algoritm walks
	 * paths from TB to IB, this happens when a block has more than one direct
	 * predecessor. In this case, the <i>preferred</i> block is selected. The
	 * preferred block is the block having the best "history preference". The
	 * history of a block is given by all its predecessors, directed and non, until
	 * the initial block (excluded). The history preference of a block is given by
	 * the sum of the {@link Workblock#getPreference()} of its history blocks having
	 * a preference set (that is a preference greater than
	 * {@link Workblock#MINIMUM_PREFERENCE}. The algorithm threats "brothers" blocks
	 * (that is blocks sharing the same father/direct successor block) as a single
	 * block having as preference the maximum preference of all the brothers<br>
	 * <br>
	 * <li>Multiple workflows of same minimum length</li> At the end of the
	 * workflows building stage, there can be several workflows of the same minimum
	 * length. This can happen if there are multiple connected couples (IB,TB). In
	 * this case the preference system is applied by the method
	 * {@link #buildPreferred(InputDescriptor, OutputDescriptor)} which takes
	 * advantage of the {@link Workflow#getPreference()} method
	 * </ol>
	 *
	 * @param initDescriptor
	 * @param targetDescriptor
	 * @return
	 */
	public List<Workflow> build(DataDescriptor initDescriptor, DataDescriptor targetDescriptor) {
		return build(null, initDescriptor, targetDescriptor);
	}

	public List<Workflow> build(GSResource resource, DataDescriptor initDescriptor, DataDescriptor targetDescriptor) {
		GSLoggerFactory.getLogger(getClass()).info("Building workflow");
		printLog("Building workflow STARTED");
		ProcessorCapabilities input = DescriptorUtils.fromInputDescriptor(initDescriptor);

		ProcessorCapabilities output = DescriptorUtils.fromTargetDescriptor(initDescriptor, targetDescriptor);

		printLog("Input capabilities: " + input);
		printLog("Output capabilities: " + output);

		// fully resets all the blocks
		blockList.forEach(v ->

		{
			v.fullReset();
		});

		printLog("Available blocks count: " + this.blockList.size());
		if (deepLogsEnabled) {
			for (Workblock workblock : blockList) {
				GSLoggerFactory.getLogger(getClass()).info(workblock.toString());
			}
		}

		ArrayList<Workflow> out = new ArrayList<>();

		// 0-blocks use case!
		if (output.accept(input)) {
			printLog("Warning: identity workflow selected");
			Workflow workflow = new Workflow();
			Workblock workBlock = new Workblock(new IdentityProcessor(input, output), null);
			workflow.getWorkblocks().add(workBlock);
			out.add(workflow);
			return out;
		}

		int terminals = countTerminals(output);

		// System.out.println("TERMINALS: " + terminals);

		if (terminals == 0) {

			printLog("No terminal blocks found, exit");
			return out;
		}

		List<Workblock> initials = findInitials(input);
		if (initials.isEmpty()) {

			printLog("No initials blocks found, exit");
			return out;
		}

		findTerminals(output);

		printLog("Found " + initials.size() + " initial blocks");

		if (deepLogsEnabled) {
			GSLoggerFactory.getLogger(getClass()).info("Initials (count: {})", initials.size());
			for (Workblock workblock : initials) {
				GSLoggerFactory.getLogger(getClass()).info("WB: " + workblock.toString());
			}
		}

		printLog("Creating graph STARTED");

		createGraph();

		printLog("Creating graph ENDED");

		printLog("Workflows finding STARTED");

		initials.forEach(initBlock -> {

			// fully resets all the blocks
			blockList.forEach(v -> {
				v.partialReset();
			});

			if (deepLogsEnabled) {

				GSLoggerFactory.getLogger(getClass()).info("Current initial block: {}", initBlock);
			}

			findWorkflows(//

					initDescriptor, //
					targetDescriptor,

					initBlock, //
					output, //
					out);
		});

		printLog("Workflows finding ENDED");

		printLog("Total workflows count: " + out.size());

		Optional<Integer> minWorkflowLength = out.stream().//
				map(w -> w.getWorkblocks().size()).//
				min((w1, w2) -> Integer.compare(w1, w2));

		if (minWorkflowLength.isPresent()) {

			printLog("Minimum workflow length: " + minWorkflowLength.get());

			// keeps only the shortest workflows
			List<Workflow> minLengthList = out.stream().//
					filter(w -> w.getWorkblocks().size() == minWorkflowLength.get()).//
					collect(Collectors.toList());

			printLog("Total workflows of minimum length count: " + minLengthList.size());

			printLog("Building ENDED");

			return minLengthList;
		}

		return out;
	}

	/**
	 * @return
	 */
	public List<Workblock> getBlocks() {

		return blockList;
	}

	/**
	 * https://www.geeksforgeeks.org/breadth-first-traversal-for-a-graph/
	 *
	 * @param outputDescriptor
	 * @param inputDescriptor
	 * @param block
	 * @param target
	 * @param out
	 * @param out
	 * @return
	 */
	private void findWorkflows(//
			DataDescriptor inputDescriptor, //
			DataDescriptor outputDescriptor, //
			Workblock block, //
			ProcessorCapabilities target, //
			ArrayList<Workflow> out) {

		block.setVisited(true);

		LinkedList<Workblock> queue = new LinkedList<>();
		queue.add(block);

		// the current level
		int level = -1;

		// // the current terminal level
		// int terminalLevel = Integer.MAX_VALUE;

		// map of adjacencies list
		HashMap<Integer, LinkedList<Workblock>> adjMap = new HashMap<>();

		while (!queue.isEmpty()) {

			block = queue.poll();

			// System.out.println("\n --- \n");
			// System.out.println("Current block: " + block.getId());
			// System.out.println("After poll: " + queue.stream().map(b ->
			// b.getId()).collect(Collectors.toList()));

			// -------------------------------
			//
			// new level, new adjacencies list
			//
			if (level != block.getLevel()) {

				level = block.getLevel();

				// --------------------------------------------------------------
				//
				// checking workflows.
				// if at least one checked workflow exists, the search ends here,
				// otherwise we go further to the current level
				//
				if (!adjMap.isEmpty()) {

					printLog("Level " + level + " traversing completed, checking workflows");

					List<Workflow> workflows = findWorkflows(inputDescriptor, outputDescriptor, adjMap);

					if (!workflows.isEmpty()) {

						printLog("Found " + workflows.size() + " checked workflows, exit");

						out.addAll(workflows);

						return;
					}
				}

				adjMap.put(level, new LinkedList<Workblock>());

				if (deepLogsEnabled) {

					GSLoggerFactory.getLogger(getClass()).info("Traversing level: {}", level);
				}
			}

			// ------------------------------------
			//
			// maximum workflow level reached, exit
			//
			if (level > maxWorkflowLength - 1) {
				// e.g: max workflow length is 5, so maximum
				// allowed level is 4
				printLog("Maximum workflow level reached, search interrupted. Checking workflows ");

				break;
			}

			// adds the current block in the proper adj list
			if (!adjMap.get(level).contains(block)) {
				adjMap.get(level).add(block);
				block.setAdjacent();
			}

			// ----------------------------------------------
			//
			// visit the successors of the last polled block
			//

			Set<Workblock> successors = block.getSuccessors();

			for (Workblock b : successors) {

				// ----------------------------------
				//
				// multi-level condition
				//
				// if the block b is already visited but is not yet
				// inserted in the map at its current level,
				// and if it has the same level of its predecessor,
				// it is forced to be
				// inserted in the map at its current level,
				// than incrementing its level is incremented.
				// this way the same block can be present
				// in the map at different levels
				if (b.isVisited()) {

					if (!adjMap.get(block.getLevel()).contains(b) &&
					// same predecessor level
							block.getLevel() == b.getLevel()) {

						adjMap.get(block.getLevel()).add(b);
						b.setAdjacent();
					}
				}

				b.setLevel(block.getLevel() + 1);
				b.setVisited(true);

				if (!b.isDeadEnd()) {
					//
					// the queue can already contains the block, but
					// now the level is incremented so it must replaced with
					//
					queue.remove(b);
					queue.add(b);
				}
			}

			// System.out.println("After add: " + queue.stream().map(b ->
			// b.getId()).collect(Collectors.toList()));
		}

		List<Workflow> workflows = findWorkflows(inputDescriptor, outputDescriptor, adjMap);
		printLog("Found " + workflows.size() + " checked workflows, exit");

		out.addAll(workflows);
	}

	private List<Workflow> findWorkflows(//
			DataDescriptor inputDescriptor, //
			DataDescriptor outputDescriptor, //
			HashMap<Integer, LinkedList<Workblock>> adjMap) {

		List<Workflow> out = new ArrayList<>();

		// -------------------------
		//
		// final step: paths finding
		//
		int level = adjMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();

		LinkedList<Workblock> termAdjList = adjMap.get(level);

		// get the terminals in the adj list
		List<Workblock> terminals = termAdjList.stream().//
				filter(v -> v.isTerminal()).//
				collect(Collectors.toList());

		if (terminals.isEmpty()) {

			printLog("No terminals on level " + level + " found");

		} else {

			for (int i = 0; i < terminals.size(); i++) {

				// a workflow for each terminal block
				Workflow workflow = new Workflow();

				// current terminal block
				Workblock terminal = terminals.get(i);

				if (deepLogsEnabled) {

					GSLoggerFactory.getLogger(getClass()).info("Current terminal block: {}", terminal);
				}

				// adds the terminal to the workflow
				workflow.getWorkblocks().add(terminal);

				Workblock current = terminal;

				// building path from terminal level -1 to level 0
				for (int j = level - 1; j >= 0; j--) {

					// get the adjacencies list of level j
					LinkedList<Workblock> adjList = adjMap.get(j);

					// get the first predecessor of the current block in the adj list
					Optional<Workblock> predecessor = findPredecessor(current, adjList);

					if (!predecessor.isPresent()) {
						return out;
					}

					workflow.getWorkblocks().add(predecessor.get());

					current = predecessor.get();
				}

				// reversing workflow blocks, from initial to terminal
				Collections.reverse(workflow.getWorkblocks());

				// only checked workflows
				if (workflow.check(inputDescriptor, outputDescriptor)) {

					out.add(workflow);
					printLog("Adding checked workflow");

				} else {

					printLog("Discarding invalid workflow");
				}
			}
		}

		return out;
	}

	/**
	 * Finds a predecessor (if any) of the supplied block contained in the adj list,
	 * according to the block with the best history preference. If a block with a a
	 * best history preference do not exists, returns a random block
	 *
	 * @param block
	 * @param adjList
	 * @param allAdjMapBlocks
	 * @return
	 */
	private Optional<Workblock> findPredecessor(Workblock block, List<Workblock> adjList) {

		// block direct predecessors contained in the adj list
		List<Workblock> predInAdjList = adjList.stream().//
				filter(b -> block.getPredecessors().contains(b)).//
				collect(Collectors.toList());

		return predInAdjList.stream().//

				max((b1, b2) -> Integer.compare(//
						b1.getHistoryPreference(), //
						b2.getHistoryPreference()));

	}

	private List<Workblock> findInitials(ProcessorCapabilities input) {

		return blockList.stream().//
				filter(v -> v.getInput().accept(input)).//
				peek(Workblock::setInitial).//
				collect(Collectors.toList());//

	}

	private void findTerminals(ProcessorCapabilities terminal) {

		blockList.stream().//
				filter(v -> terminal.accept(v.getOutput())).//
				forEach(Workblock::setTerminal);
	}

	private int countTerminals(ProcessorCapabilities terminal) {

		if (deepLogsEnabled) {

			GSLoggerFactory.getLogger(getClass()).info("Searching terminals for: " + terminal);
		}

		List<Workblock> terminals = blockList.stream().//
				peek(v -> {
					if (deepLogsEnabled) {

						GSLoggerFactory.getLogger(getClass()).info("Checking : " + v.getOutput());
					}

				}).filter(v -> terminal.accept(v.getOutput())).//

				collect(Collectors.toList());

		long count = terminals.size();

		if (deepLogsEnabled) {

			List<Workblock> list = blockList.stream().//
					peek(v -> {
						if (deepLogsEnabled) {

							GSLoggerFactory.getLogger(getClass())
									.info("Checking: " + v.getClass().getName() + " " + v.getOutput());
						}

					}).filter(v -> terminal.accept(v.getOutput())).//

					collect(Collectors.toList());

			GSLoggerFactory.getLogger(getClass()).info("Found {} terminals", count);
			for (Workblock workblock : terminals) {
				System.out.println(workblock.toString());
			}
		}

		return (int) count;
	}

	private void createGraph() {

		for (Workblock b1 : blockList) {
			for (Workblock b2 : blockList) {

				if (!b1.equals(b2) && //

						b2.getInput().accept(b1.getOutput()) //

				// !b1.getInput().accept(b2.getOutput()) && //

				// !v1.isTerminal() //

				// !b2.isInitial() //
				) {
					if (deepLogsEnabled) {

						System.out.println("*** ACCEPTED  ***");
						System.out.println("---  FROM --- ");
						System.out.println("- IN  " + b1.getInput());
						System.out.println("- OUT  " + b1.getOutput());
						System.out.println("---  TO --- ");
						System.out.println("- IN  " + b2.getInput());
						System.out.println("- OUT  " + b2.getOutput());
						System.out.println("***");
					}

					b1.addSuccessor(b2);
					b2.addPredecessor(b1);

				} else if (!b1.equals(b2)) {

					if (deepLogsEnabled) {
						System.out.println("*** REJECTED  ***");
						System.out.println("---  FROM --- ");
						System.out.println("- IN  " + b1.getInput());
						System.out.println("- OUT  " + b1.getOutput());
						System.out.println("---  TO --- ");
						System.out.println("- IN  " + b2.getInput());
						System.out.println("- OUT  " + b2.getOutput());
						System.out.println("***");
					}
				}
			}
		}
	}

	private void printLog(String message) {

		if (logsEnabled) {

			GSLoggerFactory.getLogger(getClass()).info(message);
		}
	}
}
