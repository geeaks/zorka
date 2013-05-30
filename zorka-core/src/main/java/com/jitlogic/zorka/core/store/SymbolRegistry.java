/**
 * Copyright 2012-2013 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * <p/>
 * This is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.core.store;

import com.jitlogic.zorka.core.util.ZorkaLog;
import com.jitlogic.zorka.core.util.ZorkaLogger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.Closeable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SymbolRegistry implements Closeable {
    /** Logger */
    private static final ZorkaLog log = ZorkaLogger.getLog(SymbolRegistry.class);

    /** ID of last symbol added to registry. */
    private AtomicInteger lastSymbolId;

    /** Symbol name to ID map */
    private ConcurrentHashMap<String,Integer> symbolIds;

    /** Symbol ID to name map */
    private ConcurrentNavigableMap<Integer,String> symbolNames;

    private DB db;

    public SymbolRegistry() {
        lastSymbolId  = new AtomicInteger(0);
        symbolIds = new ConcurrentHashMap<String, Integer>();
        symbolNames = new ConcurrentSkipListMap<Integer, String>();
    }

    public SymbolRegistry(File file) {

        db = DBMaker.newFileDB(file)
                .randomAccessFileEnable()
                .closeOnJvmShutdown()
                .asyncFlushDelay(1)
                .make();

        symbolNames = db.getTreeMap("symbols");
        symbolIds = new ConcurrentHashMap<String, Integer>();

        lastSymbolId = new AtomicInteger(symbolNames.size() > 0 ? symbolNames.lastKey() : 0);

        for (Map.Entry<Integer,String> e : symbolNames.entrySet()) {
            symbolIds.put(e.getValue(), e.getKey());
        }
    }


    /**
     * Returns ID of named symbol. If symbol hasn't been registered yet,
     * it will be and new ID will be assigned for it.
     *
     * @param symbol symbol name
     *
     * @return symbol ID (integer)
     */
    public int symbolId(String symbol) {

        if (symbol == null) {
            return 0;
        }

        Integer id = symbolIds.get(symbol);

        if (id == null) {
            int newid = lastSymbolId.incrementAndGet();

            log.debug(ZorkaLogger.ZTR_SYMBOL_REGISTRY, "Adding symbol '%s', newid=%s", symbol, newid);

            id = symbolIds.putIfAbsent(symbol, newid);
            if (id == null) {
                symbolNames.put(newid, symbol);
                id = newid;
            }
        }

        return id;
    }


    /**
     * Returns symbol name based on ID or null if no such symbol has been registered.
     *
     * @param symbolId symbol ID
     *
     * @return symbol name
     */
    public String symbolName(int symbolId) {
        if (symbolId == 0) {
            return "<null>";
        }
        return symbolNames.get(symbolId);
    }


    /**
     * Adds new symbol to registry (with predefined ID).
     *
     * @param symbolId symbol ID
     *
     * @param symbol symbol name
     */
    public void put(int symbolId, String symbol) {

        log.debug(ZorkaLogger.ZTR_SYMBOL_REGISTRY, "Putting symbol '%s', newid=%s", symbol, symbolId);

        symbolIds.put(symbol, symbolId);
        symbolNames.put(symbolId, symbol);

        // TODO not thread safe !
        if (symbolId > lastSymbolId.get()) {
            lastSymbolId.set(symbolId);
        }
    }

    public int size() {
        return symbolIds.size();
    }

    public void flush() {
        if (db != null) {
            db.commit();
        }
    }

    @Override
    public void close() {
        if (db != null) {
            db.commit();
            db.close();
            db = null;
        }
    }
}
