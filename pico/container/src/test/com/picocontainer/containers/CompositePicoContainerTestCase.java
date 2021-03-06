package com.picocontainer.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.containers.CompositePicoContainer;

@RunWith(JMock.class)
public class CompositePicoContainerTestCase {

    private final Mockery mockery = mockeryWithCountingNamingScheme();
    private DefaultPicoContainer one;
    private DefaultPicoContainer two;
    private PicoContainer composite;

    @Before
    public void setup() {
        one = new DefaultPicoContainer();
        one.addComponent(Map.class, HashMap.class);
        two = new DefaultPicoContainer();
        two.addComponent(Map.class, ConcurrentHashMap.class);
        composite = new CompositePicoContainer(one, two);
    }

    @Test
    public void testTypeCanBePrioritizedFromFirstInCompositeList() {
        Map map = composite.getComponent(Map.class);
        assertNotNull(map);
        assertTrue(map instanceof HashMap);
    }

    @Test
    public void testTypeCanBeFoundInSecondInCompositeList() {
        one.removeComponent(Map.class);
        Map map = composite.getComponent(Map.class);
        assertNotNull(map);
        assertTrue(map instanceof ConcurrentHashMap);
    }

    @Test
    public void testTypeMissingFromAllReportedAsMissing() {
        Set set = composite.getComponent(Set.class);
        assertNull(set);
    }


    @Test
    public void testTypeAndIntoCanBePrioritizedFromFirstInCompositeList() {
        Map map = composite.getComponentInto(Map.class, Object.class);
        assertNotNull(map);
        assertTrue(map instanceof HashMap);
    }

    @Test
    public void testTypeAndIntoCanBeFoundInSecondInCompositeList() {
        one.removeComponent(Map.class);
        Map map = composite.getComponentInto(Map.class, Object.class);
        assertNotNull(map);
        assertTrue(map instanceof ConcurrentMap);
    }

    @Test
    public void testTypeAndIntoMissingFromAllReportedAsMissing() {
        Set set = composite.getComponentInto(Set.class, Object.class);
        assertNull(set);
    }


    @Test
    public void testPlainObjectCanBePrioritizedFromFirstInCompositeList() {
        one.removeComponent(Map.class);
        one.addComponent("one", HashMap.class);
        Map map = (Map) composite.getComponent("one");
        assertNotNull(map);
        assertTrue(map instanceof HashMap);
    }

    @Test
    public void testPlainObjectCanBeFoundInSecondInCompositeList() {
        one.removeComponent(Map.class);
        two.removeComponent(Map.class);
        two.addComponent("two", ConcurrentHashMap.class);
        Map map = (Map) composite.getComponent("two");
        assertNotNull(map);
        assertTrue(map instanceof ConcurrentHashMap);
    }

    @Test
    public void testPlainObjectMissingFromAllReportedAsMissing() {
        Object o = composite.getComponent("three");
        assertNull(o);
    }

    @Test
    public void testPlainObjectCanBePrioritizedFromFirstInCompositeListGivingAdapter() {
        one.removeComponent(Map.class);
        one.addComponent("one", HashMap.class);
        ComponentAdapter mapAdapter = composite.getComponentAdapter("one");
        assertNotNull(mapAdapter);
        assertEquals(HashMap.class, mapAdapter.getComponentImplementation());
    }

    @Test
    public void testPlainObjectCanBeFoundInSecondInCompositeListGivingAdapter() {
        one.removeComponent(Map.class);
        two.removeComponent(Map.class);
        two.addComponent("two", ConcurrentHashMap.class);
        ComponentAdapter mapAdapter = composite.getComponentAdapter("two");
        assertNotNull(mapAdapter);
        assertEquals(ConcurrentHashMap.class, mapAdapter.getComponentImplementation());
    }

    @Test
    public void testTypedAndNameBoundObjectCanBePrioritizedFromFirstInCompositeListGivingAdapter() {
        ComponentAdapter<Map> mapAdapter = composite.getComponentAdapter(Map.class, (NameBinding) null);
        assertNotNull(mapAdapter);
        assertEquals(HashMap.class, mapAdapter.getComponentImplementation());
    }

    @Test
    public void testTypedAndAnnotationBoundObjectCanBePrioritizedFromFirstInCompositeListGivingAdapter() {
        ComponentAdapter<Map> mapAdapter = composite.getComponentAdapter(Map.class, (Class<? extends Annotation>) null);
        assertNotNull(mapAdapter);
        assertEquals(HashMap.class, mapAdapter.getComponentImplementation());
    }

    @Test
    public void testOtherMethodsReturnNothing() {
        assertEquals(Collections.EMPTY_LIST, composite.getComponents());
        assertEquals(Collections.EMPTY_LIST, composite.getComponents(Map.class));
        assertEquals(Collections.EMPTY_LIST, composite.getComponentAdapters());
        assertEquals(Collections.EMPTY_LIST, composite.getComponentAdapters(Map.class));
        assertEquals(Collections.EMPTY_LIST, composite.getComponentAdapters(Map.class, null));
        assertNull(composite.getParent());

        final PicoVisitor visitor = mockery.mock(PicoVisitor.class);
        composite.accept(visitor);
        // nothing called proven by JMock.

   }

}
