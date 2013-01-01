/**
 * Copyright 2012 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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

package com.jitlogic.zorka.agent.testspy;

import com.jitlogic.zorka.agent.testutil.ZorkaFixture;
import com.jitlogic.zorka.mbeans.MethodCallStatistics;
import com.jitlogic.zorka.rankproc.BucketAggregate;
import com.jitlogic.zorka.spy.SpyContext;
import com.jitlogic.zorka.spy.SpyDefinition;
import com.jitlogic.zorka.api.SpyLib;
import com.jitlogic.zorka.spy.collectors.JmxAttrCollector;
import com.jitlogic.zorka.spy.collectors.ZorkaStatsCollector;

import com.jitlogic.zorka.util.ZorkaUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;

import static com.jitlogic.zorka.agent.testutil.JmxTestUtil.getAttr;
import static com.jitlogic.zorka.api.SpyLib.*;

public class ZorkaStatsCollectionUnitTest extends ZorkaFixture {

    @Test
    public void testCollectToStatsMbeanWithoutPlaceholders() throws Exception {
        ZorkaStatsCollector collector = new ZorkaStatsCollector("test", "test:name=Test", "stats", "test", "C0", "C0");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put("S0",10L);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << ON_SUBMIT));
        record.put(".STAGE", ON_SUBMIT);
        collector.process(record);

        MethodCallStatistics stats =  (MethodCallStatistics)getAttr("test", "test:name=Test", "stats");

        assertNotNull("", stats);
        assertNotNull(stats.getStatistic("test"));
    }


    @Test
    public void testCollectorToStatsMbeanWithMethodNamePlaceholder() throws Exception {
        ZorkaStatsCollector collector = new ZorkaStatsCollector("test", "test:name=Test", "stats",
                "${methodName}", "S0", "S0");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << ON_SUBMIT));
        record.put(".STAGE", ON_SUBMIT);
        record.put("S0", 10L);
        collector.process(record);

        MethodCallStatistics stats =  (MethodCallStatistics)getAttr("test", "test:name=Test", "stats");

        assertNotNull("", stats);
        assertNotNull(stats.getStatistic("testMethod"));
    }


    @Test
    public void testCollectoToStatsMbeanWithClassAndMethodNamePlaceholder() throws Exception {
        ZorkaStatsCollector collector = new ZorkaStatsCollector("test", "test:name=${shortClassName}", "stats",
                "${methodName}", "S0", "S0");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "some.TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << ON_SUBMIT));
        record.put(".STAGE", ON_SUBMIT);
        record.put("S0", 10L);
        collector.process(record);

        MethodCallStatistics stats =  (MethodCallStatistics)getAttr("test", "test:name=TClass", "stats");

        assertNotNull("", stats);
        assertNotNull(stats.getStatistic("testMethod"));
    }


    @Test
    public void testCollectToStatsWithKeyExpression() throws Exception {
        ZorkaStatsCollector collector = new ZorkaStatsCollector("test", "test:name=${shortClassName}", "stats", "${C1}", "C0", "C0");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "some.TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put("C0", 10L); record.put("C1", "oja");
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << SpyLib.ON_SUBMIT));
        record.put(".STAGE", SpyLib.ON_SUBMIT);

        collector.process(record);

        MethodCallStatistics stats =  (MethodCallStatistics)getAttr("test", "test:name=TClass", "stats");

        assertNotNull("", stats);
        assertNotNull(stats.getStatistic("oja"));
    }

    // TODO test for actual calls/errors collection

    // TODO test if ctx<->stats pair is properly cached


    @Test
    public void testJmxAttrCollectorTrivialCall() throws Exception {
        JmxAttrCollector collector = new JmxAttrCollector("test", "test:name=${shortClassName}", "${methodName}", "C0", "C1");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "some.TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << ON_RETURN));
        record.put(".STAGE", ON_RETURN);
        record.put("C0", BucketAggregate.SEC);
        record.put("C1", BucketAggregate.SEC/2);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << SpyLib.ON_SUBMIT));
        record.put(".STAGE", SpyLib.ON_SUBMIT);
        collector.process(record);

        assertEquals(1L, getAttr("test", "test:name=TClass", "testMethod_calls"));
        assertEquals(0L, getAttr("test", "test:name=TClass", "testMethod_errors"));
        assertEquals(500L, getAttr("test", "test:name=TClass", "testMethod_time"));

        Object date = getAttr("test", "test:name=TClass", "testMethod_last");
        assertTrue("Should return java.util.Date object", date instanceof Date);
        assertEquals(1000L, ((Date)date).getTime());
    }


    @Test
    public void testJmxAttrCollectorTrivialError() throws Exception {
        JmxAttrCollector collector = new JmxAttrCollector("test", "test:name=${shortClassName}", "${methodName}", "C0", "C1");
        SpyContext ctx = new SpyContext(new SpyDefinition(), "some.TClass", "testMethod", "()V", 1);

        Map<String,Object> record = ZorkaUtil.map(".CTX", ctx, ".STAGE", 0, ".STAGES", 0);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << ON_ERROR));
        record.put(".STAGE", ON_ERROR);

        record.put("C0", BucketAggregate.SEC);
        record.put("C1", BucketAggregate.SEC/2);
        record.put(".STAGES", (Integer) record.get(".STAGES") | (1 << SpyLib.ON_SUBMIT));
        record.put(".STAGE", SpyLib.ON_SUBMIT);
        collector.process(record);

        assertEquals(1L, getAttr("test", "test:name=TClass", "testMethod_calls"));
        assertEquals(1L, getAttr("test", "test:name=TClass", "testMethod_errors"));
        assertEquals(500L, getAttr("test", "test:name=TClass", "testMethod_time"));

        Object date = getAttr("test", "test:name=TClass", "testMethod_last");
        assertTrue("Should return java.util.Date object", date instanceof Date);
        assertEquals(1000L, ((Date)date).getTime());
    }
}
