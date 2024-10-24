package eu.essi_lab.worflow.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class MultiInputTest {

    /**
     * There is 1 distinct couple (IB,TB): (0,4)
     */
    @Test
    public void diamondTest() {

	Workblock p0 = TestUtils.createBlock("0", //
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), "c3", "f3"); // 0 INITIAL

	Workblock p1 = TestUtils.createBlock("1", //
		Arrays.asList("c3", "c4"), //
		Arrays.asList("f3", "f4"), "c5", "f5"); // 1 SUCCESSOR OF 0

	Workblock p4 = TestUtils.createBlock("2", //
		Arrays.asList("c3", "c6"), //
		Arrays.asList("f3", "f6"), "c7", "f7"); // 1 SUCCESSOR OF 0

	Workblock p2 = TestUtils.createBlock("3", // this is a dead end block
		Arrays.asList("c5", "c8"), //
		Arrays.asList("f5", "f8"), "x", "y"); // 2 SUCCESSOR OF 1

	Workblock p3 = TestUtils.createBlock("4", //
		Arrays.asList("c7", "c10", "c5"), // 2 SUCCESSOR OF 1 AND 2
		Arrays.asList("f7", "f10", "f5"), "c9", "f9"); // TERMINAL

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);
	    TestUtils.printWorkflows(workflows);

	    //
	    //
	    // the terminal vertex 4 has 2 predecessors at the same level 1, vertex 1 and vertex 2;
	    // this creates a diamond shape. the current algorithm defines the possible path from the terminal to
	    // the initial considering only one predecessor at each level, so one terminal, one path.
	    // in this case since at level 1 there are 2 predecessors of terminal vertex 4, there are 2 paths
	    // but only one will be considered. depending on the insertion order of the vertex, the predecessor
	    // 1 or 2 will be considered having at last path 0,1,4 or 0,2,4
	    //
	    //

	    // only one created
	    Assert.assertEquals(1, workflows.size());

	    {
		Optional<Workflow> first = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findAny(); //

		Optional<Workflow> second = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findAny(); //

		Assert.assertTrue(first.isPresent() || second.isPresent());
	    }
	}
    }

    /**
     * There are 2 distinct couples (IB,TB): (0,3),(0,4)
     */
    @Test
    public void twoWorkflowOneInitialTwoTerminalsSameLevelTest() {

	Workblock p0 = TestUtils.createBlock("0", Arrays.asList("c1", "c2"), Arrays.asList("f1", "f2"), "c3", "f3"); // 0
	// INITIAL

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1
	Workblock p4 = TestUtils.createBlock("2", Arrays.asList("c3", "c6"), Arrays.asList("f3", "f6"), "c7", "f7"); // 1

	Workblock p2 = TestUtils.createBlock("3", Arrays.asList("c5", "c8"), Arrays.asList("f5", "f8"), "c9", "f9"); // 2
	// TERMINAL
	Workblock p3 = TestUtils.createBlock("4", Arrays.asList("c7", "c10"), Arrays.asList("f7", "f10"), "c9", "f9"); // 2
	// TERMINAL

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    // System.out.println("------------------------");
	    // System.out.println(p0.getSuccessors().size());
	    // System.out.println("------------------------");

	    Assert.assertEquals(2, workflows.size());

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).count(); //

		Assert.assertEquals(1, count);
	    }

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).count(); //

		Assert.assertEquals(1, count);
	    }
	}
    }

    /**
     * 4 workflows are built because there are 4 distinct couples (IB,TB): (0,3),(0,4),(5,3),(5,4)
     */
    @Test
    public void fourWorkflowsTwoInitialTwoTerminal() {

	Workblock p0 = TestUtils.createBlock("0", // 0 INITIAL
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), //
		"c3", "f3"); //

	Workblock p5 = TestUtils.createBlock("5", // 0 INITIAL
		Arrays.asList("c1", "c11"), //
		Arrays.asList("f1", "f11"), //
		"c3", "f3"); //

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1

	Workblock p4 = TestUtils.createBlock("2", Arrays.asList("c3", "c6"), Arrays.asList("f3", "f6"), "c7", "f7"); // 1

	Workblock p2 = TestUtils.createBlock("3", // 2 TERMINAL
		Arrays.asList("c5", "c8"), //
		Arrays.asList("f5", "f8"), //
		"c9", "f9"); //

	Workblock p3 = TestUtils.createBlock("4", // 2 TERMINAL
		Arrays.asList("c7", "c10"), //
		Arrays.asList("f7", "f10"), //
		"c9", "f9");

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(4, workflows.size());

	    //
	    //
	    // there are 4 possible workflows because there are 4 distinct couples (IB,TB) (0,3),(0,4),(5,3),(5,4)
	    // 1) 0,1,3
	    // 2) 0,2,4
	    // 3) 5,1,3
	    // 4) 5,2,4
	    {
		Optional<Workflow> _013 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); // .

		Optional<Workflow> _024 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findAny(); // .

		Optional<Workflow> _513 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("5") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); // .

		Optional<Workflow> _524 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("5") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findAny(); // .

		Assert.assertTrue(_013.isPresent() && _024.isPresent() && _513.isPresent() && _524.isPresent());

	    }
	}
    }

    /**
     * Here there are 4 possible workflows but the couples of distinct
     * (IB,TB) are just 2: (0,3),(5,3)
     */
    @Test
    public void twoWorkflowsDifferentInitialSameTerminal() {

	Workblock p0 = TestUtils.createBlock("0", // 0 INITIAL
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), //
		"c3", "f3"); //

	Workblock p5 = TestUtils.createBlock("5", // 0 INITIAL
		Arrays.asList("c1", "c11"), //
		Arrays.asList("f1", "f11"), //
		"c3", "f3"); //

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1

	Workblock p4 = TestUtils.createBlock("2", Arrays.asList("c3", "c6"), Arrays.asList("f3", "f6"), "c7", "f7"); // 1

	Workblock p2 = TestUtils.createBlock("3", // 2 TERMINAL
		Arrays.asList("c5", "c7"), //
		Arrays.asList("f5", "f7"), //
		"c9", "f9"); //

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p4);
	    shuffler.add(p5);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(2, workflows.size());

	    {
		Optional<Workflow> _013 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); //

		Optional<Workflow> _023 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); //

		Optional<Workflow> _513 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("5") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); // .

		Optional<Workflow> _523 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("5") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); // .

		//
		//
		// there is no diamond shape here, but this is another case where one block
		// has more than one predecessor. here there are 4 possible workflows but the couples of distinct
		// (IB,TB) are just 2: (0,3),(5,3)
		// 0, 1, 3
		// 0, 2, 3
		// 5, 1, 3
		// 5, 2, 3
		// for both initial 0 and initial 5 case, just one workflow can be found with the
		// current implementation of the build algorithm, so the exact number of workflows is
		// always 2, but the combination depends
		//

		Assert.assertTrue(//
			(_013.isPresent() || _023.isPresent()) && //
				(_513.isPresent() || _523.isPresent()));

	    }
	}
    }

    @Test
    public void workflow2LEngthTest() {

	Workblock p0 = TestUtils.createBlock("0", // 0 INITIAL
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), //
		"c3", "f3"); //

	Workblock p5 = TestUtils.createBlock("5", // 0 INITIAL
		Arrays.asList("c1", "c11"), //
		Arrays.asList("f1", "f11"), //
		"c3", "f3"); //

	Workblock p3 = TestUtils.createBlock("4", // 0 INITIAL
		Arrays.asList("c1", "c7"), //
		Arrays.asList("f1", "f7"), //
		"c7", "f7"); //

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1

	Workblock p4 = TestUtils.createBlock("2", Arrays.asList("c3", "c6"), Arrays.asList("f3", "f6"), "c7", "f7"); // 1

	Workblock p2 = TestUtils.createBlock("3", // 2 TERMINAL
		Arrays.asList("c5", "c7"), //
		Arrays.asList("f5", "f7"), //
		"c9", "f9"); //

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p3);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(1, workflows.size());

	    {
		Optional<Workflow> _43 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 2 && //
				w.getWorkblocks().get(0).getId().equals("4") && //
				w.getWorkblocks().get(1).getId().equals("3"))
			.findAny(); //

		Assert.assertTrue(_43.isPresent());

	    }
	}
    }

    /**
     * Here there are 2 distinct couples of (IB,TB): (0,6),(0,3) but the built workflow is only one, because the 2 paths
     * have different length 0 -> 2: l4 (0,1,4,6); 0 -> 3: l3 (0,1,3) so the shortest is selected
     */
    @Test
    public void terminalAtDifferentLevelsSameInitialTest() {

	Workblock p0 = TestUtils.createBlock("0", // 0 INITIAL
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), //
		"c3", "f3"); //

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1

	Workblock p2 = TestUtils.createBlock("3", // 2 TERMINAL
		Arrays.asList("c5", "c7"), //
		Arrays.asList("f5", "f7"), //
		"c9", "f9"); //

	Workblock p3 = TestUtils.createBlock("4", // 2
		Arrays.asList("c5", "c10"), //
		Arrays.asList("f5", "f11"), //
		"c12", "f12"); //

	Workblock p4 = TestUtils.createBlock("6", // 3 TERMINAL
		Arrays.asList("c12", "c10"), //
		Arrays.asList("f12", "f11"), //
		"c9", "f9"); //

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(1, workflows.size());

	    {
		Optional<Workflow> _013 = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); //

		Assert.assertTrue(_013.isPresent());
	    }
	}
    }

    @Test
    public void randomTest() {

	int listLength = 100;
	int vertexesCount = 1000;
	int attemptsCount = 10;
	int initialsCount = 100;
	int terminalsCount = 100;

	String targetCrs = "c" + (listLength - 1);
	String targetFormat = "f" + (listLength - 1);

	List<String> crss = new ArrayList<>();
	for (int i = 0; i < listLength; i++) {
	    crss.add("c" + i);
	}

	List<String> formats = new ArrayList<>();
	for (int i = 0; i < listLength; i++) {
	    formats.add("f" + i);
	}

	for (int i = 0; i < attemptsCount; i++) {

	    HashSet<Workblock> vertexes = Sets.newHashSet();

	    for (int j = 0; j < vertexesCount; j++) {

		String inputCrs1 = j <= initialsCount ? "c0" : nextValue(crss, Arrays.asList(""));
		String inputCrs2 = nextValue(crss, Arrays.asList(inputCrs1));
		String inputCrs3 = nextValue(crss, Arrays.asList(inputCrs1, inputCrs2));

		String inputFormat1 = j <= initialsCount ? "f0" : nextValue(formats, Arrays.asList(""));
		String inputFormat2 = nextValue(formats, Arrays.asList(inputFormat1));
		String inputFormat3 = nextValue(formats, Arrays.asList(inputFormat1, inputFormat2));

		String outputCrs = j >= vertexesCount - terminalsCount ? targetCrs : nextValue(crss, Arrays.asList(inputCrs1, inputCrs2));
		String outputFormat = j >= vertexesCount - terminalsCount ? targetFormat
			: nextValue(formats, Arrays.asList(inputFormat1, inputFormat2));

		Workblock vertex = TestUtils.createBlock(String.valueOf(j), //
			Arrays.asList(inputCrs1, inputCrs2, inputCrs3), //
			Arrays.asList(inputFormat1, inputFormat2, inputFormat3), //
			outputCrs, outputFormat); //

		vertexes.add(vertex);
	    }

	    WorkflowBuilder builder = new WorkflowBuilder();

	    builder.add(vertexes.stream().collect(Collectors.toList()));

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c9", "f9");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);

	    TestUtils.printWorkflows(workflows);

	    if (!workflows.isEmpty()) {

		int size = workflows.get(0).getWorkblocks().size();
		boolean allMatch = workflows.stream().allMatch(w -> w.getWorkblocks().size() == size);
		Assert.assertTrue(allMatch);

	    }

	    System.out.println("Attempt [" + (i + 1) + "/" + attemptsCount + "] done");
	}
    }

    private String nextValue(List<String> list, List<String> last) {

	Random random = new Random();

	int index = random.nextInt(list.size());
	String value = list.get(index);

	while (last.contains(value)) {

	    index = random.nextInt(10);
	    value = list.get(index);
	}

	return value;
    }
}
