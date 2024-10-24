package eu.essi_lab.worflow.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class WorkflowBuildingTest {

    @Test
    public void twoWorkflowsSameInitialSameTerminalTest() {

	Workblock p0 = TestUtils.createBlock("0", "a", "b"); // 0 INITIAL

	Workblock p1 = TestUtils.createBlock("1", "b", "c"); // 1
	Workblock p4 = TestUtils.createBlock("4", "b", "k"); // 1

	Workblock p2 = TestUtils.createBlock("2", "k", "e"); // 2
	Workblock p3 = TestUtils.createBlock("3", "c", "e"); // 2 TERMINAL

	// a -> e
	//
	// path 1
	// 0: a -> b
	// 1: b -> c
	// 3: c -> e
	//
	// path 1
	// 0: a -> b
	// 4: b -> k
	// 3: k -> e
	//

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "a", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "e", "");

	    // TestUtils.enablePrinting(true);
	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

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
				w.getWorkblocks().get(1).getId().equals("4") && //
				w.getWorkblocks().get(2).getId().equals("2")//
			).count(); //

		Assert.assertEquals(1, count);
	    }
	}
    }

    @Test
    public void twoWorkflowsDifferentInitialSameTerminal() {

	Workblock p0 = TestUtils.createBlock("0", "a", "b"); // 0
	Workblock p1 = TestUtils.createBlock("1", "b", "c"); // 1

	Workblock p3 = TestUtils.createBlock("3", "a", "k"); // 0
	Workblock p4 = TestUtils.createBlock("4", "k", "c"); // 1

	Workblock p5 = TestUtils.createBlock("5", "c", "e"); // 2

	// a -> e
	//
	// path 0
	// 0: a -> b
	// 1: b -> c
	// 2: c -> e
	//
	// path 1
	// 3: a -> k
	// 4: k -> c
	// 5: c -> e
	//

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "a", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "e", "");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);
	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(2, workflows.size());

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("5")//
			).count(); //

		Assert.assertEquals(1, count);
	    }

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("3") && //
				w.getWorkblocks().get(1).getId().equals("4") && //
				w.getWorkblocks().get(2).getId().equals("5")//
			).count(); //

		Assert.assertEquals(1, count);
	    }
	}
    }

    @Test
    public void twoWorkflowsDifferentInitialDifferentTerminals() {

	Workblock p0 = TestUtils.createBlock("0", "a", "b"); // 0
	Workblock p1 = TestUtils.createBlock("1", "b", "c"); // 1
	Workblock p2 = TestUtils.createBlock("2", "c", "e"); // 2

	Workblock p3 = TestUtils.createBlock("3", "a", "k"); // 0
	Workblock p4 = TestUtils.createBlock("4", "k", "d"); // 1
	Workblock p5 = TestUtils.createBlock("5", "d", "e"); // 2

	// a -> e
	//
	// path 0
	// 0: a -> b
	// 1: b -> c
	// 2: c -> e
	//
	// path 1
	// 3: a -> k
	// 4: k -> d
	// 5: d -> e
	//

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

	    DataDescriptor input = TestUtils.create(DataType.POINT, "a", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "e", "");

	    List<Workflow> workflows = builder.build(input, output);//

	    // TestUtils.enablePrinting(true);
	    // TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(2, workflows.size());

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("2")//
			).count(); //

		Assert.assertEquals(1, count);
	    }

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("3") && //
				w.getWorkblocks().get(1).getId().equals("4") && //
				w.getWorkblocks().get(2).getId().equals("5")//
			).count(); //

		Assert.assertEquals(1, count);
	    }
	}
    }

    @Test
    public void unreachableTerminalTest() {

	Workblock p0 = TestUtils.createBlock("0", "a", "b"); // 0

	Workblock p1 = TestUtils.createBlock("1", "b", "c"); // 1
	Workblock p4 = TestUtils.createBlock("4", "b", "k"); // 1

	Workblock p2 = TestUtils.createBlock("2", "c", "d"); // 2
	Workblock p3 = TestUtils.createBlock("3", "c", "e"); // 2
	Workblock p5 = TestUtils.createBlock("5", "k", "c"); // 2

	Workblock p6 = TestUtils.createBlock("6", "x", "y"); // none

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p6);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "a", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "y", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    Assert.assertTrue(workflows.isEmpty());

	    Assert.assertEquals(0, workflows.size());
	}
    }

    @Test
    public void twoInitialsThreeTerminalTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "B"); // INITIAL
	Workblock p1 = TestUtils.createBlock("1", "B", "C");
	Workblock p2 = TestUtils.createBlock("2", "C", "D");
	Workblock p3 = TestUtils.createBlock("3", "D", "E"); // TERMINAL
	Workblock p4 = TestUtils.createBlock("4", "E", "F");
	Workblock p5 = TestUtils.createBlock("5", "F", "G");

	Workblock p6 = TestUtils.createBlock("6", "A", "G"); // INITIAL
	Workblock p7 = TestUtils.createBlock("7", "B", "G");
	Workblock p8 = TestUtils.createBlock("8", "B", "F");
	Workblock p9 = TestUtils.createBlock("9", "F", "E"); // TERMINAL
	Workblock p10 = TestUtils.createBlock("10", "G", "E"); // TERMINAL

	Workblock p11 = TestUtils.createBlock("11", "E", "D");
	Workblock p12 = TestUtils.createBlock("12", "E", "B");

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p6);
	    shuffler.add(p7);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p10);
	    shuffler.add(p11);
	    shuffler.add(p12);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

	    long count = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 2 && //
			    w.getWorkblocks().get(0).getId().equals("6") && //
			    w.getWorkblocks().get(1).getId().equals("10") //
		    ).count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void terminalAtDifferentLevelsSameInitialTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "K");// 0

	Workblock p1 = TestUtils.createBlock("1", "K", "S");// 1
	Workblock p2 = TestUtils.createBlock("2", "K", "E");// 1
	Workblock p4 = TestUtils.createBlock("4", "K", "M");// 1
	Workblock p3 = TestUtils.createBlock("3", "K", "T");// 1

	Workblock p5 = TestUtils.createBlock("5", "M", "Z");// 2
	Workblock p7 = TestUtils.createBlock("7", "T", "F");// 2
	Workblock p8 = TestUtils.createBlock("8", "T", "X");// 2 TERMINAL
	Workblock p9 = TestUtils.createBlock("9", "T", "Y");// 2

	Workblock p10 = TestUtils.createBlock("10", "F", "X");// 3 TERMINAL
	Workblock p11 = TestUtils.createBlock("11", "Y", "X");// 3 TERMINAL
	Workblock p12 = TestUtils.createBlock("12", "Y", "O");// 3
	Workblock p6 = TestUtils.createBlock("6", "Z", "X"); // 3 TERMINAL

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p10);
	    shuffler.add(p6);
	    shuffler.add(p3);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p11);
	    shuffler.add(p12);
	    shuffler.add(p2);
	    shuffler.add(p7);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "X", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(1, workflows.size());

	    long count = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 3 && //
			    w.getWorkblocks().get(0).getId().equals("0") && //
			    w.getWorkblocks().get(1).getId().equals("3") && //
			    w.getWorkblocks().get(2).getId().equals("8") //
		    ).count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void terminalAtDifferentLevelsDifferentInitialTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "K");// 0 INITIAL

	Workblock p1 = TestUtils.createBlock("1", "K", "S");// 1
	Workblock p2 = TestUtils.createBlock("2", "K", "E");// 1
	Workblock p4 = TestUtils.createBlock("4", "K", "M");// 1
	Workblock p3 = TestUtils.createBlock("3", "K", "T");// 1

	Workblock p5 = TestUtils.createBlock("5", "M", "Z");// 2
	Workblock p7 = TestUtils.createBlock("7", "T", "F");// 2
	Workblock p8 = TestUtils.createBlock("8", "T", "X");// 2 TERMINAL
	Workblock p9 = TestUtils.createBlock("9", "T", "Y");// 2

	Workblock p10 = TestUtils.createBlock("10", "F", "X");// 3 TERMINAL
	Workblock p11 = TestUtils.createBlock("11", "Y", "X");// 3 TERMINAL
	Workblock p12 = TestUtils.createBlock("12", "Y", "O");// 3
	Workblock p6 = TestUtils.createBlock("6", "Z", "X"); // 3 TERMINAL

	Workblock p13 = TestUtils.createBlock("13", "A", "T"); // 0 INITIAL

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p10);
	    shuffler.add(p6);
	    shuffler.add(p3);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p11);
	    shuffler.add(p12);
	    shuffler.add(p2);
	    shuffler.add(p7);
	    shuffler.add(p13);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "X", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(1, workflows.size());

	    long count = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 2 && //
			    w.getWorkblocks().get(0).getId().equals("13") && //
			    w.getWorkblocks().get(1).getId().equals("8") //
		    ).count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void blockInitialAndTerminalTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "B");
	Workblock p1 = TestUtils.createBlock("1", "B", "C");
	Workblock p2 = TestUtils.createBlock("2", "C", "D");
	Workblock p3 = TestUtils.createBlock("3", "D", "E");
	Workblock p4 = TestUtils.createBlock("4", "E", "F");
	Workblock p5 = TestUtils.createBlock("5", "F", "G");
	Workblock p6 = TestUtils.createBlock("6", "A", "G");
	Workblock p7 = TestUtils.createBlock("7", "A", "E");
	Workblock p8 = TestUtils.createBlock("8", "B", "G");
	Workblock p9 = TestUtils.createBlock("9", "B", "F");
	Workblock p10 = TestUtils.createBlock("10", "F", "E");
	Workblock p11 = TestUtils.createBlock("11", "E", "D");
	Workblock p12 = TestUtils.createBlock("12", "E", "B");
	Workblock p13 = TestUtils.createBlock("13", "G", "E");

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p6);
	    shuffler.add(p7);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p10);
	    shuffler.add(p11);
	    shuffler.add(p12);
	    shuffler.add(p13);

	    shuffler.shuffle(builder);

	    // here there are 3 possible paths from 3 different initial vertexes:
	    // 1) 7: A -> E [ I T] - TERMINAL LEVEL 0
	    // 2) 6: A -> G [ I], 13: G -> E [ T] - TERMINAL LEVEL 1
	    // 3) 0: A -> B [ I], 1: B -> C [ C], 2: C -> D [ C], 3: D -> E [ T] - TERMINAL LEVEL 3
	    // the shortest is of course A -> E

	    DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

	    Assert.assertEquals(1, workflows.size());

	    long count = workflows.stream().filter(w -> w.getWorkblocks().size() == 1 && //
		    w.getWorkblocks().get(0).getId().equals("7") //
	    ).count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void shortWorkflowTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "B");
	Workblock p1 = TestUtils.createBlock("1", "B", "C");
	Workblock p2 = TestUtils.createBlock("2", "C", "D");
	Workblock p3 = TestUtils.createBlock("3", "D", "E");
	Workblock p4 = TestUtils.createBlock("4", "E", "F");
	Workblock p5 = TestUtils.createBlock("5", "F", "G");
	Workblock p6 = TestUtils.createBlock("6", "A", "G");
	Workblock p7 = TestUtils.createBlock("7", "A", "E");
	Workblock p8 = TestUtils.createBlock("8", "B", "G");
	Workblock p9 = TestUtils.createBlock("9", "B", "F");
	Workblock p10 = TestUtils.createBlock("10", "F", "E");
	Workblock p11 = TestUtils.createBlock("11", "E", "D");
	Workblock p12 = TestUtils.createBlock("12", "E", "B");
	Workblock p13 = TestUtils.createBlock("13", "G", "E");

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p6);
	    shuffler.add(p7);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p10);
	    shuffler.add(p11);
	    shuffler.add(p12);
	    shuffler.add(p13);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "C", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

	    List<Workflow> workflows = builder.build(input, output);//
	    TestUtils.printWorkflows(workflows);

	    long count = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 2 && //
			    w.getWorkblocks().get(0).getId().equals("2") && //
			    w.getWorkblocks().get(1).getId().equals("3") //
		    ).count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void allCombinationsTest() {

	Workblock p0 = TestUtils.createBlock("0", "A", "B");
	Workblock p1 = TestUtils.createBlock("1", "A", "C");
	Workblock p2 = TestUtils.createBlock("2", "A", "D");
	Workblock p3 = TestUtils.createBlock("3", "A", "E");

	Workblock p4 = TestUtils.createBlock("4", "B", "A");
	Workblock p5 = TestUtils.createBlock("5", "B", "C");
	Workblock p6 = TestUtils.createBlock("6", "B", "D");
	Workblock p7 = TestUtils.createBlock("7", "B", "E");

	Workblock p8 = TestUtils.createBlock("8", "C", "A");
	Workblock p9 = TestUtils.createBlock("9", "C", "B");
	Workblock p10 = TestUtils.createBlock("10", "C", "D");
	Workblock p11 = TestUtils.createBlock("11", "C", "E");

	Workblock p12 = TestUtils.createBlock("12", "D", "A");
	Workblock p13 = TestUtils.createBlock("13", "D", "B");
	Workblock p14 = TestUtils.createBlock("14", "D", "C");
	Workblock p15 = TestUtils.createBlock("15", "D", "E");

	for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

	    WorkflowBuilder builder = new WorkflowBuilder();

	    Shuffler shuffler = new Shuffler();

	    shuffler.add(p0);
	    shuffler.add(p1);
	    shuffler.add(p2);
	    shuffler.add(p3);
	    shuffler.add(p4);
	    shuffler.add(p5);
	    shuffler.add(p6);
	    shuffler.add(p7);
	    shuffler.add(p8);
	    shuffler.add(p9);
	    shuffler.add(p10);
	    shuffler.add(p11);
	    shuffler.add(p12);
	    shuffler.add(p13);
	    shuffler.add(p14);
	    shuffler.add(p15);

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

	    List<Workflow> workflows = builder.build(input, output);//

	    TestUtils.printWorkflows(workflows);

	    long count = workflows.stream().//
		    filter(w -> w.getWorkblocks().size() == 1 && w.getWorkblocks().get(0).getId().equals("3")).//
		    count(); //

	    Assert.assertEquals(1, count);
	}
    }

    @Test
    public void noInitNoTerminalTest() {

	WorkflowBuilder builder = new WorkflowBuilder();

	Workblock p0 = TestUtils.createBlock("0", "A", "B");
	Workblock p6 = TestUtils.createBlock("6", "A", "G");
	Workblock p7 = TestUtils.createBlock("7", "A", "E");

	Workblock p1 = TestUtils.createBlock("1", "B", "C");
	Workblock p8 = TestUtils.createBlock("8", "B", "G");
	Workblock p9 = TestUtils.createBlock("9", "B", "F");

	Workblock p2 = TestUtils.createBlock("2", "C", "D");
	Workblock p3 = TestUtils.createBlock("3", "D", "E");

	Workblock p4 = TestUtils.createBlock("4", "E", "F");
	Workblock p11 = TestUtils.createBlock("11", "E", "D");
	Workblock p12 = TestUtils.createBlock("12", "E", "B");

	Workblock p5 = TestUtils.createBlock("5", "F", "G");
	Workblock p10 = TestUtils.createBlock("10", "F", "E");

	Workblock p13 = TestUtils.createBlock("13", "G", "E");

	builder.add(p6);

	builder.add(p2);
	builder.add(p3);
	builder.add(p1);

	builder.add(p9);

	builder.add(p11);
	builder.add(p4);
	builder.add(p5);
	builder.add(p8);
	builder.add(p7);

	builder.add(p12);
	builder.add(p13);
	builder.add(p0);
	builder.add(p10);

	DataDescriptor input = TestUtils.create(DataType.POINT, "X", "");
	DataDescriptor output = TestUtils.create(DataType.POINT, "Y", "");

	List<Workflow> workflows = builder.build(input, output);//
	TestUtils.printWorkflows(workflows);

	Assert.assertEquals(0, workflows.size());
    }

    @Test
    public void maxWorkflowLengthReachedTest1() {

	{

	    Workblock p0 = TestUtils.createBlock("0", "A", "K");// 0 INITIAL
	    Workblock p1 = TestUtils.createBlock("1", "K", "S");// 1
	    Workblock p2 = TestUtils.createBlock("2", "S", "X");// 2 TERMINAL

	    for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

		WorkflowBuilder builder = new WorkflowBuilder();

		Shuffler shuffler = new Shuffler();

		shuffler.add(p0);
		shuffler.add(p1);
		shuffler.add(p2);

		shuffler.shuffle(builder);

		// with this length, since the only available workflow has length is 3 (0, 1, 2),
		// no result is expected
		builder.setMaxWorkflowLength(2);

		DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
		DataDescriptor output = TestUtils.create(DataType.POINT, "X", "");

		List<Workflow> workflows = builder.build(input, output);//

		TestUtils.printWorkflows(workflows);

		Assert.assertEquals(0, workflows.size());
	    }
	}

	{
	    Workblock p0 = TestUtils.createBlock("0", "A", "B");// 0 INITIAL
	    Workblock p1 = TestUtils.createBlock("1", "B", "C");// 1
	    Workblock p2 = TestUtils.createBlock("2", "C", "D");// 2
	    Workblock p3 = TestUtils.createBlock("3", "D", "E");// 3 TERMINAL
	    Workblock p4 = TestUtils.createBlock("4", "E", "F");// 4

	    for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

		WorkflowBuilder builder = new WorkflowBuilder();

		Shuffler shuffler = new Shuffler();

		shuffler.add(p0);
		shuffler.add(p1);
		shuffler.add(p2);
		shuffler.add(p3);
		shuffler.add(p4);

		shuffler.shuffle(builder);

		// with this length, since the only available workflow has length is 3 (0, 1, 2, 3),
		// no result is expected
		builder.setMaxWorkflowLength(3);

		DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
		DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

		List<Workflow> workflows = builder.build(input, output);//

		TestUtils.printWorkflows(workflows);

		Assert.assertEquals(0, workflows.size());
	    }

	    for (int i = 0; i < TestUtils.RANDOM_ITERATIONS; i++) {

		WorkflowBuilder builder = new WorkflowBuilder();

		Shuffler shuffler = new Shuffler();

		shuffler.add(p0);
		shuffler.add(p1);
		shuffler.add(p2);
		shuffler.add(p3);
		shuffler.add(p4);

		shuffler.shuffle(builder);

		// with this length, since the only available workflow has length is 3 (0, 1, 2, 3),
		// no result is expected
		builder.setMaxWorkflowLength(4);

		DataDescriptor input = TestUtils.create(DataType.POINT, "A", "");
		DataDescriptor output = TestUtils.create(DataType.POINT, "E", "");

		List<Workflow> workflows = builder.build(input, output);//

		TestUtils.printWorkflows(workflows);

		Assert.assertEquals(1, workflows.size());
	    }
	}
    }
}
