package de.tum.in.bluetooth.milling.machine;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a regular input stream and allows it to appear as if it is
 * non-blocking; that is, reads can be performed against it that timeout if no
 * data is seen for a period of time. This effect is achieved by having a
 * separate thread perform all non-blocking read requests and then waiting on
 * the thread to complete.
 *
 * <p>
 * VERY IMPORTANT NOTES
 * <ul>
 * <li>This class is not thread safe. It expects at most one reader.
 * <li>The {@link #shutdown()} method must be called in order to shut down the
 * thread that handles blocking I/O.
 * </ul>
 *
 */
public final class NonBlockingInputStream extends InputStream implements Runnable {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NonBlockingInputStream.class);
	private int ch = -2; // Recently read character

	private IOException exception = null;
	private final InputStream in; // The actual input stream
	private boolean isShutdown = false;
	private final boolean nonBlockingEnabled;
	private boolean threadIsReading = false;

	/**
	 * Creates a {@code NonBlockingInputStream} out of a normal blocking stream.
	 * Note that this call also spawn a separate thread to perform the blocking
	 * I/O on behalf of the thread that is using this class. The
	 * {@link #shutdown()} method must be called in order to shut this thread
	 * down.
	 *
	 * @param in
	 *            The input stream to wrap
	 * @param isNonBlockingEnabled
	 *            If true, then the non-blocking methods {@link #read(long)} and
	 *            {@link #peek(long)} will be available and, more importantly,
	 *            the thread will be started to provide support for the feature.
	 *            If false, then this class acts as a clean-passthru for the
	 *            underlying I/O stream and provides very little overhead.
	 */
	public NonBlockingInputStream(final InputStream in, final boolean isNonBlockingEnabled) {
		this.in = in;
		this.nonBlockingEnabled = isNonBlockingEnabled;

		if (isNonBlockingEnabled) {
			final Thread t = new Thread(this);
			t.setName("NonBlockingInputStreamThread");
			t.setDaemon(true);
			t.start();
		}
	}

	@Override
	public void close() throws IOException {
		/*
		 * The underlying input stream is closed first. This means that if the
		 * I/O thread was blocked waiting on input, it will be woken for us.
		 */
		this.in.close();
		this.shutdown();
	}

	/**
	 * Non-blocking is considered enabled if the feature is enabled and the I/O
	 * thread has not been shut down.
	 *
	 * @return true if non-blocking mode is enabled.
	 */
	public boolean isNonBlockingEnabled() {
		return this.nonBlockingEnabled && !this.isShutdown;
	}

	/**
	 * Peeks to see if there is a byte waiting in the input stream without
	 * actually consuming the byte.
	 *
	 * @param timeout
	 *            The amount of time to wait, 0 == forever
	 * @return -1 on eof, -2 if the timeout expired with no available input or
	 *         the character that was read (without consuming it).
	 */
	public int peek(final long timeout) throws IOException {
		if (!this.nonBlockingEnabled || this.isShutdown) {
			throw new UnsupportedOperationException(
					"peek() " + "cannot be called as non-blocking operation is disabled");
		}
		return this.read(timeout, true);
	}

	@Override
	public int read() throws IOException {
		if (this.nonBlockingEnabled) {
			return this.read(0L, false);
		}
		return this.in.read();
	}

	/**
	 * This version of read() is very specific to jline's purposes, it will
	 * always always return a single byte at a time, rather than filling the
	 * entire buffer.
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (len < 0) || (len > (b.length - off))) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int c;
		if (this.nonBlockingEnabled) {
			c = this.read(0L);
		} else {
			c = this.in.read();
		}

		if (c == -1) {
			return -1;
		}
		b[off] = (byte) c;
		return 1;
	}

	/**
	 * Attempts to read a character from the input stream for a specific period
	 * of time.
	 *
	 * @param timeout
	 *            The amount of time to wait for the character
	 * @return The character read, -1 if EOF is reached, or -2 if the read timed
	 *         out.
	 */
	public int read(final long timeout) throws IOException {
		if (!this.nonBlockingEnabled || this.isShutdown) {
			throw new UnsupportedOperationException(
					"read() with timeout " + "cannot be called as non-blocking operation is disabled");
		}
		return this.read(timeout, false);
	}

	/**
	 * Attempts to read a character from the input stream for a specific period
	 * of time.
	 *
	 * @param timeout
	 *            The amount of time to wait for the character
	 * @return The character read, -1 if EOF is reached, or -2 if the read timed
	 *         out.
	 */
	private synchronized int read(long timeout, final boolean isPeek) throws IOException {
		/*
		 * If the thread hit an IOException, we report it.
		 */
		if (this.exception != null) {
			assert this.ch == -2;
			final IOException toBeThrown = this.exception;
			if (!isPeek) {
				this.exception = null;
			}
			throw toBeThrown;
		}

		/*
		 * If there was a pending character from the thread, then we send it. If
		 * the timeout is 0L or the thread was shut down then do a local read.
		 */
		if (this.ch >= -1) {
			assert this.exception == null;
		} else if (((timeout == 0L) || this.isShutdown) && !this.threadIsReading) {
			this.ch = this.in.read();
		} else {
			/*
			 * If the thread isn't reading already, then ask it to do so.
			 */
			if (!this.threadIsReading) {
				this.threadIsReading = true;
				this.notify();
			}

			final boolean isInfinite = (timeout <= 0L);

			/*
			 * So the thread is currently doing the reading for us. So now we
			 * play the waiting game.
			 */
			while (isInfinite || (timeout > 0L)) {
				final long start = System.currentTimeMillis();

				try {
					this.wait(timeout);
				} catch (final InterruptedException e) {
					/* IGNORED */
				}

				if (this.exception != null) {
					assert this.ch == -2;

					final IOException toBeThrown = this.exception;
					if (!isPeek) {
						this.exception = null;
					}
					throw toBeThrown;
				}

				if (this.ch >= -1) {
					assert this.exception == null;
					break;
				}

				if (!isInfinite) {
					timeout -= System.currentTimeMillis() - start;
				}
			}
		}

		/*
		 * ch is the character that was just read. Either we set it because a
		 * local read was performed or the read thread set it (or failed to
		 * change it). We will return it's value, but if this was a peek
		 * operation, then we leave it in place.
		 */
		final int ret = this.ch;
		if (!isPeek) {
			this.ch = -2;
		}
		return ret;
	}

	// @Override
	@Override
	public void run() {
		LOGGER.debug("NonBlockingInputStream start");
		boolean needToShutdown = false;
		boolean needToRead = false;

		while (!needToShutdown) {

			/*
			 * Synchronize to grab variables accessed by both this thread and
			 * the accessing thread.
			 */
			synchronized (this) {
				needToShutdown = this.isShutdown;
				needToRead = this.threadIsReading;

				try {
					/*
					 * Nothing to do? Then wait.
					 */
					if (!needToShutdown && !needToRead) {
						this.wait(0);
					}
				} catch (final InterruptedException e) {
					/* IGNORED */
				}
			}

			/*
			 * We're not shutting down, but we need to read. This cannot happen
			 * while we are holding the lock (which we aren't now).
			 */
			if (!needToShutdown && needToRead) {
				int charRead = -2;
				IOException failure = null;
				try {
					charRead = this.in.read();
				} catch (final IOException e) {
					failure = e;
				}

				/*
				 * Re-grab the lock to update the state.
				 */
				synchronized (this) {
					this.exception = failure;
					this.ch = charRead;
					this.threadIsReading = false;
					this.notify();
				}
			}
		}

		LOGGER.debug("NonBlockingInputStream shutdown");
	}

	/**
	 * Shuts down the thread that is handling blocking I/O. Note that if the
	 * thread is currently blocked waiting for I/O it will not actually shut
	 * down until the I/O is received. Shutting down the I/O thread does not
	 * prevent this class from being used, but causes the non-blocking methods
	 * to fail if called and causes {@link #isNonBlockingEnabled()} to return
	 * false.
	 */
	public synchronized void shutdown() {
		if (!this.isShutdown && this.nonBlockingEnabled) {
			this.isShutdown = true;
			this.notify();
		}
	}
}