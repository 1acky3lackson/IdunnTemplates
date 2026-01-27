package com.jackyblackson.idunntemplates.core.history;

import com.sk89q.worldedit.history.changeset.BlockOptimizedHistory;

import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.fastasyncworldedit.core.history.changeset.ChangeSetSummary; // 注意 FAWE 的包名
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

/**
 * 包装 FAWE 的 ChangeSet，用于附加 Idunn 插件的追踪 ID。
 */
public class IdunnChangeSet implements ChangeSet {

    private final ChangeSet delegate;
    private final UUID operationId;

    public IdunnChangeSet(ChangeSet delegate, UUID operationId) {
        this.delegate = delegate;
        this.operationId = operationId;
    }

    /**
     * 获取绑定的自定义操作 ID
     */
    public UUID getOperationId() {
        return operationId;
    }

    // --- 以下均为委托方法 (Delegate Methods) ---

    @Override
    public void add(Change change) {
        delegate.add(change);
    }

    @Override
    public boolean isRecordingChanges() {
        return delegate.isRecordingChanges();
    }

    @Override
    public void setRecordChanges(boolean recordChanges) {
        delegate.setRecordChanges(recordChanges);
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return delegate.backwardIterator();
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return delegate.forwardIterator();
    }

    @Override
    @Deprecated
    public int size() {
        return delegate.size();
    }

    // --- FAWE 特有方法的委托 ---

    @Override
    public long longSize() {
        return delegate.longSize();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void delete() {
        delegate.delete();
    }

    @Nullable
    @Override
    public ChangeSetSummary summarize(Region region, boolean shallow) {
        return delegate.summarize(region, shallow);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
