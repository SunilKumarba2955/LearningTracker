package com.airtribe.learntrack.db;

import com.airtribe.learntrack.exception.TransactionException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory key-value store with ACID-style transaction semantics.
 *
 * <p>The store uses a LIFO delta-stack pattern. Each active transaction owns a
 * small map containing only the writes and deletes made in that transaction.
 * Reads walk from newest transaction frame to oldest, then fall back to the
 * committed main store. Rolling back is therefore an O(1) frame discard, and
 * committing merges only the modified keys.</p>
 *
 * @param <K> key type used to identify stored values
 * @param <V> stored value type
 */
public class TransactionalStore<K, V> {
    private final Map<K, V> mainStore = new HashMap<>();
    private final Deque<Map<K, V>> txStack = new ArrayDeque<>();
    private final V tombstone;

    /**
     * Creates a transactional store using a caller-provided tombstone marker.
     *
     * <p>The tombstone must be a unique marker that will never be used as a
     * real stored value. Deletions inside active transactions are represented
     * by writing this marker into the top delta frame.</p>
     *
     * @param tombstone unique marker value used for transactional deletions
     * @throws IllegalArgumentException if {@code tombstone} is null
     */
    public TransactionalStore(V tombstone) {
        if (tombstone == null) {
            throw new IllegalArgumentException("tombstone must not be null");
        }
        this.tombstone = tombstone;
    }

    /**
     * Starts a new transaction frame.
     *
     * <p>Nested transactions are supported by pushing another delta frame onto
     * the stack. New writes always target the top frame.</p>
     */
    public synchronized void begin() {
        txStack.push(new HashMap<>());
    }

    /**
     * Stores or updates a value.
     *
     * @param key non-null record key
     * @param value non-null record value that is not the tombstone marker
     * @throws IllegalArgumentException if key or value is null, or value is the tombstone marker
     */
    public synchronized void put(K key, V value) {
        validateKey(key);
        validateValue(value);

        if (txStack.isEmpty()) {
            mainStore.put(key, value);
        } else {
            txStack.peek().put(key, value);
        }
    }

    /**
     * Reads the current visible value for a key.
     *
     * <p>The lookup checks active transaction frames from newest to oldest
     * before consulting committed storage. A visible tombstone is returned as
     * {@code null}.</p>
     *
     * @param key non-null record key
     * @return current visible value, or {@code null} when absent or transactionally deleted
     * @throws IllegalArgumentException if {@code key} is null
     */
    public synchronized V get(K key) {
        validateKey(key);

        for (Map<K, V> frame : txStack) {
            if (frame.containsKey(key)) {
                V value = frame.get(key);
                return isTombstone(value) ? null : value;
            }
        }
        return mainStore.get(key);
    }

    /**
     * Deletes a key from the current visible store.
     *
     * <p>Outside a transaction, the key is removed directly from committed
     * storage. Inside a transaction, a tombstone is written to the top delta
     * frame so rollback can restore the previous visible state.</p>
     *
     * @param key non-null record key
     * @throws IllegalArgumentException if {@code key} is null
     */
    public synchronized void delete(K key) {
        validateKey(key);

        if (txStack.isEmpty()) {
            mainStore.remove(key);
        } else {
            txStack.peek().put(key, tombstone);
        }
    }

    /**
     * Commits the active transaction.
     *
     * <p>If this is the outermost transaction, modifications are merged into
     * committed storage. If a parent transaction exists, modifications are
     * propagated to that parent frame.</p>
     *
     * @throws TransactionException if no transaction is active
     */
    public synchronized void commit() {
        if (txStack.isEmpty()) {
            throw new TransactionException("Database Failure: No active transaction to commit.");
        }

        Map<K, V> topFrame = txStack.pop();
        if (txStack.isEmpty()) {
            mergeIntoMainStore(topFrame);
        } else {
            txStack.peek().putAll(topFrame);
        }
    }

    /**
     * Rolls back the active transaction by discarding the top delta frame.
     *
     * @throws TransactionException if no transaction is active
     */
    public synchronized void rollback() {
        if (txStack.isEmpty()) {
            throw new TransactionException("Database Failure: No active transaction to roll back.");
        }
        txStack.pop();
    }

    /**
     * Returns a consolidated view of all currently visible values.
     *
     * <p>The view starts with committed storage and applies transaction frames
     * from oldest to newest so nested changes override earlier state.</p>
     *
     * @return list of current visible values
     */
    public synchronized List<V> getAll() {
        Map<K, V> consolidated = new HashMap<>(mainStore);
        List<Map<K, V>> frames = new ArrayList<>(txStack);
        Collections.reverse(frames);

        for (Map<K, V> frame : frames) {
            mergeInto(consolidated, frame);
        }

        return new ArrayList<>(consolidated.values());
    }

    /**
     * Indicates whether at least one transaction frame is active.
     *
     * @return {@code true} when a transaction is active
     */
    public synchronized boolean isTxActive() {
        return !txStack.isEmpty();
    }

    private void mergeIntoMainStore(Map<K, V> deltaFrame) {
        mergeInto(mainStore, deltaFrame);
    }

    private void mergeInto(Map<K, V> target, Map<K, V> deltaFrame) {
        for (Map.Entry<K, V> entry : deltaFrame.entrySet()) {
            if (isTombstone(entry.getValue())) {
                target.remove(entry.getKey());
            } else {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void validateKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
    }

    private void validateValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (isTombstone(value)) {
            throw new IllegalArgumentException("value must not be the tombstone marker");
        }
    }

    private boolean isTombstone(V value) {
        return value == tombstone;
    }
}
