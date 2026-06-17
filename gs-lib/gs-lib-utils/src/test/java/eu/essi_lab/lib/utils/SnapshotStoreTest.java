package eu.essi_lab.lib.utils;

import org.junit.*;
import org.mockito.*;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Fabrizio
 */
public class SnapshotStoreTest {

    private ScheduledExecutorService scheduler;

    @Before
    public void setUp() {

	scheduler = mock(ScheduledExecutorService.class);

	when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(
		mock(ScheduledFuture.class));
    }

    @Test
    public void initModeTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = mock(SnapshotStore.ThrowingSupplier.class);

	List<String> expected = List.of("A", "B");

	when(supplier.get()).thenReturn(expected);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.ON_INIT, supplier, TimeUnit.MINUTES, 5);

	List<String> snapshots = store.getSnapshots();

	assertEquals(expected, snapshots);

	verify(supplier, times(1)).get();

	verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void lazilyLoadTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = mock(SnapshotStore.ThrowingSupplier.class);

	List<String> expected = List.of("A");

	when(supplier.get()).thenReturn(expected);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, supplier, TimeUnit.MINUTES, 5);

	verify(supplier, never()).get();

	List<String> result = store.getSnapshots();

	assertEquals(expected, result);

	verify(supplier, times(1)).get();
    }

    @Test
    public void shouldExecuteSupplierOnlyOnceForLazyInitializationTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = mock(SnapshotStore.ThrowingSupplier.class);

	when(supplier.get()).thenReturn(List.of("X"));

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, supplier, TimeUnit.MINUTES, 5);

	store.getSnapshots();
	store.getSnapshots();
	store.getSnapshots();

	verify(supplier, times(1)).get();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldReturnUnmodifiableListTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = () -> Arrays.asList("A");

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, supplier, TimeUnit.MINUTES, 5);

	store.getSnapshots().add("B");
    }

    @Test
    public void shouldRefreshSnapshotsWhenScheduledTaskRunsTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = mock(SnapshotStore.ThrowingSupplier.class);

	when(supplier.get()).thenReturn(List.of("FIRST")).thenReturn(List.of("SECOND"));

	ArgumentCaptor<Runnable> refreshCaptor = ArgumentCaptor.forClass(Runnable.class);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.ON_INIT, supplier, TimeUnit.MINUTES, 5);

	verify(scheduler).scheduleAtFixedRate(refreshCaptor.capture(), anyLong(), anyLong(), any());

	Runnable refresh = refreshCaptor.getValue();

	refresh.run();

	assertEquals(List.of("SECOND"), store.getSnapshots());

	verify(supplier, times(2)).get();
    }

    @Test
    public void shouldIgnoreRefreshExceptionsTest() throws Exception {

	SnapshotStore.ThrowingSupplier<List<String>> supplier = mock(SnapshotStore.ThrowingSupplier.class);

	when(supplier.get()).thenReturn(List.of("INITIAL")).thenThrow(new RuntimeException());

	ArgumentCaptor<Runnable> refreshCaptor = ArgumentCaptor.forClass(Runnable.class);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.ON_INIT, supplier, TimeUnit.MINUTES, 5);

	verify(scheduler).scheduleAtFixedRate(refreshCaptor.capture(), anyLong(), anyLong(), any());

	refreshCaptor.getValue().run();

	assertEquals(List.of("INITIAL"), store.getSnapshots());
    }

    @Test
    public void shouldShutdownSchedulerOnCloseTest() throws Exception {

	when(scheduler.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, () -> List.of(),
		TimeUnit.MINUTES, 5);

	store.close();

	verify(scheduler).shutdown();

	verify(scheduler).awaitTermination(10, TimeUnit.SECONDS);

	verify(scheduler, never()).shutdownNow();
    }

    @Test
    public void shouldForceShutdownWhenTerminationTimeoutExpiresTest() throws Exception {

	when(scheduler.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(false);

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, () -> List.of(),
		TimeUnit.MINUTES, 5);

	store.close();

	verify(scheduler).shutdown();

	verify(scheduler).shutdownNow();
    }

    @Test
    public void shouldForceShutdownWhenInterruptedTest() throws Exception {

	when(scheduler.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException());

	SnapshotStore<String> store = new SnapshotStore<>(scheduler, SnapshotStore.LoadingMode.LAZY, () -> List.of(),
		TimeUnit.MINUTES, 5);

	store.close();

	verify(scheduler).shutdownNow();

	assertTrue(Thread.currentThread().isInterrupted());

	Thread.interrupted();
    }
}
