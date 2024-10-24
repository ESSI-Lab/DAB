package eu.essi_lab.worflow.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class MultiLevelBlocksTest {

    /**
     * 
     */
    @Test
    public void test1() {

	Workblock p0 = TestUtils.createBlock("0", "a", "b"); // 0

	Workblock p1 = TestUtils.createBlock("1", "b", "c"); // 1
	Workblock p4 = TestUtils.createBlock("2", "b", "k"); // 1

	// it can be reached from:
	// level 1 ( a -> b, b -> c, c -> d) so it is at level 2
	// level 2 ( a -> b, b -> k, k -> c, c -> d) so it is at level 3
	Workblock p2 = TestUtils.createBlock("3", "c", "d"); // 2

	// it can be reached from:
	// level 1 ( a -> b, b -> c, c -> e) so it is at level 2
	// level 2 ( a -> b, b -> k, k -> c, c -> e) so it is at level 3
	Workblock p3 = TestUtils.createBlock("4", "c", "e"); // 2 TERMINAL NODE

	Workblock p5 = TestUtils.createBlock("5", "k", "c"); // 2

	// a -> e
	//
	// path 0
	// 0: a -> b (level 0)
	// 1: b -> c (level 1)
	// 4: c -> e (level 2)
	//

	// a -> e
	//
	// path 1
	// 0: a -> b (level 0)
	// 2: b -> k (level 1)
	// 5: k -> c (level 2)
	// 4: c -> e (level 3)
	//

	// --------------------------------------------------------
	//
	// terminal node 4 could be both at level 2 or 3
	// depending on the order by which the blocks are traversed. in particular
	// if the block 2 is traversed before block, the adj map status is as
	// following:

	// -----------------------------
	// without multi-level condition
	// -----------------------------

	// end of level 0
	//
	// 0=[0]
	//
	// end of level 1
	//
	// 0=[0]
	// 1=[2,1]
	//
	// end of level 2
	//
	// 0=[0]
	// 1=[2,1]
	// 2=[5]

	// --------------------------
	// with multi-level condition
	// --------------------------

	// end of level 0
	//
	// 0=[0]
	//
	// end of level 1
	//
	// 0=[0]
	// 1=[2,1]
	//
	// end of level2
	//
	// 0=[0]
	// 1=[2,1]
	// 2=[5,3,4]

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
	    TestUtils.printWorkflows(workflows);

	    // for (Workflow workflow : workflows) {
	    // System.out.println(workflow);
	    // }

	    Assert.assertEquals(1, workflows.size());

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).count(); //

		Assert.assertEquals(1, count);
	    }
	}
    }

    /**
     * There is 1 distinct couple (IB,TB): (0,4)
     */
    @Test
    public void test2() {

	Workblock p0 = TestUtils.createBlock("0", Arrays.asList("c1", "c2"), Arrays.asList("f1", "f2"), "c3", "f3"); // 0
	// INITIAL

	Workblock p1 = TestUtils.createBlock("1", Arrays.asList("c3", "c4"), Arrays.asList("f3", "f4"), "c5", "f5"); // 1
	Workblock p4 = TestUtils.createBlock("2", Arrays.asList("c3", "c6"), Arrays.asList("f3", "f6"), "c7", "f7"); // 1

	Workblock p2 = TestUtils.createBlock("3", Arrays.asList("c5", "c8"), Arrays.asList("f5", "f8"), "c9", "f9"); // 2
	// TERMINAL
	Workblock p3 = TestUtils.createBlock("4", Arrays.asList("c7", "c10"), Arrays.asList("f7", "f10"), "c8", "f8"); // 2
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

	    Assert.assertEquals(1, workflows.size());

	    {
		long count = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).count(); //

		Assert.assertEquals(1, count);
	    }

	}
    }

}
