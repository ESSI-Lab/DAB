package eu.essi_lab.lib.utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class SnapshotStore<T> implements Closeable {

    /**
     *
     */
    private final Object lock;

    /**
     *
     */
    private static final int DEFAULT_INTERVAL_MINUTES = 5;

    /**
     *
     */
    private final ThrowingSupplier<List<T>> supplier;

    /**
     *
     */
    private volatile List<T> snapshotList;

    /**
     *
     */
    private final ScheduledExecutorService scheduler;

    /**
     * @author Fabrizio
     */
    public enum LoadingMode {
	/**
	 *
	 */
	ON_INIT,
	/**
	 *
	 */
	LAZY
    }

    /**
     * @param <T>
     * @author Fabrizio
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {

	T get() throws Exception;
    }

    /**
     * @param supplier
     * @throws Exception
     */
    public SnapshotStore(ThrowingSupplier<List<T>> supplier) throws Exception {

	this(LoadingMode.ON_INIT, supplier, TimeUnit.MINUTES, DEFAULT_INTERVAL_MINUTES);
    }

    /**
     * @param mode
     * @param supplier
     * @throws Exception
     */
    public SnapshotStore(LoadingMode mode, ThrowingSupplier<List<T>> supplier) throws Exception {

	this(mode, supplier, TimeUnit.MINUTES, DEFAULT_INTERVAL_MINUTES);
    }

    /**
     * @param supplier
     * @param unit
     * @param interval
     * @throws Exception
     */
    public SnapshotStore(ThrowingSupplier<List<T>> supplier, TimeUnit unit, int interval) throws Exception {

	this(LoadingMode.ON_INIT, supplier, unit, interval);
    }

    /**
     * @param mode
     * @param supplier
     * @param unit
     * @param interval
     * @throws Exception
     */
    public SnapshotStore(LoadingMode mode, ThrowingSupplier<List<T>> supplier, TimeUnit unit, int interval) throws Exception {

	this(Executors.newSingleThreadScheduledExecutor(), mode, supplier, unit, interval);
    }

    /**
     * @param scheduler
     * @param mode
     * @param supplier
     * @param unit
     * @param interval
     * @throws Exception
     */
    public SnapshotStore(//
	    ScheduledExecutorService scheduler, //
	    LoadingMode mode, //
	    ThrowingSupplier<List<T>> supplier,//
	    TimeUnit unit,//
	    int interval) throws Exception {

	this.lock = new Object();
	this.supplier = supplier;
	this.scheduler = scheduler;

	if (mode == LoadingMode.ON_INIT) {

	    this.snapshotList = supplier.get();
	}

	scheduler.scheduleAtFixedRate( //
		this::refresh,//
		0,//
		interval,//
		unit//
	);

	Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    /**
     * @return
     */
    public List<T> getSnapshots() throws Exception {

	if (snapshotList == null) {

	    synchronized (lock) {

		if (snapshotList == null) {

		    snapshotList = supplier.get();
		}
	    }
	}

	return Collections.unmodifiableList(snapshotList);
    }

    @Override
    public void close() {

	scheduler.shutdown();

	try {
	    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
		scheduler.shutdownNow();
	    }

	} catch (InterruptedException e) {

	    scheduler.shutdownNow();
	    Thread.currentThread().interrupt();
	}
    }

    /**
     *
     */
    private void refresh() {

	try {

	    snapshotList = supplier.get();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}


