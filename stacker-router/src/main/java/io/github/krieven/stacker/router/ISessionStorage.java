package io.github.krieven.stacker.router;


import io.github.krieven.stacker.common.ICallback;

/**
 * SessionStorage interface
 */
public interface ISessionStorage {
    /**
     * Find SessionStat\ck by process id and call callback with result
     *
     * @param id the process id
     * @param callback callback
     */
    void find(String id, ICallback<SessionStack> callback);

    /**
     * Save session stack to storage
     *
     * @param id the process id
     * @param session SessionStack
     */
    void save(String id, SessionStack session);
}