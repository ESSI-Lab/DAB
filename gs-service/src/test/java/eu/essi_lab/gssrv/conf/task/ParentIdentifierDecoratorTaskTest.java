package eu.essi_lab.gssrv.conf.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.ParentIdentifierDecoratorTask;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Mattia Santoro
 */
public class ParentIdentifierDecoratorTaskTest {

	private DatabaseReader dbReader;
	private DatabaseWriter dbWriter;
	private ParentIdentifierDecoratorTask task;
	private DatabaseFinder dbFinder;

	@Before
	public void init() throws GSException {

		dbReader = Mockito.mock(DatabaseReader.class);
		dbWriter = Mockito.mock(DatabaseWriter.class);
		dbFinder = Mockito.mock(DatabaseFinder.class);
		task = Mockito.spy(new ParentIdentifierDecoratorTask());

		Mockito.doReturn(dbReader).when(task).getDataBaseReader();
		Mockito.doReturn(dbWriter).when(task).getDataBaseWriter();
		Mockito.doReturn(dbFinder).when(task).getDataBaseFinder();
	}

	@Test
	public void test1() throws Exception {

		Mockito.doReturn(Optional.empty()).when(task).readTaskOptions(Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));
	}

	@Test
	public void test2() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	public void test3() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(s1list, new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(dbWriter, Mockito.times(0)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(0)).store((GSResource) Mockito.any());
	}

	@Test
	public void test4() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(s1list, new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent@s1");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent@s1");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(dbReader, Mockito.times(3)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(1)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(1)).store((GSResource) Mockito.any());
	}

	@Test
	public void test5() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		GSResource r1_3 = new Dataset();
		r1_3.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);
		s1list.add(r1_3);

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(s1list, new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent@s1");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent@s1");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(dbReader, Mockito.times(3)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(2)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(2)).store((GSResource) Mockito.any());
	}

	@Test
	public void test6() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent@s1");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		GSResource r1_3 = new Dataset();
		r1_3.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);
		s1list.add(r1_3);

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(s1list, new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent@s1");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent@s1");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent@s1");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_1_parent".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata().getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_1_parent");

				return null;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_2_parent@s1".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata()
						.getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_2_parent@s1");

				return null;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_2_parent@s1".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata()
						.getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_2_parent@s1");

				return null;
			}
		}).when(dbWriter).store((GSResource) Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(dbReader, Mockito.times(4)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(3)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(3)).store((GSResource) Mockito.any());
	}

	@Test
	public void test7() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		configuredSources.add(source1);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent@s1");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		GSResource r1_3 = new Dataset();
		r1_3.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);
		s1list.add(r1_3);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");

				DiscoveryCountResponse response = new DiscoveryCountResponse();
				response.setCount(5);
				return response;
			}
		}).when(dbFinder).count(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(s1list);
				return resultSet;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s2".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(new ArrayList<>());
				return resultSet;
			}
		}).when(dbFinder).discover(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent@s1");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent@s1");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent@s1");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_1_parent".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata().getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_1_parent");

				return null;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_2_parent@s1".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata()
						.getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_2_parent@s1");

				return null;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_2_parent@s1".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata()
						.getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_2_parent@s1");

				return null;
			}
		}).when(dbWriter).store((GSResource) Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(1)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(1)).count(Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(1)).discover(Mockito.any());

		Mockito.verify(dbReader, Mockito.times(4)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(3)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(3)).store((GSResource) Mockito.any());
	}

	@Test
	public void test8() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);

		GSResource r2_1 = new Dataset();
		r2_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r2_1_parent");

		List<GSResource> s2list = new ArrayList<>();
		s1list.add(r2_1);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");

				DiscoveryCountResponse response = new DiscoveryCountResponse();
				response.setCount(1);
				return response;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s2".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");

				DiscoveryCountResponse response = new DiscoveryCountResponse();
				response.setCount(1);
				return response;
			}
		}).when(dbFinder).count(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(s1list);
				return resultSet;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s2".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(s2list);
				return resultSet;
			}
		}).when(dbFinder).discover(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent@s1");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent@s1");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r2_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r2_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r2_1_parent");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doThrow(GSException.createException()).when(dbWriter).store((GSResource) Mockito.any());
		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(2)).discover(Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(2)).count(Mockito.any());

		Mockito.verify(dbWriter, Mockito.times(1)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(1)).store((GSResource) Mockito.any());
	}

	@Test
	public void test9() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent@s1");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		GSResource r1_3 = new Dataset();
		r1_3.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);
		s1list.add(r1_3);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");

				DiscoveryCountResponse response = new DiscoveryCountResponse();
				response.setCount(10);
				return response;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s2".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");

				DiscoveryCountResponse response = new DiscoveryCountResponse();
				response.setCount(0);
				return response;
			}
		}).when(dbFinder).count(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s1".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(s1list);
				return resultSet;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				DiscoveryMessage m = invocationOnMock.getArgument(0);

				String sidBond = ((LogicalBond) m.getUserBond().get()).getFirstOperand().toString();

				if (!"sourceId = s2".equalsIgnoreCase(sidBond))
					throw new Exception("Expected sourceId = s1");
				ResultSet<GSResource> resultSet = new ResultSet<>();
				resultSet.setResultsList(new ArrayList<>());
				return resultSet;
			}
		}).when(dbFinder).discover(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent@s1");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return Arrays.asList(collection);
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_1_parent".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata().getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_1_parent");

				return null;
			}
		}).when(dbWriter).store((GSResource) Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(1)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(1)).discover(Mockito.any());
		Mockito.verify(dbFinder, Mockito.times(2)).count(Mockito.any());

		Mockito.verify(dbReader, Mockito.times(3)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(1)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(1)).store((GSResource) Mockito.any());
	}

	@Test
	public void test10() throws Exception {

		String sourceids = "s1,s2";
		Mockito.doReturn(Optional.of(sourceids)).when(task).readTaskOptions(Mockito.any());

		List<GSSource> configuredSources = new ArrayList<>();
		GSSource source1 = new GSSource();
		source1.setUniqueIdentifier("s1");
		source1.setLabel("S1");

		GSSource source2 = new GSSource();
		source2.setUniqueIdentifier("s2");
		source2.setLabel("S2");

		configuredSources.add(source1);
		configuredSources.add(source2);

		Mockito.doReturn(configuredSources).when(task).readConfiguredSources();

		GSResource r1_1 = new Dataset();
		r1_1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_1_parent@s1");
		GSResource r1_2 = new Dataset();
		r1_2.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		GSResource r1_3 = new Dataset();
		r1_3.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier("r1_2_parent");

		List<GSResource> s1list = new ArrayList<>();
		s1list.add(r1_1);
		s1list.add(r1_2);
		s1list.add(r1_3);

		Mockito.doReturn(10).when(task).countResourcesWithParentId(Mockito.any(), Mockito.any());
		Mockito.doReturn(s1list, new ArrayList<>()).when(task).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent@s1".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent@s1");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_1_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_1_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_1_parent");

				return new ArrayList<>();
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (Database.IdentifierType.PUBLIC.compareTo(invocationOnMock.getArgument(0)) != 0)
					throw new Exception("Expected " + Database.IdentifierType.PUBLIC);

				if (!"r1_2_parent".equalsIgnoreCase(invocationOnMock.getArgument(1)))
					throw new Exception("Expcted r1_2_parent");

				GSResource collection = new DatasetCollection();
				collection.setPublicId("r1_2_parent");

				return Arrays.asList(collection);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (!"r1_1_parent".equalsIgnoreCase(((GSResource) invocationOnMock.getArgument(0)).getHarmonizedMetadata().getCoreMetadata()
						.getMIMetadata().getParentIdentifier()))
					throw new Exception("Expected parent id r1_1_parent");

				return null;
			}
		}).when(dbWriter).store((GSResource) Mockito.any());

		task.doJob(Mockito.mock(JobExecutionContext.class), Mockito.mock(SchedulerJobStatus.class));

		Mockito.verify(task, Mockito.times(2)).getResourcesWithParentId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(dbReader, Mockito.times(3)).getResources(Mockito.any(), Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(0)).remove(Mockito.any());
		Mockito.verify(dbWriter, Mockito.times(0)).store((GSResource) Mockito.any());
	}

}