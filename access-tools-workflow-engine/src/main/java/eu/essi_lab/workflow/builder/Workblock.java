package eu.essi_lab.workflow.builder;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;

/**
 * Blocks are used by the {@link WorkflowBuilder} to build {@link Workflow}s
 *
 * @author Fabrizio
 */
public class Workblock {

	/**
	 *
	 */
	public static final int MINIMUM_PREFERENCE = 1;

	/**
	 *
	 */
	public static final int MEDIUM_PREFERENCE = 5;

	/**
	 *
	 */
	public static final int MAXIMUM_PREFERENCE = 10;

	private String id;
	private boolean isTerminal;
	private boolean isInitial;
	private LinkedHashSet<Workblock> successors;
	private LinkedHashSet<Workblock> predecessors;
	private boolean visited;
	private DataProcessor process;
	private int level;
	private int preference;
	private boolean isAdjacent;

	private WorkblockBuilder builder;

	/**
	 * Creates a new block with the supplied <code>process</code>,
	 * <code>input</code> and <code>output</code>
	 *
	 * @param process
	 * @param input
	 * @param output
	 */
	public Workblock(DataProcessor process, WorkblockBuilder builder) {

		this(UUID.randomUUID().toString().substring(0, 6), process, builder);
	}

	/**
	 * Creates a new block with the supplied <code>id</code> and
	 * <code>process</code>
	 *
	 * @param id
	 * @param process
	 */
	public Workblock(String id, DataProcessor process, WorkblockBuilder builder) {

		this.id = id;
		this.builder = builder;

		setProcess(process);
		setPreference(MINIMUM_PREFERENCE);

		successors = new LinkedHashSet<>();
		predecessors = new LinkedHashSet<>();
	}

	public WorkblockBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(WorkblockBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Get the preference of this block
	 */
	public int getPreference() {

		return preference;
	}

	/**
	 * Set the preference of this block
	 *
	 * @param preference an integer >= {@link #MINIMUM_PREFERENCE} AND <=
	 *                   {@link #MAXIMUM_PREFERENCE}
	 * @throws IllegalArgumentException
	 */
	public void setPreference(int preference) {

		if (preference < MINIMUM_PREFERENCE || preference > MAXIMUM_PREFERENCE) {

			throw new IllegalArgumentException("Unsupported preference value");
		}

		this.preference = preference;
	}

	/**
	 * An adjacent block is a block inserted in an adjacentes list
	 */
	public boolean isAdjacent() {

		return isAdjacent;
	}

	/**
	 * Set the adjacent state to true
	 */
	public void setAdjacent() {

		isAdjacent = true;
	}

	/**
	 * A deadend block is not terminal and has no successors
	 *
	 * @return
	 */
	public boolean isDeadEnd() {

		return !isTerminal() && getSuccessors().isEmpty();
	}

	/**
	 * Get the level of this block
	 */
	public int getLevel() {

		return level;
	}

	/**
	 * Fully resets this block:
	 * <ul>
	 * <li>set the visited state to false</li>
	 * <li>set the the level to 0</li>
	 * <li>set the terminal state to false</li>
	 * <li>set the initial state to false</li>
	 * <li>empties the successors list</li>
	 * <li>empties the predecessors list</li>
	 * </ul>
	 */
	public void fullReset() {

		partialReset();

		this.isInitial = false;
		this.isTerminal = false;
		successors = new LinkedHashSet<>();
		predecessors = new LinkedHashSet<>();
	}

	/**
	 * Partially reset this block:
	 * <ul>
	 * <li>set the visited state to false</li>
	 * <li>set the the level to 0</li>
	 * </ul>
	 */
	public void partialReset() {

		setVisited(false);
		setLevel(0);
		isAdjacent = false;
	}

	/**
	 * Set the level of this block
	 *
	 * @param level
	 */
	public void setLevel(int level) {

		this.level = level;
	}

	/**
	 * Get the process of this block
	 *
	 * @return
	 */
	public DataProcessor getProcess() {

		return process;
	}

	/**
	 * Set the process of this block
	 *
	 * @param process
	 */
	public void setProcess(DataProcessor process) {
		this.process = process;
	}

	/**
	 * Returns the visited state of this block
	 */
	public boolean isVisited() {

		return visited;
	}

	/**
	 * Set the visited state of this block
	 *
	 * @param visited
	 */
	public void setVisited(boolean visited) {

		this.visited = visited;
	}

	/**
	 * Returns the terminal state of this block
	 */
	public boolean isTerminal() {

		return isTerminal;
	}

	/**
	 * Set to <code>true<code> the terminal state of this block
	 */
	public void setTerminal() {

		this.isTerminal = true;
	}

	/**
	 * Returns the initial state of this block
	 */
	public boolean isInitial() {

		return isInitial;
	}

	/**
	 * Set to <code>true<code> the initial state of this block
	 */
	public void setInitial() {

		this.isInitial = true;
	}

	/**
	 * Adds the supplied successor to this block
	 *
	 * @param successor
	 */
	public void addSuccessor(Workblock successor) {

		successors.add(successor);
	}

	/**
	 * Adds the supplied predecessor to this block
	 *
	 * @param successor
	 */
	public void addPredecessor(Workblock predecessor) {

		predecessors.add(predecessor);
	}

	/**
	 * Returns the successors of this block
	 */
	public Set<Workblock> getSuccessors() {

		return successors;
	}

	/**
	 * Returns the predecessors of this block
	 */
	public Set<Workblock> getPredecessors() {

		return predecessors;
	}

	/**
	 * Finds all the predecessors of this block (if it is contained in an adjacentes
	 * list) having at list one predecessor (this excludes the initial blocks) and
	 * inserts them in the supplied <code>list</code>
	 *
	 * @param list
	 */
	private void findPredecessors(List<Workblock> list) {

		if (!list.contains(this) && !this.getPredecessors().isEmpty() && this.isAdjacent()) {

			list.add(this);

			for (Workblock proc : predecessors) {

				proc.findPredecessors(list);
			}
		}
	}

	/**
	 * Return the sum of the preferences of all the predecessors (directs and non)
	 * of this block. In the computation brothers blocks (blocks having the same
	 * successor) are considered as a single block having as preference the max
	 * preference. This is fine because the algorithm which uses this method
	 * compares only blocks at the same level in the blocks graph, so even if a
	 * block has more predecessors of another, the path which brings to the root has
	 * the same length
	 */
	public int getHistoryPreference() {

		List<Workblock> allPredList = Lists.newArrayList();
		findPredecessors(allPredList);

		return allPredList.//
				stream().//
				// collects only blocks with the same successor. the iterator.next call is safe
				// since allPredList contains only blocks in a adjacentes list, which contain
				// only non deadend blocks
				collect(Collectors.groupingBy(w -> w.getSuccessors().iterator().next().getId())).//
				// getting all the values. if a list contains multiple blocks, they are brothers
				values().//
				// a stream of list of blocks
				stream().//
				// each list of blocks is mapped to a single integer value
				mapToInt(l ->
				// this is a stream of blocks
				l.stream().//
				// maps to int by preference
						mapToInt(w -> w.getPreference()).//
						// get the max value, thus reducing brothers blocks in a single block having as
						// preference the max value of all the preferences
						max().//
						getAsInt())//
				// get the sum
				.sum();//

	}

	/**
	 * Get the identifier of this block
	 */
	public String getId() {

		return id;
	}

	/**
	 * Set this block identifier
	 *
	 * @param id
	 */
	public void setId(String id) {

		this.id = id;
	}

	/**
	 * Get the input of the {@link DataProcessor} of this block
	 *
	 * @see DataProcessor#getInputCapabilities()
	 */
	public ProcessorCapabilities getInput() {

		return process.getInputCapabilities();
	}

	/**
	 * Get the output of the {@link DataProcessor} of this block
	 *
	 * @see DataProcessor#getOutputCapabilities()
	 */
	public ProcessorCapabilities getOutput() {

		return process.getOutputCapabilities();
	}

	@Override
	public boolean equals(Object o) {

		if (o == null)
			return false;

		if (!(o instanceof Workblock))
			return false;

		Workblock v = (Workblock) o;

		ProcessorCapabilities input = v.getInput();

		ProcessorCapabilities output = v.getOutput();

		return this.getInput().equals(input) && this.getOutput().equals(output);
	}

	@Override
	public String toString() {

		String status = " C";

		String initial = isInitial ? " I" : "";
		String terminal = isTerminal ? " T" : "";

		if (isInitial() || isTerminal()) {
			status = initial + terminal;
		}

		String processInfo = "";

		if (getProcess() != null) {
			processInfo = " (" + getProcess().toString() + ") ";
		}

		return getId() + processInfo + ": " + getInput() + " -> " + getOutput() + //
				" [" + status + "]";
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}
}
