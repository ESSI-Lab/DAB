package eu.essi_lab.worflow.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class PreferencesTest {

    @Test
    public void diamondTestWithPreference1() {

	Workblock p0 = TestUtils.createBlock("0", //
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), "c3", "f3"); // 0 INITIAL

	Workblock p1 = TestUtils.createBlock("1", //
		Arrays.asList("c3", "c4"), //
		Arrays.asList("f3", "f4"), "c5", "f5"); // 1 SUCCESSOR OF 0

	// gives the preference to the block 1
	p1.setPreference(2);

	Workblock p4 = TestUtils.createBlock("2", //
		Arrays.asList("c3", "c6"), //
		Arrays.asList("f3", "f6"), "c7", "f7"); // 1 SUCCESSOR OF 0

	Workblock p2 = TestUtils.createBlock("3", // this is a dead end block
		Arrays.asList("c5", "c8"), //
		Arrays.asList("f5", "f8"), "x", "y"); // 2 SUCCESSOR OF 1

	Workblock p3 = TestUtils.createBlock("4", //
		Arrays.asList("c7", "c10", "c5"), //
		Arrays.asList("f7", "f10", "f5"), "c9", "f9"); // 2 TERMINAL

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

	    TestUtils.printWorkflows(workflows);

	    // only one created
	    Assert.assertEquals(1, workflows.size());

	    // the sum of the 3 blocks preference
	    Assert.assertEquals(4, workflows.get(0).getPreference());

	    {
		Optional<Workflow> first = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findFirst(); //

		Assert.assertTrue(first.isPresent());
	    }
	}
    }

    @Test
    public void diamondTestWithPreference2() {

	Workblock p0 = TestUtils.createBlock("0", //
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), "c3", "f3"); // 0 INITIAL

	Workblock p1 = TestUtils.createBlock("1", //
		Arrays.asList("c3", "c4"), //
		Arrays.asList("f3", "f4"), "c5", "f5"); // 1 SUCCESSOR OF 0

	Workblock p4 = TestUtils.createBlock("2", //
		Arrays.asList("c3", "c6"), //
		Arrays.asList("f3", "f6"), "c7", "f7"); // 1 SUCCESSOR OF 0

	// gives the preference to the block 2
	p4.setPreference(2);

	Workblock p2 = TestUtils.createBlock("3", // this is a dead end block
		Arrays.asList("c5", "c8"), //
		Arrays.asList("f5", "f8"), "x", "y"); // 2 SUCCESSOR OF 1

	Workblock p3 = TestUtils.createBlock("4", //
		Arrays.asList("c7", "c10", "c5"), //
		Arrays.asList("f7", "f10", "f5"), "c9", "f9"); // 2 TERMINAL

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

	    TestUtils.printWorkflows(workflows);

	    // only one created
	    Assert.assertEquals(1, workflows.size());

	    // the sum of the 3 blocks preference
	    Assert.assertEquals(4, workflows.get(0).getPreference());

	    {
		Optional<Workflow> first = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("2") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findFirst(); //

		Assert.assertTrue(first.isPresent());
	    }
	}
    }

    @Test
    public void preferredTerminalBlockTest() {

	Workblock p0 = TestUtils.createBlock("0", "CRS1", "CRS2");
	Workblock p1 = TestUtils.createBlock("1", "CRS2", "CRS3");

	// TERMINALS
	Workblock p2 = TestUtils.createBlock("2", Arrays.asList("CRS3", "CRS4"), Arrays.asList(""), "CRSX", "");
	Workblock p3 = TestUtils.createBlock("3", Arrays.asList("CRS3", "CRS5"), Arrays.asList(""), "CRSX", "");
	Workblock p4 = TestUtils.createBlock("4", Arrays.asList("CRS3", "CRS6"), Arrays.asList(""), "CRSX", "");
	Workblock p5 = TestUtils.createBlock("5", Arrays.asList("CRS3", "CRS7"), Arrays.asList(""), "CRSX", "");

	WorkflowBuilder builder = new WorkflowBuilder();

	DataDescriptor input = TestUtils.create(DataType.POINT, "CRS1", "");
	DataDescriptor output = TestUtils.create(DataType.POINT, "CRSX", "");

	{
	    builder.add(p0);
	    builder.add(p1);
	    builder.add(p2);
	    builder.add(p3);
	    builder.add(p4);
	    builder.add(p5);

	    List<Workflow> workflows = builder.build(input, output);//
	    Assert.assertEquals(4, workflows.size());

	    {
		Optional<Workflow> first = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("2")//
			).findAny(); //

		Optional<Workflow> second = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("3")//
			).findAny(); //

		Optional<Workflow> third = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("4")//
			).findAny(); //

		Optional<Workflow> fourth = workflows.stream()
			.filter(w -> w.getWorkblocks().size() == 3 && //
				w.getWorkblocks().get(0).getId().equals("0") && //
				w.getWorkblocks().get(1).getId().equals("1") && //
				w.getWorkblocks().get(2).getId().equals("5")//
			).findAny(); //

		Assert.assertTrue(first.isPresent() || second.isPresent() || third.isPresent() || fourth.isPresent());
	    }
	}

	{

	    p2.setPreference(2);

	    Workflow workflow = builder.buildPreferred(input, output).get();//
	    Assert.assertEquals(3, workflow.getWorkblocks().size());

	    Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
	    Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    Assert.assertEquals("2", workflow.getWorkblocks().get(2).getId());
	}

	resetPreference(builder);

	{

	    p3.setPreference(2);

	    Workflow workflow = builder.buildPreferred(input, output).get();//
	    Assert.assertEquals(3, workflow.getWorkblocks().size());

	    Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
	    Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    Assert.assertEquals("3", workflow.getWorkblocks().get(2).getId());
	}

	resetPreference(builder);

	{

	    p4.setPreference(2);

	    Workflow workflow = builder.buildPreferred(input, output).get();//
	    Assert.assertEquals(3, workflow.getWorkblocks().size());

	    Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
	    Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    Assert.assertEquals("4", workflow.getWorkblocks().get(2).getId());
	}

	resetPreference(builder);

	{
	    p5.setPreference(2);

	    Workflow workflow = builder.buildPreferred(input, output).get();//
	    Assert.assertEquals(3, workflow.getWorkblocks().size());

	    Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
	    Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    Assert.assertEquals("5", workflow.getWorkblocks().get(2).getId());
	}
    }

    @Test
    public void preferredPathTest() {

	// ------------- L0 --------------------------------------

	Workblock p0 = TestUtils.createBlock("0", // INITIAL
		Arrays.asList("c1", "c2"), //
		Arrays.asList("f1", "f2"), //
		"c3", "f3"); //

	// ------------- L1 --------------------------------------

	Workblock p1 = TestUtils.createBlock("1", // SUCCESSOR OF 0
		Arrays.asList("c3", "c4"), //
		Arrays.asList("f3", "f4"), //
		"c6", "f6"); //

	Workblock p2 = TestUtils.createBlock("2", // SUCCESSOR OF 0
		Arrays.asList("c3", "c5"), //
		Arrays.asList("f3", "f5"), //
		"c7", "f7"); //

	// ------------- L2 --------------------------------------

	Workblock p3 = TestUtils.createBlock("3", // SUCCESSOR OF 1
		Arrays.asList("c6", "c8"), //
		Arrays.asList("f6", "f8"), //
		"c11", "f11"); //

	Workblock p4 = TestUtils.createBlock("4", // SUCCESSOR OF 1
		Arrays.asList("c6", "c9"), //
		Arrays.asList("f6", "f9"), //
		"c11", "f11"); //

	Workblock p5 = TestUtils.createBlock("5", // SUCCESSOR OF 1
		Arrays.asList("c6", "c10"), //
		Arrays.asList("f6", "f10"), //
		"c11", "f11"); //

	Workblock p6 = TestUtils.createBlock("6", // L2 SUCCESSOR OF 2
		Arrays.asList("c7", "c15"), //
		Arrays.asList("f7", "f15"), //
		"c12", "f12"); //

	// ------------- L3 --------------------------------------

	Workblock p7 = TestUtils.createBlock("7", // SUCCESSOR OF 3,4,5
		Arrays.asList("c11", "c122"), //
		Arrays.asList("f11", "f122"), //
		"c13", "f13"); //

	Workblock p8 = TestUtils.createBlock("8", // SUCCESSOR of 6
		Arrays.asList("c12", "c16"), //
		Arrays.asList("f12", "f16"), //
		"c17", "f17"); // 1

	// ------------- L4 --------------------------------------

	Workblock p9 = TestUtils.createBlock("9", // SUCCESSOR of 7
		Arrays.asList("c13", "c41"), //
		Arrays.asList("f13", "f41"), //
		"c20", "f20"); // 1

	Workblock p10 = TestUtils.createBlock("10", // SUCCESSOR of 8
		Arrays.asList("c17", "c40"), //
		Arrays.asList("f17", "f40"), //
		"c20", "f20"); //

	// ------------- L5 --------------------------------------

	Workblock p11 = TestUtils.createBlock("11", //
		Arrays.asList("c20", "c42"), //
		Arrays.asList("f20", "f42"), //
		"c30", "f30"); //

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

	    shuffler.shuffle(builder);

	    DataDescriptor input = TestUtils.create(DataType.POINT, "c1", "f1");
	    DataDescriptor output = TestUtils.create(DataType.POINT, "c30", "f30");

	    List<Workflow> workflows = builder.build(input, output);

	    //
	    //
	    // with no preferences, there are 4 possible paths
	    //
	    //

	    Optional<Workflow> first = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 6 && //
			    w.getWorkblocks().get(0).getId().equals("0") && //
			    w.getWorkblocks().get(1).getId().equals("2") && //
			    w.getWorkblocks().get(2).getId().equals("6") && //
			    w.getWorkblocks().get(3).getId().equals("8") && //
			    w.getWorkblocks().get(4).getId().equals("10") && //
			    w.getWorkblocks().get(5).getId().equals("11")//
		    ).findAny(); //

	    Optional<Workflow> second = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 6 && //
			    w.getWorkblocks().get(0).getId().equals("0") && //
			    w.getWorkblocks().get(1).getId().equals("1") && //
			    w.getWorkblocks().get(2).getId().equals("5") && //
			    w.getWorkblocks().get(3).getId().equals("7") && //
			    w.getWorkblocks().get(4).getId().equals("9") && //
			    w.getWorkblocks().get(5).getId().equals("11")//
		    ).findAny(); //

	    Optional<Workflow> third = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 6 && //
			    w.getWorkblocks().get(0).getId().equals("0") && //
			    w.getWorkblocks().get(1).getId().equals("1") && //
			    w.getWorkblocks().get(2).getId().equals("4") && //
			    w.getWorkblocks().get(3).getId().equals("7") && //
			    w.getWorkblocks().get(4).getId().equals("9") && //
			    w.getWorkblocks().get(5).getId().equals("11")//
		    ).findAny(); //

	    Optional<Workflow> fourth = workflows.stream()
		    .filter(w -> w.getWorkblocks().size() == 6 && //
			    w.getWorkblocks().get(0).getId().equals("0") && //
			    w.getWorkblocks().get(1).getId().equals("1") && //
			    w.getWorkblocks().get(2).getId().equals("3") && //
			    w.getWorkblocks().get(3).getId().equals("7") && //
			    w.getWorkblocks().get(4).getId().equals("9") && //
			    w.getWorkblocks().get(5).getId().equals("11")//
		    ).findAny(); //

	    Assert.assertTrue(first.isPresent() || second.isPresent() || third.isPresent() || fourth.isPresent());

	    // --------------------------------------------------------------------------------
	    //
	    // in the following tests we give always preference to block 10 or its predecessors
	    //
	    //
	    //

	    resetPreference(builder);

	    {
		p2.setPreference(2);

		List<Workflow> w = builder.build(input, output);//

		Assert.assertEquals(6, w.get(0).getWorkblocks().size());

		// w.get(0).getWorkblocks().forEach(b -> System.out.println(b.getId()));

		Assert.assertEquals("0", w.get(0).getWorkblocks().get(0).getId());
		Assert.assertEquals("2", w.get(0).getWorkblocks().get(1).getId());
		Assert.assertEquals("6", w.get(0).getWorkblocks().get(2).getId());
		Assert.assertEquals("8", w.get(0).getWorkblocks().get(3).getId());
		Assert.assertEquals("10", w.get(0).getWorkblocks().get(4).getId());
		Assert.assertEquals("11", w.get(0).getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {
		p6.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
		Assert.assertEquals("6", workflow.getWorkblocks().get(2).getId());
		Assert.assertEquals("8", workflow.getWorkblocks().get(3).getId());
		Assert.assertEquals("10", workflow.getWorkblocks().get(4).getId());
		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {
		p8.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
		Assert.assertEquals("6", workflow.getWorkblocks().get(2).getId());
		Assert.assertEquals("8", workflow.getWorkblocks().get(3).getId());
		Assert.assertEquals("10", workflow.getWorkblocks().get(4).getId());
		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {
		p10.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
		Assert.assertEquals("6", workflow.getWorkblocks().get(2).getId());
		Assert.assertEquals("8", workflow.getWorkblocks().get(3).getId());
		Assert.assertEquals("10", workflow.getWorkblocks().get(4).getId());
		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    // -------------------------------------------------------------------------------
	    //
	    // in the following tests we give always preference to block 9 or its predecessors
	    //
	    //
	    //
	    {
		p9.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());

		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());

		// since the preference is given only to the block 9, there is no
		// way to know exactly which successors will be selected
		Assert.assertTrue( //
			workflow.getWorkblocks().get(2).getId().equals("3") || //
				workflow.getWorkblocks().get(2).getId().equals("4") || //
				workflow.getWorkblocks().get(2).getId().equals("5")); //

		Assert.assertEquals("7", workflow.getWorkblocks().get(3).getId());

		Assert.assertEquals("9", workflow.getWorkblocks().get(4).getId());

		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {
		p5.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());

		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());

		// with a preference on block 5, the path is well known
		Assert.assertEquals("5", workflow.getWorkblocks().get(2).getId());

		Assert.assertEquals("7", workflow.getWorkblocks().get(3).getId());

		Assert.assertEquals("9", workflow.getWorkblocks().get(4).getId());

		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {
		p9.setPreference(2);
		p5.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());

		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());

		// with a preference on block 5, the path is well known
		Assert.assertEquals("5", workflow.getWorkblocks().get(2).getId());

		Assert.assertEquals("7", workflow.getWorkblocks().get(3).getId());

		Assert.assertEquals("9", workflow.getWorkblocks().get(4).getId());

		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    {

		// setting a preference on a remote block, predecessor of the root
		p3.setPreference(2);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("0", workflow.getWorkblocks().get(0).getId());

		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());

		// since the preference is given only to the remote block 3, there is no
		// way to know exactly which successors will be selected
		Assert.assertTrue( //
			workflow.getWorkblocks().get(2).getId().equals("3") || //
				workflow.getWorkblocks().get(2).getId().equals("4") || //
				workflow.getWorkblocks().get(2).getId().equals("5")); //

		Assert.assertEquals("7", workflow.getWorkblocks().get(3).getId());

		Assert.assertEquals("9", workflow.getWorkblocks().get(4).getId());

		Assert.assertEquals("11", workflow.getWorkblocks().get(5).getId());
	    }

	    resetPreference(builder);

	    // -------------------------------------------------------------------------------
	    //
	    // in the following tests we give preference both to block 9 predecessors and block
	    // 10 predecessors (direct predecessor of the terminal block 11)
	    //
	    //
	    //
	    {
		// setting the same preference on both p9 and p10
		p9.setPreference(Workblock.MEDIUM_PREFERENCE);
		p10.setPreference(Workblock.MEDIUM_PREFERENCE);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// there is no way to know exactly which root successors will be selected
		Assert.assertTrue(//
			workflow.getWorkblocks().get(1).getId().equals("1") || //
				workflow.getWorkblocks().get(1).getId().equals("2"));
	    }

	    resetPreference(builder);

	    {
		// setting more preference on p9
		p9.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p10.setPreference(5);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// now the root successor must be 1 because it is a remote
		// predecessor of 9
		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// setting more preference on p4
		p4.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p10.setPreference(5);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// now the root successor must be 1 because it is the
		// direct predecessor of 4
		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// setting more preference on p3
		p3.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p10.setPreference(5);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// now the root successor must be 1 because it is the
		// direct predecessor of 3
		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// setting more preference on p5
		p5.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p10.setPreference(5);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// now the root successor must be 1 because it is the
		// direct predecessor of 5
		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// setting more preference on p1
		p1.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p10.setPreference(5);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// now the root successor must be 1
		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    // -------------------------------------------------------------------------------
	    //
	    // in the following tests we give preferences to 2 predecessors of the block 7 (predecessor of 9)
	    // which has blocks 3,4,5 as predecessors. we give also a preference to the block 6, predecessor
	    // of the block 10, which has a different path than 9.
	    // these tests consider the fact that at the moment the preference system works with the sum
	    // of all the predecessors (another solution is to consider the max value of brothers blocks)
	    //
	    resetPreference(builder);

	    {
		// setting the same preference on both p3 and p4
		// which are both predecessors of 7. the block 9
		// has a history preference of 8 considering the sum (4 with the max)
		p3.setPreference(4);// brothers
		p4.setPreference(4);// brothers

		// with this preference the history of 10 is 10, greater
		// than 4 so it is the preferred one
		p6.setPreference(Workblock.MAXIMUM_PREFERENCE);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// the block 2 is expected as consequence of the choice of the block
		// 6 which is a remote successor of 2
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// setting the same preference on both p3 and p4
		// which are both predecessors of 7. so the block 9
		// has a history preference of 5, since for blocks having the same
		// predecessor, a single value (the max) is considered
		// with the max it should be 5
		p3.setPreference(Workblock.MEDIUM_PREFERENCE);// brothers
		p4.setPreference(Workblock.MEDIUM_PREFERENCE);// brothers

		// with this preference the history of 10 is 7, gt than 5
		p6.setPreference(7);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// the block 2 is expected
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    {
		// here the max preference of 3,4,5 (with the same predecessor 7) is 7
		p3.setPreference(5);// brothers
		p4.setPreference(6);// brothers
		p5.setPreference(7);// brothers

		// setting preference of 8 to block 6
		p6.setPreference(8);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		// the block 2 is expected
		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
	    }

	    resetPreference(builder);

	    // ----------------------------------------------
	    //
	    // in this tests all the blocks have a preference
	    //

	    {
		// with the max is p1(2) + p3,p4,p5(7) + b7(7) + b9(10) = 26
		p1.setPreference(2);

		p3.setPreference(3);// brothers
		p4.setPreference(7);// brothers
		p5.setPreference(2);// brothers

		p7.setPreference(7);
		p9.setPreference(Workblock.MAXIMUM_PREFERENCE);

		// 10 + 5 + 3 + 7 = 25
		p2.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p6.setPreference(Workblock.MEDIUM_PREFERENCE);
		p8.setPreference(3);
		p10.setPreference(7);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("1", workflow.getWorkblocks().get(1).getId());
	    }

	    {
		// with the max is p1(1) + p3,p4,p5(5) + b7(5) + b9(5) = 16
		p1.setPreference(Workblock.MINIMUM_PREFERENCE);

		p3.setPreference(3);// brothers
		p4.setPreference(Workblock.MEDIUM_PREFERENCE);// brothers
		p5.setPreference(Workblock.MINIMUM_PREFERENCE);// brothers

		p7.setPreference(Workblock.MEDIUM_PREFERENCE);
		p9.setPreference(Workblock.MEDIUM_PREFERENCE);

		// 10 + 5 + 5 + 1 = 21
		p2.setPreference(Workblock.MAXIMUM_PREFERENCE);
		p6.setPreference(Workblock.MEDIUM_PREFERENCE);
		p8.setPreference(Workblock.MEDIUM_PREFERENCE);
		p10.setPreference(Workblock.MINIMUM_PREFERENCE);

		Workflow workflow = builder.buildPreferred(input, output).get();//
		Assert.assertEquals(6, workflow.getWorkblocks().size());

		Assert.assertEquals("2", workflow.getWorkblocks().get(1).getId());
	    }
	}
    }

    private void resetPreference(WorkflowBuilder builder) {

	builder.getBlocks().forEach(b -> b.setPreference(Workblock.MINIMUM_PREFERENCE));
    }
}
